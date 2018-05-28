package com.amr.instagramclone.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.amr.instagramclone.Post;
import com.amr.instagramclone.R;

public class PostView extends FrameLayout {

    public PostView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public PostView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PostView(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        View view = inflate(getContext(), R.layout.post_view, null);
        addView(view);
    }

    public void initialize(final Post post, Bitmap bitmap) {

        TextView accountName = findViewById(R.id.account_name);
        TextView pubDate = findViewById(R.id.pub_date);
        TextView text = findViewById(R.id.text);

        ImageView accountImage = findViewById(R.id.account_image);
        final ImageView postImage = findViewById(R.id.image_post);
        accountName.setText(post.getUser(0).getName());

        text.setText(post.getText());
        pubDate.setText(post.getPubDate().split(",")[0]);
        accountImage.setImageBitmap(bitmap);

        final Handler handler = new Handler();
        handler.post(new Runnable() {

            @Override
            public void run() {
                if(post.getPostImage(0).isImageDownloaded()) {
                    postImage.setImageBitmap(post.getImageBitmap(0));
                }else {
                    handler.postDelayed(this, 1000);
                }
            }
        });

    }

    public void initialize(final Post post) {

        TextView accountName = findViewById(R.id.account_name);
        TextView pubDate = findViewById(R.id.pub_date);
        TextView text = findViewById(R.id.text);

        final ImageView accountImage = findViewById(R.id.account_image);
        final ImageView postImage = findViewById(R.id.image_post);
        accountName.setText(post.getUser(0).getName());

        text.setText(post.getText());
        pubDate.setText(post.getPubDate().split(",")[0]);
        final Handler handler = new Handler();
        handler.post(new Runnable() {

            @Override
            public void run() {
                if(post.getPostImage(0).isImageDownloaded() &&
                        post.getUser(0).getImage(0).isImageDownloaded()) {

                    accountImage.setImageBitmap(post.getUser(0).getImageBitmap(0));
                    postImage.setImageBitmap(post.getImageBitmap(0));
                }else {
                    handler.postDelayed(this, 1000);
                }
            }
        });

    }
}
