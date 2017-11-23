package com.renny.recyclerbanner.banner.layoutmanager;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import static android.support.v7.widget.RecyclerView.NO_POSITION;


public class BannerLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {

    private static final float SCALE_RATE = 1.2f;
    private int mOrientation = HORIZONTAL;

    private static final int HORIZONTAL = OrientationHelper.HORIZONTAL;
    static final int VERTICAL = OrientationHelper.VERTICAL;

    private int mDecoratedMeasurement;

    private int mDecoratedMeasurementInOther;

    private int itemSpace;

    private int mSpaceMain;

    private int mSpaceInOther;

    /**
     * The offset of property which will change while scrolling
     */
    private float mOffset;

    /**
     * Many calculations are made depending on orientation. To keep it clean, this interface
     * helps {@link LinearLayoutManager} make those decisions.
     * Based on {@link #mOrientation}, an implementation is lazily created in
     */
    private OrientationHelper mOrientationHelper;

    /**
     * Works the same way as {@link android.widget.AbsListView#setSmoothScrollbarEnabled(boolean)}.
     * see {@link android.widget.AbsListView#setSmoothScrollbarEnabled(boolean)}
     */
    private boolean mSmoothScrollbarEnabled = true;

    /**
     * When LayoutManager needs to scroll to a position, it sets this variable and requests a
     * layout which will check this variable and re-layout accordingly.
     */
    private int mPendingScrollPosition = NO_POSITION;

    private SavedState mPendingSavedState = null;

    private float mInterval; //the mInterval of each item's mOffset

    OnPageChangeListener onPageChangeListener;

    private boolean mInfinite = false;

    private int mLeftItems;

    private int mRightItems;


    public BannerLayoutManager() {
        this(HORIZONTAL, 0);
    }


    public BannerLayoutManager(int orientation, int itemSpace) {
        this.itemSpace = itemSpace;
        setOrientation(orientation);
        setAutoMeasureEnabled(true);
    }

    /**
     * @return the mInterval of each item's mOffset
     */
    private float setInterval() {
        return mDecoratedMeasurement * ((1.2f - 1) / 2 + 1) + itemSpace;
    }


    /**
     * @param x start positon of the view you want scale
     * @return the scale rate of current scroll mOffset
     */
    private float calculateScale(float x) {
        float deltaX = Math.abs(x - (mOrientationHelper.getTotalSpace() - mDecoratedMeasurement) / 2f);
        float diff = 0f;
        if ((mDecoratedMeasurement - deltaX) > 0) diff = mDecoratedMeasurement - deltaX;
        return (SCALE_RATE - 1f) / mDecoratedMeasurement * diff + 1;
    }

    /**
     * cause elevation is not support below api 21,
     * so you can set your elevation here for supporting it below api 21
     * or you can just setElevation in {@link #setItemViewProperty(View, float)}
     */
    private float setViewElevation(View itemView, float targetOffset) {
        return 0;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    @Override
    public Parcelable onSaveInstanceState() {
        if (mPendingSavedState != null) {
            return new SavedState(mPendingSavedState);
        }
        SavedState savedState = new SavedState();
        savedState.position = mPendingScrollPosition;
        savedState.offset = mOffset;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            mPendingSavedState = new SavedState((SavedState) state);
            requestLayout();
        }
    }

    /**
     * @return true if {@link #getOrientation()} is {@link #HORIZONTAL}
     */
    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == HORIZONTAL;
    }

    /**
     * @return true if {@link #getOrientation()} is {@link #VERTICAL}
     */
    @Override
    public boolean canScrollVertically() {
        return mOrientation == VERTICAL;
    }

    /**
     * Returns the current orientation of the layout.
     *
     * @return Current orientation,  either {@link #HORIZONTAL} or {@link #VERTICAL}
     * @see #setOrientation(int)
     */
    int getOrientation() {
        return mOrientation;
    }

    /**
     * Sets the orientation of the layout. {@link BannerLayoutManager}
     * will do its best to keep scroll position.
     *
     * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    void setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException("invalid orientation:" + orientation);
        }
        assertNotInLayoutOrScroll(null);
        if (orientation == mOrientation) {
            return;
        }
        mOrientation = orientation;
        mOrientationHelper = null;
        removeAllViews();
    }

    @Override
    public View findViewByPosition(int position) {
        final int childCount = getChildCount();
        if (childCount == 0) {
            return null;
        }
        final int firstChild = getPosition(getChildAt(0));
        final int viewPosition = position - firstChild;
        if (viewPosition >= 0 && viewPosition < childCount) {
            final View child = getChildAt(viewPosition);
            if (getPosition(child) == position) {
                return child; // in pre-layout, this may not match
            }
        }
        return super.findViewByPosition(position);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext());
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        final int firstChildPos = getPosition(getChildAt(0));
        final float direction = targetPosition < firstChildPos ?
                -1 / getDistanceRatio() : 1 / getDistanceRatio();
        if (mOrientation == HORIZONTAL) {
            return new PointF(direction, 0);
        } else {
            return new PointF(0, direction);
        }
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            mOffset = 0;
            return;
        }
        if (mOrientationHelper == null) {
            mOrientationHelper = OrientationHelper.createOrientationHelper(this, mOrientation);
        }
        if (getChildCount() == 0) {
            View scrap = recycler.getViewForPosition(0);
            measureChildWithMargins(scrap, 0, 0);
            mDecoratedMeasurement = mOrientationHelper.getDecoratedMeasurement(scrap);
            mDecoratedMeasurementInOther = mOrientationHelper.getDecoratedMeasurementInOther(scrap);
            mSpaceMain = (mOrientationHelper.getTotalSpace() - mDecoratedMeasurement) / 2;
            mSpaceInOther = (mOrientationHelper.getTotalSpaceInOther() - mDecoratedMeasurementInOther) / 2;
            mInterval = setInterval();
            mLeftItems = (int) Math.abs(minRemoveOffset() / mInterval) + 1;
            mRightItems = (int) Math.abs(maxRemoveOffset() / mInterval) + 1;
        }

        if (mPendingSavedState != null) {
            mPendingScrollPosition = mPendingSavedState.position;
            mOffset = mPendingSavedState.offset;
        }

        if (mPendingScrollPosition != NO_POSITION) {
            mOffset = mPendingScrollPosition * mInterval;
        }

        detachAndScrapAttachedViews(recycler);
        layoutItems(recycler);
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        mPendingSavedState = null;
        mPendingScrollPosition = NO_POSITION;
    }


    private float getProperty(int position) {
        return position * mInterval;
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        removeAllViews();
        mOffset = 0;
    }

    @Override
    public void scrollToPosition(int position) {
        mPendingScrollPosition = position;
        mOffset = position * mInterval;
        requestLayout();
    }

    @Override
    public int computeHorizontalScrollOffset(RecyclerView.State state) {
        return computeScrollOffset();
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        return computeScrollOffset();
    }

    @Override
    public int computeHorizontalScrollExtent(RecyclerView.State state) {
        return computeScrollExtent();
    }

    @Override
    public int computeVerticalScrollExtent(RecyclerView.State state) {
        return computeScrollExtent();
    }

    @Override
    public int computeHorizontalScrollRange(RecyclerView.State state) {
        return computeScrollRange();
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        return computeScrollRange();
    }

    private int computeScrollOffset() {
        if (getChildCount() == 0) {
            return 0;
        }

        if (!mSmoothScrollbarEnabled) {
            return getItemCount() - getCurrentPosition() - 1;
        }

        final float realOffset = getOffsetOfRightAdapterPosition();
        return (int) realOffset;
    }

    private int computeScrollExtent() {
        if (getChildCount() == 0) {
            return 0;
        }

        if (!mSmoothScrollbarEnabled) {
            return 1;
        }

        return (int) mInterval;
    }

    private int computeScrollRange() {
        if (getChildCount() == 0) {
            return 0;
        }

        if (!mSmoothScrollbarEnabled) {
            return getItemCount();
        }

        return (int) (getItemCount() * mInterval);
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (mOrientation == VERTICAL) {
            return 0;
        }
        return scrollBy(dx, recycler, state);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (mOrientation == HORIZONTAL) {
            return 0;
        }
        return scrollBy(dy, recycler, state);
    }

    private int scrollBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0 || dy == 0) {
            return 0;
        }
        if (mOrientationHelper == null) {
            mOrientationHelper = OrientationHelper.createOrientationHelper(this, mOrientation);
        }
        int willScroll = dy;

        float realDx = dy / getDistanceRatio();
        if (Math.abs(realDx) < 0.00000001f) {
            return 0;
        }
        float targetOffset = mOffset + realDx;

        //handle the boundary
        if (!mInfinite && targetOffset < getMinOffset()) {
            willScroll -= (targetOffset - getMinOffset()) * getDistanceRatio();
        } else if (!mInfinite && targetOffset > getMaxOffset()) {
            willScroll = (int) ((getMaxOffset() - mOffset) * getDistanceRatio());
        }


        realDx = willScroll / getDistanceRatio();

        mOffset += realDx;

        // we re-layout all current views in the right place
        for (int i = 0; i < getChildCount(); i++) {
            final View scrap = getChildAt(i);
            final float delta = propertyChangeWhenScroll(scrap) - realDx;
            layoutScrap(scrap, delta);
        }

        //handle recycle
        layoutItems(recycler);

        return willScroll;
    }

    private void layoutItems(RecyclerView.Recycler recycler) {
        final float rightOffset = getOffsetOfRightAdapterPosition();
        for (int i = 0; i < getChildCount(); i++) {
            final View view = getChildAt(i);
            final int position = getPosition(view);
            if (removeCondition(getProperty(position) - rightOffset)) {
                removeAndRecycleView(view, recycler);
            }
        }

        //make sure that current position start from 0 to 1
        final int currentPos = getCurrentPositionOffset();
        int start = currentPos - mLeftItems;
        int end = currentPos + mRightItems;

        final int itemCount = getItemCount();
        if (!mInfinite) {
            if (start < 0) start = 0;
            if (end > itemCount) end = itemCount;
        }

        float lastOrderWeight = Float.MIN_VALUE;
        for (int i = start; i < end; i++) {
            if (!removeCondition(getProperty(i) - mOffset)) {
                // start and end zero base on current position,
                // so we need to calculate the adapter position
                int adapterPosition = i;
                if (i >= itemCount) {
                    adapterPosition %= itemCount;
                } else if (i < 0) {
                    int delta = (-adapterPosition) % itemCount;
                    if (delta == 0) delta = itemCount;
                    adapterPosition = itemCount - delta;
                }
                if (findViewByPosition(adapterPosition) == null) {
                    final View scrap = recycler.getViewForPosition(adapterPosition);
                    measureChildWithMargins(scrap, 0, 0);
                    resetViewProperty(scrap);
                    // we need i to calculate the real offset of current view
                    final float targetOffset = getProperty(i) - mOffset;
                    layoutScrap(scrap, targetOffset);
                    if ((float) adapterPosition > lastOrderWeight) {
                        addView(scrap);
                    } else {
                        addView(scrap, 0);
                    }
                    lastOrderWeight = (float) adapterPosition;
                }
            }
        }
    }

    private boolean removeCondition(float targetOffset) {
        return targetOffset > maxRemoveOffset() || targetOffset < minRemoveOffset();
    }

    private void resetViewProperty(View v) {
        v.setRotation(0);
        v.setRotationY(0);
        v.setRotationX(0);
        v.setScaleX(1f);
        v.setScaleY(1f);
        v.setAlpha(1f);
    }

    private float getMaxOffset() {
        return (getItemCount() - 1) * mInterval;
    }

    private float getMinOffset() {
        return 0;
    }

    private void layoutScrap(View scrap, float targetOffset) {
        final int left = calItemLeft(scrap, targetOffset);
        final int top = calItemTop(scrap, targetOffset);
        if (mOrientation == VERTICAL) {
            layoutDecorated(scrap, mSpaceInOther + left, mSpaceMain + top,
                    mSpaceInOther + left + mDecoratedMeasurementInOther, mSpaceMain + top + mDecoratedMeasurement);
        } else {
            layoutDecorated(scrap, mSpaceMain + left, mSpaceInOther + top,
                    mSpaceMain + left + mDecoratedMeasurement, mSpaceInOther + top + mDecoratedMeasurementInOther);
        }
        setItemViewProperty(scrap, targetOffset);
    }

    private void setItemViewProperty(View itemView, float targetOffset) {
        float scale = calculateScale(targetOffset + mSpaceMain);
        itemView.setScaleX(scale);
        itemView.setScaleY(scale);
    }

    private int calItemLeft(View itemView, float targetOffset) {
        return mOrientation == VERTICAL ? 0 : (int) targetOffset;
    }

    private int calItemTop(View itemView, float targetOffset) {
        return mOrientation == VERTICAL ? (int) targetOffset : 0;
    }

    /**
     * when the target offset reach this,
     * the view will be removed and recycled in {@link #layoutItems(RecyclerView.Recycler)}
     */
    private float maxRemoveOffset() {
        return mOrientationHelper.getTotalSpace() - mSpaceMain;
    }

    /**
     * when the target offset reach this,
     * the view will be removed and recycled in {@link #layoutItems(RecyclerView.Recycler)}
     */
    private float minRemoveOffset() {
        return -mDecoratedMeasurement - mOrientationHelper.getStartAfterPadding() - mSpaceMain;
    }

    private float propertyChangeWhenScroll(View itemView) {
        if (mOrientation == VERTICAL)
            return itemView.getTop() - mSpaceMain;
        return itemView.getLeft() - mSpaceMain;
    }

    private float getDistanceRatio() {
        return 1f;
    }

    public int getCurrentPosition() {
        int position = getCurrentPositionOffset();
        if (!mInfinite) return Math.abs(position);
        position = (position >= 0 ? position % getItemCount() : getItemCount() + position % getItemCount());
        return position;
    }

    private int getCurrentPositionOffset() {
        return Math.round(mOffset / mInterval);
    }

    /**
     * Sometimes we need to get the right offset of matching adapter position
     * cause when {@link #mInfinite} is set true, there will be no limitation of {@link #mOffset}
     */
    private float getOffsetOfRightAdapterPosition() {
        return mInfinite ?
                (mOffset >= 0 ?
                        (mOffset % (mInterval * getItemCount())) :
                        (getItemCount() * mInterval + mOffset % (mInterval * getItemCount()))) :
                mOffset;
    }

    /**
     * used by {@link CenterScrollListener} to center the current view
     *
     * @return the dy between center and current position
     */
    public int getOffsetToCenter() {
        if (mInfinite)
            return (int) ((getCurrentPositionOffset() * mInterval - mOffset) * getDistanceRatio());
        return (int) ((getCurrentPosition() *
                (mInterval) - mOffset) * getDistanceRatio());
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }


    private static class SavedState implements Parcelable {
        int position;
        float offset;
        boolean isReverseLayout;

        SavedState() {

        }

        SavedState(Parcel in) {
            position = in.readInt();
            offset = in.readFloat();
            isReverseLayout = in.readInt() == 1;
        }

        public SavedState(SavedState other) {
            position = other.position;
            offset = other.offset;
            isReverseLayout = other.isReverseLayout;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(position);
            dest.writeFloat(offset);
            dest.writeInt(isReverseLayout ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public interface OnPageChangeListener {
        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);
    }
}
