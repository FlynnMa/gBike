package com.vehicle.uart;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * Handles scrolling back of a list tied to a header.
 * <p>
 * This is used to implement a header that scrolls up with the content of a list
 * to be partially obscured.
 */
public class BackScrollManager implements OnScrollListener 
{
    /**
     * {@code #onScrollStateChanged(AbsListView, int)} listener
     */
    private final ScrollableHeader mScrollableHeader;

    /**
     * The carousel header
     */
    private final CarouselContainer mCarousel;

    /**
     * The position of the {@link ViewPager} to scroll to
     */
    private final int mPageIndex;

    /* Constructor for <code>VerticalScrollListener</code> */
    /**
     * @param carouselHeader The {@link CarouselContainer} to move
     * @param scrollableHeader Capture onScrollStateChanged
     * @param pageIndex The position of the {@link ViewPager} this is used in
     */
    public BackScrollManager(CarouselContainer carouselHeader, ScrollableHeader scrollableHeader, int pageIndex) 
    {
        // Initialize the scoll listener
        mScrollableHeader = scrollableHeader;
        // Initialize the header
        mCarousel = carouselHeader;
        // Match the pager positions
        mPageIndex = pageIndex;
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) 
    {
        // Don't move the carousel if: 1) It is already being animated
        if (mCarousel == null || mCarousel.isTabCarouselIsAnimating()) 
		{
            return;
        }

        // If the FIRST item is not visible on the screen, then the carousel
        // must be pinned
        // at the top of the screen.
        if (firstVisibleItem != 0) 
		{
            mCarousel.moveToYCoordinate(mPageIndex, -mCarousel.getAllowedVerticalScrollLength());
            return;
        }

        final View topView = view.getChildAt(firstVisibleItem);
        if (topView == null) 
		{
            return;
        }

        final float y = view.getChildAt(firstVisibleItem).getTop();
        final float amtToScroll = Math.max(y, -mCarousel.getAllowedVerticalScrollLength());
        mCarousel.moveToYCoordinate(mPageIndex, amtToScroll);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) 
    {
        if (mScrollableHeader != null) 
		{
            mScrollableHeader.onScrollStateChanged(view, scrollState);
        }
    }

    /**
     * Defines the header to be scrolled
     */
    public interface ScrollableHeader 
    {
        /**
         * Used to capture
         * {@code BackScrollManager#onScrollStateChanged(AbsListView, int)} in
         * case you need to pause your disk cache while scrolling.
         */
        public void onScrollStateChanged(AbsListView view, int scrollState);
    }
}
