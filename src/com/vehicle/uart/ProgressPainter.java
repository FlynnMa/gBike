package com.vehicle.uart;

public interface ProgressPainter extends Painter 
{
    void setMax(float max);
    void setMin(float min);
    void setValue(float value);
}
