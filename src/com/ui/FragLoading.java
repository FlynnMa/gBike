package com.ui;

import com.vehicle.uart.R;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class FragLoading extends Fragment{

	private View rootView;
    private ImageView img;
    private ImageView imgl;
    private ImageView imgr;
    private Handler mHandler;

	public FragLoading(){
		
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);

		rootView = inflater.inflate(R.layout.frag_loading,
				container, false);

        img = (ImageView)rootView.findViewById(R.id.img_wheel);
        imgl = (ImageView)rootView.findViewById(R.id.img_wheel_left);
        imgl.setImageAlpha(0);
        imgr = (ImageView)rootView.findViewById(R.id.img_wheel_right);
        imgr.setImageAlpha(0);

        animateWheel(img);
        
        mHandler = new Handler();
        
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                imgl.setImageAlpha(255);
                animateWheel(imgl);
            }
        }, 1000);

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                imgr.setImageAlpha(255);
                animateWheel(imgr);
            }
        }, 2000);
		return rootView;
	}
	
    @Override
    public void onDestroyView() {
        img = null;
        imgl = null;
        imgr = null;
        rootView = null;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        super.onDestroyView();
    }

	private void animateWheel(ImageView img){

        ObjectAnimator rot = ObjectAnimator.ofFloat(img, "rotation", 0, 359);
        rot.setDuration(700);
        rot.setRepeatCount(-1);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(img, "scaleX",  
                0f, 6f);  
        scaleX.setRepeatCount(0);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(img, "scaleY",  
                0f, 6f);  
        scaleY.setRepeatCount(0);

        AnimatorSet animSet = new AnimatorSet();
        animSet.play(rot).with(scaleX);
        animSet.play(scaleX).with(scaleY);
        animSet.setDuration(500);
        animSet.start();
	}

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
	
	@Override
    public void onDetach() {
        super.onDetach();
    }
}
