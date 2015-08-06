package com.ui;

import com.cycleButton.CircleButton;
import com.utility.DebugLogger;
import com.vehicle.uart.DevMaster;
import com.vehicle.uart.R;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

public class FragPower extends Fragment implements View.OnClickListener {

    public View rootView;
    CircleButton powerButton;
    Handler mHandler;
    int powerOffColor = R.color.DarkGray;
    int powerOnColor = R.color.DarkGreen;
    
    int isPowerOn = 0;
    boolean isSwitchingDevice = false;

    public FragPower(){
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
                                Log.e("HelloWorld", "update x" + size + "y:" + size);
                                
                            }
                        });

                    }
                });                
            }
        }, 150);

        return rootView;
    }
    
    private void setPowerButtonStatus(){
        if (isPowerOn == 0)
        {
            powerButton.setBackgroundColor(powerOffColor);
        }
        else
        {
            powerButton.setBackgroundColor(powerOnColor);
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
    }

    public void dataUpdated()
    {
        
    }

}
