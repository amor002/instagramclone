package com.amr.instagramclone;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.amr.instagramclone.views.PostView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Post {

    private User user;
    private PostImage image;
    private String id;

    private String text = "";
    private Bitmap bitmap;
    private String pubDate;

    public Post(User user, Bitmap bitmap, String text) {
        this.user = user;
        this.text = text;
        this.bitmap = bitmap;

    }

    public Post(User user, Bitmap bitmap) {
        this.user = user;
        this.bitmap = bitmap;

    }

    private Post(User user, String text, String id) {
        this.user = user;
        this.text = text;
        this.id = id;
    }

    private void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public void setImage(PostImage image) {
        this.image = image;
    }

    public void save() {

        final DatabaseReference post = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getId()+"")
                .child("posts")
                .push();

        SimpleDateFormat dateFormat = new SimpleDateFormat("YY/MM/dd,hh");
        this.pubDate = dateFormat.format(new Date());
        this.id = post.getKey();
        this.image = new PostImage(this, bitmap);

        FirebaseDatabase.getInstance().getReference()
                .child("allPosts")
                .push()
                .setValue(id);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(image.isImageUrlDownloaded()) {
                    post.setValue(Post.this);
                    Log.i("info","done======>");
                    cancel();
                }
            }
        }, 1000, 1000);
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {

        return image.getImageUrl().toString();
    }

    public Bitmap getImageBitmap(int i) {
        return image.getImage();
    }

    public PostImage getPostImage(int i) {
        return image;
    }

    public User getUser(int i) {
        return user;
    }

    public static Post findPostById(DataSnapshot dataSnapshot, User user, String id) {
        DataSnapshot posts = dataSnapshot.child("allPosts");
        for (DataSnapshot pk : posts.getChildren()) {
            if (pk.getValue().equals(id)) {
                DataSnapshot dss = dataSnapshot
                        .child("users")
                        .child(user.getId())
                        .child("posts")
                        .child(id);

                String text = (String) dss.child("text").getValue();
                String pubDate = (String) dss.child("pubDate").getValue();
                Post post = new Post(user, text, id);

                post.setPubDate(pubDate);
                new PostImage(post, id);
                return post;
            }
        }
        return null;
    }
}
