package com.example.smartbuy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ImagesAdapter extends PagerAdapter {

    private ArrayList<StorageReference> images;
    private LayoutInflater inflater;
    private Context context;

    public ImagesAdapter(Context context, ArrayList<StorageReference> images) {
        this.images = images;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, int position) {
        View myImageLayout = inflater.inflate(R.layout.single_image_layout, view, false);
        ImageView itemImage = (ImageView) myImageLayout.findViewById(R.id.image);
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(images.get(position))
                .into(itemImage);
        view.addView(myImageLayout);
        itemImage.setOnTouchListener(new ImageMatrixTouchHandler(view.getContext()));
        return myImageLayout;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }
}
