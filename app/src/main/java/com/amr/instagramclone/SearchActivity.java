package com.amr.instagramclone;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amr.instagramclone.views.PostView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

public class SearchActivity extends AppCompatActivity implements ValueEventListener
        , AdapterView.OnItemClickListener, ViewTreeObserver.OnScrollChangedListener {

    public ListView suggestedUsers;
    public ArrayList<Image> images = new ArrayList<>();
    public int index;

    public boolean insideProfile = false;
    public ArrayList<String> posts;
    public ArrayList<String> names = new ArrayList<>();
    public ImageView userImage;

    public ArrayList<String> ids = new ArrayList<>();
    public DataSnapshot dss;
    public User user = null;
    public ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().hide();

        suggestedUsers = findViewById(R.id.suggested_users);
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(this);
        scrollView = findViewById(R.id.scrollView);

    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        this.dss = dataSnapshot;
        Iterator<DataSnapshot> iterator = dataSnapshot.child("allUsers").getChildren().iterator();
        boolean noneMatched = true;
        Log.i("info", "--------------------------------------------");

        while(iterator.hasNext()) {
            DataSnapshot data = iterator.next().getChildren().iterator().next();
            String name = data.getKey();
            if(nameMatched(name)) {

                User user = User.findUserById(dataSnapshot, data.getValue().toString());
                names.add(user.getName());
                images.add(user.getImage(0));
                ids.add(user.getId());
                noneMatched = false;
            }
        }
        if(noneMatched) {
            Snackbar.make(findViewById(R.id.activity_search_layout),
                    "unable to find "+getIntent().getStringExtra("query"), 4000).show();
        }else {
            SearchAdapter adapter = new SearchAdapter(this,
                    toStringArray(names),toImageArray(images), toStringArray(ids));
            suggestedUsers.setAdapter(adapter);
            suggestedUsers.setOnItemClickListener(this);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    public boolean nameMatched(String name) {

        String userQuery = getIntent().getStringExtra("query");
        Pattern pattern = Pattern.compile("[a-z-A-Z-0-9]*"+userQuery+"[a-z-A-Z-0-9]*");
        return pattern.matcher(name).matches();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        insideProfile = true;
        String pk = parent.getItemAtPosition(position).toString();
        String name = ((TextView) view.findViewById(R.id.username)).getText().toString();

        User user = User.findUserById(dss, pk);
        this.user = user;
        userImage = view.findViewById(R.id.user_image);
        ImageView profileImage = findViewById(R.id.profile_image);

        userImage.setDrawingCacheEnabled(true);
        profileImage.setImageBitmap(userImage.getDrawingCache());
        posts = user.getPosts(dss);
        index = posts.size()-1;

        ((TextView) findViewById(R.id.profile_name)).setText(name);
        ((TextView) findViewById(R.id.followers)).setText(user.getFollowersNumber()+"");
        ((TextView) findViewById(R.id.following)).setText(user.getFollowingNumber()+"");

        if(HomeActivity.user.getId().equals(user.getId())) {
            findViewById(R.id.follow_btn).setVisibility(View.INVISIBLE);
        }else {
            findViewById(R.id.follow_btn).setVisibility(View.VISIBLE);
        }

        if(HomeActivity.user.follows(user)) {
            ((Switch) findViewById(R.id.follow_btn)).setChecked(true);
        }else {
            ((Switch) findViewById(R.id.follow_btn)).setChecked(false);
        }

        for(int i=0;i<3;i++) {
            if(index < 0) {
                break;
            }
            Post post = Post.findPostById(dss, user, posts.get(index));
            PostView postView = new PostView(this);
            postView.initialize(post, userImage.getDrawingCache());
            ((LinearLayout) findViewById(R.id.profile_posts)).addView(postView);
            index--;
        }

        scrollView.getViewTreeObserver().addOnScrollChangedListener(this);
        findViewById(R.id.profile).setVisibility(View.VISIBLE);
        findViewById(R.id.suggested_users).setVisibility(View.INVISIBLE);

    }

    @Override
    public void onBackPressed() {
        if(insideProfile) {
            ((LinearLayout) findViewById(R.id.profile_posts)).removeAllViews();
            findViewById(R.id.profile).setVisibility(View.INVISIBLE);
            findViewById(R.id.suggested_users).setVisibility(View.VISIBLE);
            insideProfile = false;

        }else {
            super.onBackPressed();
        }
    }

    public void followUnfollow(View view) {
        Switch swich = (Switch) view;
        if(swich.isChecked()) {
            HomeActivity.user.addFollowing(user);
        }else {
            HomeActivity.user.removeFollowing(user);
        }
        HomeActivity.user.updateProfile(HomeActivity.user.getName());
    }

    public String[] toStringArray(ArrayList<String> arr) {
        String[] array = new String[arr.size()];
        for(int i=0;i<arr.size();i++) {
            array[i] = arr.get(i);
        }
        return array;
    }

    public Image[] toImageArray(ArrayList<Image> arr) {
        Image[] array = new Image[arr.size()];
        for(int i=0;i<arr.size();i++) {
            array[i] = arr.get(i);
        }
        return array;
    }

    @Override
    public void onScrollChanged() {
        if (scrollView.getChildAt(0).getBottom() <= (scrollView.getHeight() + scrollView.getScrollY())) {
            for(int i=0;i<3;i++) {
                if(index < 0) {
                    break;
                }
                Post post = Post.findPostById(dss, user, posts.get(index));
                PostView postView = new PostView(SearchActivity.this);

                postView.initialize(post, userImage.getDrawingCache());
                ((LinearLayout) findViewById(R.id.profile_posts)).addView(postView);
                index--;
            }
        }

    }
}
