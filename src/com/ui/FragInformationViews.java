package com.ui;

import com.dd.CircularProgressButton;
import com.utility.DebugLogger;
import com.vehicle.uart.DevMaster;
import com.vehicle.uart.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FragInformationViews extends Fragment{

	private View rootView;
	
	private TextView temperatureText = null;
	private TextView batteryText = null;
	private TextView currentText = null;
	
	private ImageView temperatureImage = null;
	private ImageView batteryImage = null;
	private ImageView currentImage = null;
	
	RelativeLayout parentLayout;
	
	boolean isAnimating = false;

	public FragInformationViews(){
		
	}

	   @Override
    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

//	      if (getArguments().containsKey(ARG_ITEM_ID)) {
	            // Load the dummy content specified by the fragment
	            // arguments. In a real-world scenario, use a Loader
	            // to load content from a content provider.
//	          mItem = DummyContent.ITEM_MAP.get(getArguments().getString(
//	                  ARG_ITEM_ID));
//	      }
    }
	   
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.frag_information,
				container, false);

		// Show the dummy content as text in a TextView.
//		if (mItem != null) {
//			((TextView) rootView.findViewById(R.id.frag_detail))
//					.setText(mItem.content);
//		}
		
//		Button myButton = (Button)rootView.findViewById(R.id.button1);
//		myButton.setOnClickListener(button_listener);

//		helpText = (TextView)rootView.findViewById(R.id.helpConnectText);
//		startButton = (CircularProgressButton) rootView.findViewById(R.id.startConnectButton);
//		startButton.setIdleText(getResources().getString(R.string.start));
		
		temperatureText = (TextView)rootView.findViewById(R.id.txt_temperature);
		temperatureImage = (ImageView)rootView.findViewById(R.id.img_temperature);
//		temperatureImage.setBackgroundColor(R.color.Gray);
		temperatureText.setText("25");
//		temperatureText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		
//		parentLayout = (RelativeLayout)rootView.findViewById(R.id.devinfo_container);
		
		batteryText = (TextView)rootView.findViewById(R.id.txt_battery);
		batteryImage = (ImageView)rootView.findViewById(R.id.img_battery);
		batteryText.setText("85%");
		
		currentText = (TextView)rootView.findViewById(R.id.txt_current);
		currentImage = (ImageView)rootView.findViewById(R.id.img_current);
		currentText.setText("1024mA");
		
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            
            @Override
            public void onGlobalLayout() {

                if (true == isAnimating)
                    return;
                /*
                isAnimating = true;
                int width = rootView.getWidth();
                int height = rootView.getHeight();
                
                int imgWidth = temperatureImage.getWidth();
                int imgHeight = temperatureImage.getHeight();
                DebugLogger.d("img width is:" + imgWidth);
                
                float x = temperatureImage.getLeft();
                float y = temperatureImage.getTop();
                
                temperatureImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                temperatureImage.setAdjustViewBounds(true);
//                temperatureImage.setScaleY(0.4f);
//                temperatureImage.setScaleX(0.4f);
                RelativeLayout.LayoutParams imgPos = new RelativeLayout.LayoutParams(imgWidth / 3, imgHeight / 3);
                imgPos.addRule(RelativeLayout.CENTER_HORIZONTAL);
                imgPos.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                temperatureImage.setLayoutParams(imgPos);
                temperatureImage.setMaxHeight(imgHeight / 3);
                temperatureImage.setMaxWidth(imgWidth / 3);
                
                int imgWidth2 = temperatureImage.getWidth();
//                int imgHeight2 = temperatureImage.getHeight();
                
                if (imgWidth2 == imgWidth)
                {
                    DebugLogger.d("property is not changed!");
                }
                float y2 = temperatureImage.getTop();
                if (y == y2)
                {
                    DebugLogger.d("temperature is not changed!");
                }
                Handler delayHdl = new Handler();
                delayHdl.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        int imgWidth = temperatureImage.getWidth();
                        DebugLogger.d("img width is:" + imgWidth);
                        
                        
                        RelativeLayout.LayoutParams txtPos = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
  //                      txtPos.addRule(RelativeLayout.CENTER_HORIZONTAL);
//                        txtPos.addRule(RelativeLayout.BELOW, R.id.img_temperature);
                        txtPos.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        txtPos.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                      helpPos.addRule(RelativeLayout.ABOVE, startButtonID);
                        temperatureText.setLayoutParams(txtPos);
                        temperatureText.setText("125");


                    }
                }, 200);*/

//                PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 0.4f);  
//                PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", 0.4f);  
//                ObjectAnimator.ofPropertyValuesHolder(temperatureImage, pvhX, pvhY).setDuration(1000).start(); 
                
//                
//                temperatureImage.setScaleY(0.4f);
                
                /*
                temperatureImage.valueAnimater().scaleX(0.4f).scaleY(0.4f).setDuration(250).setListener(new AnimatorListenerAdapter(){
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Handler delayHdl = new Handler();
                        delayHdl.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                temperatureImage.animate().cancel();
                                float y = temperatureImage.getTop();
                                RelativeLayout.LayoutParams imgPos = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
                                imgPos.addRule(RelativeLayout.CENTER_HORIZONTAL);
                                imgPos.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                                temperatureImage.setLayoutParams(imgPos);
                            }
                        }, 200);
                    }
                }).start();*/
            }
        });
		
		/*
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(new     ViewTreeObserver.OnGlobalLayoutListener() {
		    public void onGlobalLayout() {
		        if(alreadyMeasured)
		            mainView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		        else
		            measureTest();
		    }
		    });*/

		return rootView;
	}

//	 @Override
//     public void onGlobalLayout() {
//      }

}
