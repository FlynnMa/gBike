/*
 * Copyright (C) 2015 Daniel.Liu Tel:13818674825
 */

package com.vehicle.uart;

import android.graphics.Canvas;

/**
 * Painter delegate the onDraw method in canvas to draw method here, each painter paints something
 * of the view
 */
public interface Painter 
{
    void draw(Canvas canvas);
    void setColor(int color);
    int getColor();
    void onSizeChanged(int height, int width);
}
