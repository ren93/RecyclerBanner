package com.renny.recyclerbanner;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewBanner extends FrameLayout {

    private int autoPlayDuration;//刷新间隔时间


    private boolean showIndicator;//是否显示指示器
    private RecyclerView indicatorContainer;
    private Drawable mSelectedDrawable;
    private Drawable mUnselectedDrawable;
    private IndicatorAdapter indicatorAdapter;
    private int indicatorMargin;//指示器间距

    private RecyclerView mRecyclerView;
    private RecyclerAdapter adapter;
    private LinearLayoutManager mLinearLayoutManager;

    private int WHAT_AUTO_PLAY = 1000;

    private boolean hasInit;
    private int bannerSize = 1;
    private int currentIndex;
    private boolean isPlaying;

    private boolean isAutoPlaying;
    private List<String> urlList;

    private OnBannerItemClickListener onBannerItemClickListener;


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == WHAT_AUTO_PLAY) {
                mRecyclerView.smoothScrollToPosition(++currentIndex);
                refreshIndicator();
                mHandler.sendEmptyMessageDelayed(WHAT_AUTO_PLAY, autoPlayDuration);

            }
            return false;
        }
    });

    public RecyclerViewBanner(Context context) {
        this(context, null);
    }

    public RecyclerViewBanner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecyclerViewBanner);
        showIndicator = a.getBoolean(R.styleable.RecyclerViewBanner_showIndicator, true);
        autoPlayDuration = a.getInt(R.styleable.RecyclerViewBanner_interval, 4000);
        isAutoPlaying = a.getBoolean(R.styleable.RecyclerViewBanner_autoPlaying, true);
        mSelectedDrawable = a.getDrawable(R.styleable.RecyclerViewBanner_indicatorSelectedSrc);
        mUnselectedDrawable = a.getDrawable(R.styleable.RecyclerViewBanner_indicatorUnselectedSrc);
        if (mSelectedDrawable == null) {
            //绘制默认选中状态图形
            GradientDrawable selectedGradientDrawable = new GradientDrawable();
            selectedGradientDrawable.setShape(GradientDrawable.OVAL);
            selectedGradientDrawable.setColor(getColor(R.color.colorAccent));
            selectedGradientDrawable.setSize(dp2px(5), dp2px(5));
            selectedGradientDrawable.setCornerRadius(dp2px(5) / 2);
            mSelectedDrawable = new LayerDrawable(new Drawable[]{selectedGradientDrawable});
        }
        if (mUnselectedDrawable == null) {
            //绘制默认未选中状态图形
            GradientDrawable unSelectedGradientDrawable = new GradientDrawable();
            unSelectedGradientDrawable.setShape(GradientDrawable.OVAL);
            unSelectedGradientDrawable.setColor(getColor(R.color.colorPrimaryDark));
            unSelectedGradientDrawable.setSize(dp2px(5), dp2px(5));
            unSelectedGradientDrawable.setCornerRadius(dp2px(5) / 2);
            mUnselectedDrawable = new LayerDrawable(new Drawable[]{unSelectedGradientDrawable});
        }

        indicatorMargin = a.getDimensionPixelSize(R.styleable.RecyclerViewBanner_indicatorSpace, dp2px(4));
        int marginLeft = a.getDimensionPixelSize(R.styleable.RecyclerViewBanner_indicatorMarginLeft, dp2px(16));
        int marginRight = a.getDimensionPixelSize(R.styleable.RecyclerViewBanner_indicatorMarginRight, dp2px(0));
        int marginBottom = a.getDimensionPixelSize(R.styleable.RecyclerViewBanner_indicatorMarginBottom, dp2px(11));
        int g = a.getInt(R.styleable.RecyclerViewBanner_indicatorGravity, 0);
        int gravity;
        if (g == 0) {
            gravity = GravityCompat.START;
        } else if (g == 2) {
            gravity = GravityCompat.END;
        } else {
            gravity = Gravity.CENTER;
        }
        int o = a.getInt(R.styleable.RecyclerViewBanner_orientation, 0);
        int orientation = 0;
        if (o == 0) {
            orientation = LinearLayoutManager.HORIZONTAL;
        } else if (o == 1) {
            orientation = LinearLayoutManager.VERTICAL;
        }
        a.recycle();
        //recyclerView部分
        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
        adapter = new RecyclerAdapter();
        urlList = new ArrayList<>();
        adapter.setData(urlList);
        mRecyclerView.setAdapter(adapter);
        new PagerSnapHelper().attachToRecyclerView(mRecyclerView);
        mRecyclerView.setItemAnimator(new BannerAnimator());
        mLinearLayoutManager = new LinearLayoutManager(context, orientation, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                //解决连续滑动时指示器不更新的问题
                if (bannerSize < 2) return;
                int firstReal = mLinearLayoutManager.findFirstVisibleItemPosition();
                View viewFirst = mLinearLayoutManager.findViewByPosition(firstReal);
                float width = getWidth();
                if (width != 0 && viewFirst != null) {
                    float right = viewFirst.getRight();
                    float ratio = right / width;
                    if (ratio > 0.8) {
                        if (currentIndex != firstReal) {
                            currentIndex = firstReal;
                            refreshIndicator();
                        }
                    } else if (ratio < 0.2) {
                        if (currentIndex != firstReal + 1) {
                            currentIndex = firstReal + 1;
                            refreshIndicator();
                        }
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                int first = mLinearLayoutManager.findFirstVisibleItemPosition();
                int last = mLinearLayoutManager.findLastVisibleItemPosition();
                if (currentIndex != first && first == last) {
                    currentIndex = first;
                    refreshIndicator();
                }
            }
        });
        LayoutParams vpLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mRecyclerView, vpLayoutParams);
        //指示器部分
        indicatorContainer = new RecyclerView(context);

        LinearLayoutManager indicatorLayoutManager = new LinearLayoutManager(context, orientation, false);
        indicatorContainer.setLayoutManager(indicatorLayoutManager);
        indicatorAdapter = new IndicatorAdapter();
        indicatorContainer.setAdapter(indicatorAdapter);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | gravity;
        params.setMargins(marginLeft, 0, marginRight, marginBottom);
        addView(indicatorContainer, params);
        if (!showIndicator) {
            indicatorContainer.setVisibility(GONE);
        }
    }


    /**
     * 设置轮播间隔时间
     *
     * @param millisecond 时间毫秒
     */
    public void setIndicatorInterval(int millisecond) {
        this.autoPlayDuration = millisecond;
    }

    /**
     * 设置是否自动播放（上锁）
     *
     * @param playing 开始播放
     */
    private synchronized void setPlaying(boolean playing) {
        if (isAutoPlaying && hasInit) {
            if (!isPlaying && playing && adapter != null && adapter.getItemCount() > 2) {
                mHandler.sendEmptyMessageDelayed(WHAT_AUTO_PLAY, autoPlayDuration);
                isPlaying = true;
            } else if (isPlaying && !playing) {
                mHandler.removeMessages(WHAT_AUTO_PLAY);
                isPlaying = false;
            }
        }
    }

    /**
     * 设置是否禁止滚动播放
     */
    public void setAutoPlaying(boolean isAutoPlaying) {
        this.isAutoPlaying = isAutoPlaying;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setShowIndicator(boolean showIndicator) {
        this.showIndicator = showIndicator;
        indicatorContainer.setVisibility(showIndicator ? VISIBLE : GONE);
    }

    public void setOnBannerItemClickListener(OnBannerItemClickListener onBannerItemClickListener) {
        this.onBannerItemClickListener = onBannerItemClickListener;
    }

    /**
     * 设置轮播数据集
     */
    public void initBannerImageView(@NonNull List<String> newList) {
        //解决recyclerView嵌套问题
        if (compareListDifferent(newList, this.urlList)) {
            hasInit = false;
            setVisibility(VISIBLE);
            setPlaying(false);
            urlList.clear();
            urlList.addAll(newList);
            bannerSize = urlList.size();
            adapter.notifyDataSetChanged();
            if (bannerSize > 1) {
                indicatorContainer.setVisibility(VISIBLE);
                currentIndex = bannerSize * 10000;
                mRecyclerView.scrollToPosition(currentIndex);
                indicatorAdapter.notifyDataSetChanged();
                setPlaying(true);
            } else {
                indicatorContainer.setVisibility(GONE);
                currentIndex = 0;
            }
            hasInit = true;
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setPlaying(false);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPlaying(true);
                break;
        }
        //解决recyclerView嵌套问题
        try {
            return super.dispatchTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    //解决recyclerView嵌套问题
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    //解决recyclerView嵌套问题
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setPlaying(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setPlaying(false);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            setPlaying(true);
        } else {
            setPlaying(false);
        }
    }

    /**
     * RecyclerView适配器
     */
    private class RecyclerAdapter extends RecyclerView.Adapter {
        List<String> urlList;

        public void setData(List<String> urlList) {
            this.urlList = urlList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView bannerItem = new ImageView(getContext());
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            bannerItem.setLayoutParams(params);
            bannerItem.setScaleType(ImageView.ScaleType.FIT_XY);
            bannerItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onBannerItemClickListener != null) {
                        onBannerItemClickListener.onItemClick(currentIndex % bannerSize);
                    }
                }
            });
            return new RecyclerView.ViewHolder(bannerItem) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (urlList == null || urlList.isEmpty())
                return;
            String url = urlList.get(position % bannerSize);
            ImageView img = (ImageView) holder.itemView;
            Glide.with(getContext()).load(url).into(img);
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            super.onViewRecycled(holder);
        }

        @Override
        public int getItemCount() {
            return bannerSize < 2 ? 1 : Integer.MAX_VALUE;
        }
    }

    /**
     * RecyclerView适配器
     */
    private class IndicatorAdapter extends RecyclerView.Adapter {

        int currentPosition = 0;

        public void setPosition(int currentPosition) {
            this.currentPosition = currentPosition;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ImageView bannerPoint = new ImageView(getContext());
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(indicatorMargin,indicatorMargin,indicatorMargin,indicatorMargin);
            bannerPoint.setLayoutParams(lp);
            return new RecyclerView.ViewHolder(bannerPoint) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ImageView bannerPoint = (ImageView) holder.itemView;
            bannerPoint.setImageDrawable(currentPosition == position ? mSelectedDrawable : mUnselectedDrawable);

        }

        @Override
        public int getItemCount() {
            return bannerSize;
        }
    }


    /**
     * 改变导航的指示点
     */
    private synchronized void refreshIndicator() {
        if (showIndicator && bannerSize > 1) {
            indicatorAdapter.setPosition(currentIndex % bannerSize);
            indicatorAdapter.notifyDataSetChanged();
        }
    }

    public interface OnBannerItemClickListener {
        void onItemClick(int position);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    /**
     * 获取颜色
     */
    private int getColor(@ColorRes int color) {
        return ContextCompat.getColor(getContext(), color);
    }

    private boolean compareListDifferent(List<String> newTabList, List<String> oldTabList) {
        if (oldTabList == null || oldTabList.isEmpty())
            return true;
        if (newTabList.size() != oldTabList.size())
            return true;
        for (int i = 0; i < newTabList.size(); i++) {
            if (TextUtils.isEmpty(newTabList.get(i)))
                return true;
            if (!newTabList.get(i).equals(oldTabList.get(i))) {
                return true;
            }
        }
        return false;
    }

    private class BannerAnimator extends DefaultItemAnimator {


    }

}