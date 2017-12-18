package com.renny.recyclerbanner.banner;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.renny.recyclerbanner.banner.adapter.MzBannerAdapter;
import com.renny.recyclerbanner.banner.layoutmanager.BannerLayoutManager;

import java.util.List;

public class RecyclerViewBannerMz extends RecyclerViewBannerBase<BannerLayoutManager, MzBannerAdapter> {


    public RecyclerViewBannerMz(Context context) {
        this(context, null);
    }

    public RecyclerViewBannerMz(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewBannerMz(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onBannerScrollStateChanged(RecyclerView recyclerView, int newState) {
        int first = mLayoutManager.getCurrentPosition();
        if (currentIndex != first) {
            currentIndex = first;
            refreshIndicator();
        }
    }

    @Override
    protected BannerLayoutManager getLayoutManager(Context context, int orientation) {
        BannerLayoutManager bannerLayoutManager = new BannerLayoutManager(context, orientation);
        //请根据图片大小作相应调整，不然可能会出现一些显示问题
        bannerLayoutManager.setItemSpace(dp2px(15));//图片间距
        bannerLayoutManager.setCenterScale(1.2f);//中间图片缩放比例
        return bannerLayoutManager;
    }

    @Override
    protected MzBannerAdapter getAdapter(Context context, List<String> list, OnBannerItemClickListener onBannerItemClickListener) {
        return new MzBannerAdapter(context, list, onBannerItemClickListener);
    }


}