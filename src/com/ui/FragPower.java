package com.ui;

import com.cycleButton.CircleButton;
import com.utility.DebugLogger;
import com.vehicle.uart.DevMaster;
import com.vehicle.uart.R;
import com.vehicle.uart.UartService;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

public class FragPower extends Fragment implements View.OnClickListener {

    public static final int   POWER_STATE_CONNECT     =   0;
    public static final int   POWER_STATE_SET_POWER   =   1;

    public View rootView;
    CircleButton powerButton;
    Handler mHandler;
    int powerOffColor = R.color.DimGray;
    int powerOnColor = R.color.DarkGreen;
    DevMaster mDevice = null;
    
    boolean isPowerOn = false;
    boolean isSwitchingDevice = false;
    
    long delayMS;

    public FragPower(){
        mDevice = ActivityMainView.evDevice;
        mHandler = new Handler();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frag_controlview_powerbtn,
                container, false);
        
        powerButton = (CircleButton) rootView.findViewById(R.id.power_button);
        powerButton.setOnClickListener(this);
        setPowerButtonStatus();
        
        createDeviceListener();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ValueAnimator resizeAnimator = ValueAnimator.ofInt(0,100);
                        resizeAnimator.setDuration(1000);
                        resizeAnimator.setInterpolator(new LinearInterpolator());
                        
                        
                        int padding = rootView.getHeight() / 6;
                        powerButton.setPadding(padding, padding, padding, padding);

                        resizeAnimator.start();
                        resizeAnimator.addListener(resizerAnimatorListener);
                        resizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int height = rootView.getHeight();
                                int width = rootView.getWidth();
                                int diameter = height < width ? height : width;
                                diameter = diameter * 2 / 3;
                                int progress = (Integer)animation.getAnimatedValue();
                                int size = diameter * progress / 100;
                                RelativeLayout.LayoutParams layoutParam = new RelativeLayout.LayoutParams(size,size);
                                layoutParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
                                layoutParam.addRule(RelativeLayout.CENTER_VERTICAL);
                                powerButton.setLayoutParams(layoutParam);
                                
                            }
                        });

                    }
                });                
            }
        }, 150);

        return rootView;
    }
    
    @Override
    public void onDestroyView() {
        mDevice = null;
        super.onDestroyView();
    }

    public void createDeviceListener()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DevMaster.ACTION_DATA_UPDATED);
        intentFilter.addAction(DevMaster.ACTION_POWER_ON);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(deviceStatusReceiver, intentFilter);
    }
    
    private final BroadcastReceiver deviceStatusReceiver = new BroadcastReceiver()
    {
         public void onReceive(Context context, Intent intent)
        {
             String action = intent.getAction();

             if (action.equals(DevMaster.ACTION_DATA_UPDATED))
             {
                 getActivity().runOnUiThread(new Runnable()
                 {
                     public void run() {
                        
                     }
                 });
             }
             else if(action.equals(UartService.ACTION_GATT_DISCONNECTED))
             {
                 mHandler.removeCallbacks(setPowerRunable);
             }
        }
    };
    
    private void setPowerButtonStatus(){
        if (isPowerOn == false)
        {
            powerButton.setColor(powerOffColor);
        }
        else
        {
            powerButton.setColor(powerOnColor);
        }
    }
    
    private final Animator.AnimatorListener resizerAnimatorListener = new Animator.AnimatorListener() {
        
        @Override
        public void onAnimationStart(Animator animation) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void onAnimationRepeat(Animator animation) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void onAnimationEnd(Animator animation) {
            // TODO Auto-generated method stub
            powerButton.enableContinuePressedRing();
            
        }
        
        @Override
        public void onAnimationCancel(Animator animation) {
            // TODO Auto-generated method stub
            
        }
    };
    
    @Override
    public void onClick(View view) {
        DebugLogger.d("power button clicked!");
        isPowerOn = !isPowerOn;
        mHandler.postDelayed(setPowerRunable, 200);
    }

    Runnable setPowerRunable = new Runnable() {
        @Override
        public void run()
        {
            if (isPowerOn != (mDevice.powerOnOff != 0))
            {
                mDevice.setIntEx(DevMaster.CMD_ID_POWER_ONOFF, DevMaster.DEVICE_TYPE_BIKE, isPowerOn ? 1 : 0);
                mDevice.queryEx(DevMaster.CMD_ID_POWER_ONOFF, DevMaster.DEVICE_TYPE_BIKE);
            }
            else{
                mHandler.removeCallbacks(setPowerRunable);
            }
            
//            mHandler.postDelayed(setPowerRunable, delayMS);
        }
  };
    public void dataUpdated()
    {
        
    }

}
