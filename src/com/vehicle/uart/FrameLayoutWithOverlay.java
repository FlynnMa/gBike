/*
 * Copyright (C) 2015 Daniel.Liu Tel:13818674825
 */

package com.vehicle.uart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * A {@link FrameLayout} whose contents are kept beneath an
 * {@link AlphaTouchInterceptorOverlay}. If necessary, you can specify your own
 * alpha-layer and manually manage its z-order.
 */
public class FrameLayoutWithOverlay extends FrameLayout 
{
    private final AlphaTouchInterceptorOverlay mOverlay;

    /**
     * @param context The {@link Context} to use
     * @param attrs The attributes of the XML tag that is inflating the view
     */
    public FrameLayoutWithOverlay(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        // Programmatically create touch-interceptor View
        mOverlay = new AlphaTouchInterceptorOverlay(context);
        addView(mOverlay);
    }

    /**
     * After adding the View, bring the overlay to the front to ensure it's
     * always on top
     */
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) 
    {
        super.addView(child, index, params);
        mOverlay.bringToFront();
    }

    /**
     * Delegate to overlay: set the View that it will use as its alpha-layer. If
     * none is set, the overlay will use its own alpha layer. Only necessary to
     * set this if some child views need to appear above the alpha-layer
     */
    protected void setAlphaLayer(View layer)
    {
        mOverlay.setAlphaLayer(layer);
    }

    /** Delegate to overlay: set the alpha value on the alpha layer */
    public void setAlphaLayerValue(float alpha) 
    {
        mOverlay.setAlphaLayerValue(alpha);
    }

    /** Delegate to overlay */
    public void setOverlayOnClickListener(OnClickListener listener)
    {
        mOverlay.setOverlayOnClickListener(listener);
    }

    /** Delegate to overlay */
    public void setOverlayClickable(boolean clickable)
    {
        mOverlay.setOverlayClickable(clickable);
    }
}
