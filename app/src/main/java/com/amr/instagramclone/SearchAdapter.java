package com.amr.instagramclone;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import com.amr.instagramclone.Image;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class SearchAdapter extends ArrayAdapter<String> {

    Image[] images;
    String[] names;
    String[] ids;

    public SearchAdapter(Context context,String[] names, Image[] images, String[] ids) {
        super(context, R.layout.adapter_search, ids);

        this.images = images;
        this.names = names;
        this.ids = ids;
    }

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View row = inflater.inflate(R.layout.adapter_search, parent, false);

        final ImageView userImage = row.findViewById(R.id.user_image);
        TextView userName = row.findViewById(R.id.username);
        userName.setText(names[position]);

        final Image image = (images[position].isImageDownloaded()) ? images[position] : images[position].downloadImage();
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(image.isImageDownloaded()) {
                    userImage.setImageBitmap(image.getImage());
                }else {
                    handler.postDelayed(this, 1500);
                }
            }
        });
        return row;
    }
}
