package com.amr.instagramclone;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class PostImage implements OnSuccessListener<byte[]>,
        OnCompleteListener<UploadTask.TaskSnapshot> {

    private Bitmap image;
    private Uri imageUrl;
    private Post post;

    public PostImage(Post post, String url) {
        this.post = post;
        this.imageUrl = Uri.parse(url);
        this.image = downloadImage().getImage();
        post.setImage(this);

    }

    public PostImage(Post post, Bitmap image) {
        this.post = post;
        this.image = image;
        uploadImage(image);
        post.setImage(this);
    }

    public void uploadImage(Bitmap image) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference posts = storageReference.child("posts");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        posts.child(post.getId()+"").putBytes(data).addOnCompleteListener(this);
    }

    public boolean isImageDownloaded() {
        return image != null;
    }

    public boolean isImageUrlDownloaded() {
        return imageUrl != null;
    }

    public Post getPost() {
        return this.post;
    }

    public Bitmap getImage() {
        return this.image;
    }

    public Uri getImageUrl() {
        Log.i("info", "image url was token");
        return this.imageUrl;
    }

    public PostImage downloadImage() {
        final long dimensions = 800 * 480;
        StorageReference storageReference = FirebaseStorage
                .getInstance().getReference("posts")
                .child(post.getId());

        storageReference.getBytes(dimensions).addOnSuccessListener(this);
        return this;
    }

    @Override
    public void onSuccess(byte[] bytes) {
        this.image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Log.i("info", "image set sync completed");

    }

    @Override
    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
        Log.i("info", "imageurl set up");
        imageUrl = task.getResult().getDownloadUrl();

    }

}
