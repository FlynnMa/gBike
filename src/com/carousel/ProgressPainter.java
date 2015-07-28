package com.carousel;


public interface ProgressPainter extends Painter 
{
    void setMax(float max);
    void setMin(float min);
    void setValue(float value);
}
