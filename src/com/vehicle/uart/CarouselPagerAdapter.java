/*
 * Copyright (C) 2015 Daniel.Liu Tel:13818674825
 */

package com.vehicle.uart;

import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import java.lang.ref.WeakReference;

/**
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class CarouselPagerAdapter implements OnPageChangeListener, OnCarouselListener 
{
    /**
     * A reference the parent {@link ViewPager}
     */
    private final WeakReference<ViewPager> mReference;

    /**
     * The carousel header
     */
    private final CarouselContainer mCarousel;

    /**
     * Constructor for <code>ViewPagerAdapter</code>
     * 
     * @param ViewPager A reference the parent {@link ViewPager}
     */
    public CarouselPagerAdapter(ViewPager viewPager, CarouselContainer carouselHeader) 
    {
        if (viewPager == null || carouselHeader == null) 
		{
            throw new IllegalStateException("The ViewPager and CarouselHeader must not be null");
        }
        mReference = new WeakReference<ViewPager>(viewPager);
        viewPager.setOnPageChangeListener(this);
        mCarousel = carouselHeader;
        mCarousel.setListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPageScrollStateChanged(int state) 
    {
        if (state == ViewPager.SCROLL_STATE_IDLE) 
		{
            mCarousel.restoreYCoordinate(75, mReference.get().getCurrentItem());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {
        if (mReference.get().isFakeDragging()) 
		{
            return;
        }

        final int scrollToX = (int) ((position + positionOffset) * mCarousel
                .getAllowedHorizontalScrollLength());
        mCarousel.scrollTo(scrollToX, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPageSelected(int position) 
    {
        mCarousel.setCurrentTab(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTouchDown() 
    {
        if (!mReference.get().isFakeDragging()) 
		{
            mReference.get().beginFakeDrag();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTouchUp() 
    {
        if (mReference.get().isFakeDragging()) 
		{
            mReference.get().endFakeDrag();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTabSelected(int position)
    {
        mReference.get().setCurrentItem(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCarouselScrollChanged(int l, int t, int oldl, int oldt) 
    {
        if (mReference.get().isFakeDragging()) 
		{
            mReference.get().fakeDragBy(oldl - l);
        }
    }
}
