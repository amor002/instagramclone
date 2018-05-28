package com.amr.instagramclone;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class Image implements OnSuccessListener<byte[]>,
        OnCompleteListener<UploadTask.TaskSnapshot>{

    private Bitmap image;
    private Uri imageUrl;
    private User user;

    public Image(User user, String url, ContentResolver cr7) {
        this.user = user;
        user.setImage(this);
        this.imageUrl = Uri.parse(url);
        this.image = downloadImage().getImage();

    }

    public Image(User user, Resources res, int id) {

        this.user = user;
        image = BitmapFactory.decodeResource(res, id);
        uploadImage(image);

    }

    public Image(User user, Bitmap image) {
        this.user = user;
        this.image = image;
        uploadImage(image);
    }

    public void uploadImage(Bitmap image) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference users = storageReference.child("users");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        users.child(user.getId()+"").putBytes(data).addOnCompleteListener(this);
    }

    public boolean isImageDownloaded() {
        return image != null;
    }

    public boolean isImageUrlDownloaded() {
        return imageUrl != null;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

    public Bitmap getImage() {
        return this.image;
    }

    public Uri getImageUrl() {
        Log.i("info", "image url was token");
        return this.imageUrl;
    }

    public Image downloadImage() {
        final long MEGABYTE = 1024 * 1024;
        StorageReference storageReference = FirebaseStorage.
                getInstance().getReference("users").child(user.getId());

        storageReference.getBytes(MEGABYTE).addOnSuccessListener(this);
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

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getId()+"")
                .child("user")
                .child("imageUrl").setValue(imageUrl.toString());
    }
}
