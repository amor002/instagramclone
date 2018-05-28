package com.amr.instagramclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.amr.instagramclone.views.PostView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ValueEventListener, SearchView.OnQueryTextListener
        ,ViewTreeObserver.OnScrollChangedListener {

    public int[] tabs = {R.id.home_tab, R.id.profile_tab, R.id.posts_tab};
    public static User user;
    public EditText name;
    public ScrollView scrollView;

    public Intent pickIntent;
    public static final int PROFILE_IMAGE = 1;
    public static final int POST_IMAGE = 2;

    public EditText email;
    public EditText password;
    public ImageView userImage;
    LinearLayout postsField;

    public DataSnapshot dss;
    public ArrayList<String> following;
    public ArrayList<String> postsIds = new ArrayList<>();
    public HashMap<String, User> dict = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.setType("image/*");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(this);
        addHomeDesign(toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(Color.BLACK);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        name = ((EditText)findViewById(R.id.name1));
        email = ((EditText)findViewById(R.id.email1));
        password = ((EditText)findViewById(R.id.password1));

        scrollView = findViewById(R.id.scroll_view1);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(this);
        postsField = findViewById(R.id.posts);

    }

    public static void addHomeDesign(Toolbar toolbar) {
        toolbar.setBackground(new ColorDrawable(Color.parseColor("#FFFFFF")));
        toolbar.setTitleTextColor(Color.parseColor("#061b30"));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("search...");
        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(Color.parseColor("#000000"));
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "بصراحة انا تعبت و كسلت اعملها, هبقي اعملها بعدين بقي (;", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void hideTabsExcept(int selected_tab) {
        for(int tab : tabs) {
            if(tab == selected_tab) {
                findViewById(tab).setVisibility(View.VISIBLE);
            }else {
                findViewById(tab).setVisibility(View.INVISIBLE);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {

            case R.id.nav_logout:
               FirebaseAuth.getInstance().signOut();
               startActivity(new Intent(this, MainActivity.class));
               this.finish();
               break;

            case R.id.nav_home:
                hideTabsExcept(R.id.home_tab);
                break;

            case R.id.nav_manage:
                hideTabsExcept(R.id.profile_tab);
                break;

            case R.id.nav_posts:
                hideTabsExcept(R.id.posts_tab);
                break;

            case R.id.nav_share:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String appUrl = "https://drive.google.com/open?id=1K_G16H4tJCQkFDT2MMXKgnriO19OzOa0";

                sharingIntent.putExtra(Intent.EXTRA_TEXT, appUrl);
                sharingIntent.setPackage("com.facebook.katana");
                startActivity(sharingIntent);
                break;

            default:
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        Log.i("info", "data downloaded");
        User user = User.findUserById(dataSnapshot, FirebaseAuth.getInstance().getUid());
        this.user = user;

        final ImageView image = findViewById(R.id.drawer_image);
        userImage = findViewById(R.id.image1);

        final TextView name = findViewById(R.id.drawer_name);
        final TextView email = findViewById(R.id.drawer_email);

        this.name.setText(user.getName());
        this.email.setText(user.getEmail());
        this.password.setText(user.getPassword());

        name.setText(user.getName());
        email.setText(user.getEmail());
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if(HomeActivity.this.user.getImage(0).isImageDownloaded()) {
                    image.setImageBitmap(HomeActivity.this.user.getImageBitmap(0));
                    userImage.setImageBitmap(HomeActivity.this.user.getImageBitmap(0));
                }else {
                    handler.postDelayed(this, 1000);
                }
            }
        });

        ((TextView)findViewById(R.id.followers)).setText(user.getFollowersNumber()+"");
        ((TextView)findViewById(R.id.following)).setText(user.getFollowingNumber()+"");
        LinearLayout myPosts = findViewById(R.id.my_posts);
        for(String postId : user.getPosts(dataSnapshot)) {

            Post post = Post.findPostById(dataSnapshot, user, postId);
            PostView postView = new PostView(this);
            postView.initialize(post);
            myPosts.addView(postView);
        }

        Random random = new Random();
        following = (ArrayList<String>) user.getFollowing().clone();
        for(int i=0;i<5;i++) {
            if(following.size() < 1) {
                break;
            }
            User followingUser = User.findUserById(dataSnapshot, following.remove(random.nextInt(following.size())));
            for(String postId : followingUser.getPosts(dataSnapshot)) {
                postsIds.add(postId);
                dict.put(postId, followingUser);
            }
        }
        this.dss = dataSnapshot;
        showPosts();
    }

    public void showPosts() {
        Random random = new Random();

        for (int i=0;i<5;i++) {
            if(postsIds.size() < 1) {
                break;
            }
            String postId = postsIds.remove(random.nextInt(postsIds.size()));
            Post post = Post.findPostById(dss, dict.remove(postId), postId);

            PostView postView = new PostView(this);
            postView.initialize(post);
            postsField.addView(postView);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("query", query);
        startActivity(intent);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PROFILE_IMAGE && data != null) {
            userImage.setImageURI(data.getData());
        }else if(requestCode == POST_IMAGE && data != null) {

            ImageView postImage = findViewById(R.id.post_image);
            postImage.setImageURI(data.getData());
            postImage.setTag("true");

        }

    }

    public void saveChanges(View view) {

        ImageView imageView = findViewById(R.id.image1);
        imageView.setDrawingCacheEnabled(true);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("updating profile");
        progressDialog.show();

        final String newName = name.getText().toString()+"";
        final String newPassword = password.getText().toString()+"";
        final String newEmail = email.getText().toString()+"";

        user.setName(newName);
        user.setEmail(newEmail);
        user.setPassword(newPassword);
        Log.i("info", newName);

        FirebaseAuth.getInstance().getCurrentUser().updateEmail(newEmail);
        FirebaseAuth.getInstance().getCurrentUser().updatePassword(newPassword);

        final Handler handler = new Handler();
        handler.post(new Runnable(){
            public void run() {

                if(HomeActivity.this.user.getImage(0).isImageUrlDownloaded()) {
                    progressDialog.dismiss();
                    user.updateProfile(newName);
                    Toast.makeText(HomeActivity.this, "profile update success", Toast.LENGTH_SHORT).show();
                }else {
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    public void pickImage(View view) {
        startActivityForResult(pickIntent, PROFILE_IMAGE);

    }

    public void uploadPostImage(View view) {
        startActivityForResult(pickIntent, POST_IMAGE);
    }

    public void sharePost(View view) {

        TextView postLabel = findViewById(R.id.post_label);
        ImageView postImage = findViewById(R.id.post_image);

        if(Boolean.parseBoolean(postImage.getTag().toString())) {

            postImage.setDrawingCacheEnabled(true);
            Post post = new Post(this.user, postImage.getDrawingCache(), postLabel.getText().toString());
            post.save();

            Toast.makeText(this, "post saved", Toast.LENGTH_LONG).show();
            postImage.setImageResource(R.drawable.placeholder);
            postLabel.setText("");
            postImage.setTag("false");

        }else {
            Toast.makeText(this, "you have to select an image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onScrollChanged() {
        if (scrollView.getChildAt(0).getBottom() <= (scrollView.getHeight() + scrollView.getScrollY())) {
            this.showPosts();

        }

    }
}
