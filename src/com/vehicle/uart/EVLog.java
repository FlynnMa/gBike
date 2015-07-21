/*
 * Copyright (C) 2015 Daniel.Liu Tel:13818674825
 */

package com.vehicle.uart;

import android.util.Log;

public class EVLog
{
	protected static final String TAG = "ElectronicVehicle";

	public final static void e(String msg)
	{
		if (Feature.blEnableLog)
		{
			Log.e(TAG, msg);
		}
	}
}
