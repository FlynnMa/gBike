/*
 * Copyright (C) 2015 Daniel.Liu Tel:13818674825
 */

package com.vehicle.uart;

/**
 * Interface for callbacks invoked when the user interacts with the carousel.
 */
public interface OnCarouselListener 
{
    /**
     * Determines when the user is touching the carousel
     */
    public void onTouchDown();

    /**
     * Determines when the user lifts their finger up from the carousel
     */
    public void onTouchUp();

    /**
     * @param l Current horizontal scroll origin
     * @param t Current vertical scroll origin
     * @param oldl Previous horizontal scroll origin
     * @parm oldt Previous vertical scroll origin
     */
    public void onCarouselScrollChanged(int l, int t, int oldl, int oldt);

    /**
     * Called when a tab is selected
     * 
     * @param position The position of the selected tab
     */
    public void onTabSelected(int position);
}
