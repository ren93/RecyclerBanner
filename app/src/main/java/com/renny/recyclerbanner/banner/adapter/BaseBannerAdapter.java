package com.renny.recyclerbanner.banner.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by test on 2017/11/22.
 */


public abstract class BaseBannerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected List<String> urlList;
    protected Context context;

    public BaseBannerAdapter(Context context, List<String> urlList) {
        this.urlList = urlList;
        this.context = context;
    }

    @Override
    public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return createCustomViewHolder(parent, viewType);
    }

    /**
     * 创建自定义的ViewHolder
     *
     * @param parent   父类容器
     * @param viewType view类型{@link #getItemViewType(int)}
     * @return ImgHolder
     */
    protected abstract VH createCustomViewHolder(ViewGroup parent, int viewType);


    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindCustomViewHolder((VH) holder, position);
    }

    /**
     * 绑定自定义的ViewHolder
     *
     * @param holder   ImgHolder
     * @param position 位置
     */
    public abstract void bindCustomViewHolder(VH holder, int position);

    @Override
    public int getItemCount() {
        return urlList.size() < 2 ? 1 : Integer.MAX_VALUE;
    }

}
