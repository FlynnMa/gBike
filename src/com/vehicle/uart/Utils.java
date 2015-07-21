/*
 * Copyright (C) 2015 Daniel.Liu Tel:13818674825
 */

package com.vehicle.uart;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.Resources.Theme;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

/**
 * Helpers
 */
public final class Utils
{
    /* This class is never initiated */
    public Utils() 
    {
    }

    /**
     * Used to determine if the device is running Honeycomb or greater
     * 
     * @return True if the device is running Honeycomb or greater, false
     *         otherwise
     */
    public static final boolean hasHoneycomb() 
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Used to determine if the device is running Jelly Bean or greater
     * 
     * @return True if the device is running Jelly Bean or greater, false
     *         otherwise
     */
    public static final boolean hasJellyBean()
    {
        return Build.VERSION.SDK_INT >= 16;
    }

    /**
     * Resolves the given attribute id of the theme to a resource id
     */
    public static int getAttribute(Theme theme, int attrId) 
    {
        final TypedValue outValue = new TypedValue();
        theme.resolveAttribute(attrId, outValue, true);
        return outValue.resourceId;
    }

    /**
     * Returns the resource id of the background used for buttons to show
     * pressed and focused state
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static int getSelectableItemBackground(Theme theme) 
    {
        if (hasHoneycomb()) 
		{
            return getAttribute(theme, android.R.attr.selectableItemBackground);
        }
        return 0;
    }

    /**
     * Sets an alpha value on the view.
     */
    public static void setAlphaOnViewBackground(View view, float alpha) 
    {
        if (view != null) 
		{
            view.setBackgroundColor((int) (clamp(alpha, 0.0f, 1.0f) * 255) << 24);
        }
    }

    /**
     * If the input value lies outside of the specified range, return the nearer
     * bound. Otherwise, return the input value, unchanged.
     */
    public static float clamp(float input, float lowerBound, float upperBound)
    {
        if (input < lowerBound) 
		{
            return lowerBound;
        }
		else if (input > upperBound) 
        {
            return upperBound;
        }
        return input;
    }

    /**
     * Runs a piece of code after the next layout run
     * 
     * @param view The {@link View} used.
     * @param runnable The {@link Runnable} used after the next layout run
     */
    @SuppressLint("NewApi")
    public static void doAfterLayout(final View view, final Runnable runnable) 
    {
        final OnGlobalLayoutListener listener = new OnGlobalLayoutListener()
		{
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout()
            {
                /* Layout pass done, unregister for further events */
                if (hasJellyBean()) 
				{
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } 
				else 
				{
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                runnable.run();
            }
        };
        view.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }
}
