package com.amr.instagramclone;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.amr.instagramclone.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


public class User {

    private String name;
    private String email;
    private Image image;

    private String password;
    private String id;
    private ArrayList<String> followers = new ArrayList<String>();;

    public static Resources resources;
    private String searhId;
    private ArrayList<String> following = new ArrayList<String>();

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;

    }

    public User(String id, String name, String email, String password) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;

    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public User(String id, String name, String email, String password, Image image) {
        this.id = id;
        this.name = name;

        this.email = email;
        this.password = password;
        this.image = image;
    }

    public void addFollowing(User user) {
        following.add(user.getId());
        user.addFollower(this);
        user.updateProfile(user.getName());
    }

    public void addFollower(User user) {
        getFollowers().add(user.getId());
    }

    public void removeFollowing(User user) {
        getFollowing().remove(user.getId());
        user.removeFollower(this);
        user.updateProfile(user.getName());
    }

    public void removeFollower(User user) {
        getFollowers().remove(user.getId());
    }

    public ArrayList<String> getFollowing() {
        return this.following;
    }

    public ArrayList<String> getFollowers() {
        return this.followers;
    }

    public int getFollowingNumber() {
        return following.size();
    }

    public int getFollowersNumber() {
        return followers.size();
    }

    public String getSearchId() {
        return this.searhId;
    }

    public String getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public String getImageUrl() {

        return image.getImageUrl().toString();
    }


    public ArrayList<String> getPosts(DataSnapshot dss) {
        DataSnapshot posts = dss.child("users").child(getId())
                .child("posts");

        ArrayList<String> allPosts = new ArrayList<String>();
        if(!posts.hasChildren()) {
            return allPosts;
        }

        for (DataSnapshot post : posts.getChildren()) {
            allPosts.add(post.getKey());
        }

        return allPosts;
    }

    public Image getImage(int i) {
        return this.image;
    }

    private void setFollowing(ArrayList<String> following) {
        this.following = following;
    }

    private void setFollowers(ArrayList<String> followers) {
        this.followers = followers;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setSearhId(String id) {
        this.searhId = id;
    }

    public void updateProfile(final String name) {
        Log.i("info", "new name : "+this.name);
        FirebaseDatabase.getInstance()
                .getReference("users").child(id+"").child("user").setValue(this);

        FirebaseDatabase.getInstance().getReference("allUsers")
                .child(getSearchId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                databaseReference.child(name).setValue(getId());
            }
        });
    }

    public boolean follows(User user) {
        return getFollowing().contains(user.getId());
    }

    public Bitmap getImageBitmap(int i) {
        return this.image.getImage();
    }

    @Nullable
    public static User findUserById(DataSnapshot dataSnapshot, String id) {
        DataSnapshot users = dataSnapshot.child("users");
        for(DataSnapshot ID : users.getChildren()) {
            if(ID.getKey().equals(id)) {
                DataSnapshot user = ID.child("user");
                String name = user.child("name").getValue().toString();

                String email = user.child("email").getValue().toString();
                String password = user.child("password").getValue().toString();
                String imageUrl = (String) user.child("imageUrl").getValue();
                String searchId = (String) user.child("searchId").getValue();

                ArrayList<String> following = new ArrayList<>();
                ArrayList<String> followers = new ArrayList<>();
                for(DataSnapshot dss : user.child("following").getChildren()) {
                    following.add(dss.getValue().toString());
                }
                for(DataSnapshot dss : user.child("followers").getChildren()) {
                    followers.add(dss.getValue().toString());
                }

                User userClass = new User(id, name, email, password);
                userClass.setFollowing(following);
                userClass.setFollowers(followers);
                userClass.setSearhId(searchId);

                Image image = new Image(userClass, imageUrl, MainActivity.contentResolver);
                userClass.setImage(image);
                return userClass;
            }
        }
        return null;
    }

    public Task<AuthResult> save() {

        return FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        User.this.id = authResult.getUser().getUid();

                    }
                });
    }

}
