package com.ui;

import com.vehicle.uart.DevMaster;
import com.vehicle.uart.R;
import com.vehicle.uart.UartService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragBase extends Fragment{

    public static final int   POWER_STATE_CONNECT     =   0;
    public static final int   POWER_STATE_SET_POWER   =   1;
    public static final int   RETRY_TIMES             =   2;
   
    public View rootView;

    boolean isPowerOn = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        createDeviceListener();

//        rootView = inflater.inflate(R.layout.frag_base,
//                container, false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    public void createDeviceListener()
    {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);

        intentFilter.addAction(DevMaster.ACTION_DATA_UPDATED);
        intentFilter.addAction(DevMaster.ACTION_PACKAGE_PUSHED);
        intentFilter.addAction(DevMaster.ACTION_POWER_ON);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(deviceStatusReceiver, intentFilter);
    }
    
    private final BroadcastReceiver deviceStatusReceiver = new BroadcastReceiver()
    {
         public void onReceive(Context context, Intent intent)
        {
             String action = intent.getAction();

             if (action.equals(DevMaster.ACTION_POWER_ON))
             {
//                 isPowerOn = (DevMaster.powerOnOff == 0 )? false : true;
                 onPowerSwitch(isPowerOn);
             }
             else if(action.equals(UartService.ACTION_GATT_DISCONNECTED))
             {
                 onConnection(false);
             }
             else if(action.equals(UartService.ACTION_GATT_CONNECTED))
             {
                 onConnection(true);
             }
        }
    };
    
    public void onPowerSwitch(boolean onOff)
    {   
    }
    
    public void onConnection(boolean connected)
    {
        
    }
    
}
