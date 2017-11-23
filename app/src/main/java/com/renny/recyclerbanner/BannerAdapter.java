package com.renny.recyclerbanner;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by test on 2017/11/22.
 */


public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {
    List<String> urlList;
    Context context;

    public BannerAdapter(Context context, List<String> urlList) {
        this.urlList = urlList;
        this.context = context;
    }

    @Override
    public BannerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false));
    }

    @Override
    public void onBindViewHolder(BannerAdapter.ViewHolder holder, int position) {
        if (urlList == null || urlList.isEmpty())
            return;
        String url = urlList.get(position % urlList.size());
        ImageView img = (ImageView) holder.imageView;
        Glide.with(context).load(url).into(img);
    }


    @Override
    public int getItemCount() {
        return urlList.size() < 2 ? 1 : Integer.MAX_VALUE;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "clicked:" + v.getTag(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
