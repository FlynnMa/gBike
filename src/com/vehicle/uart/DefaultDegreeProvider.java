/*
 * Copyright (C) 2015 Daniel.Liu Tel:13818674825
 */

package com.vehicle.uart;

public class DefaultDegreeProvider implements IDegreeProvider 
{
	public float[] getDegrees(int count, float totalDegrees)
	{
		if(count < 1)
        {
            return new float[]{};
        }

        float[] result = null;
        int tmpCount = 0;
        if(count < 4)
		{
            tmpCount = count+1;
        }
		else
		{
            tmpCount = count-1;
        }
        
        result = new float[count];
        float delta = totalDegrees / tmpCount;
        
        for(int index=0; index<count; index++)
		{
            int tmpIndex = index;
            if(count < 4)
			{
                tmpIndex = tmpIndex+1;
            }
            result[index] = tmpIndex * delta;
        }
        
        return result;
	}
}
