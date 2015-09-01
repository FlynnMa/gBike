package com.ui;

import com.vehicle.uart.DevMaster;
import com.vehicle.uart.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    Handler mHandler;
    Activity mActivity = null;

    private static final long LONG_PERIOD = 10000; //10 seconds
    private static final long SHORT_PERIOD = 5000; //0.5 seconds

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


		temperatureText = (TextView)rootView.findViewById(R.id.txt_temperature);
		temperatureImage = (ImageView)rootView.findViewById(R.id.img_temperature);
//		temperatureImage.setBackgroundColor(R.color.Gray);
//		temperatureText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

//		parentLayout = (RelativeLayout)rootView.findViewById(R.id.devinfo_container);

		batteryText = (TextView)rootView.findViewById(R.id.txt_battery);
		batteryImage = (ImageView)rootView.findViewById(R.id.img_battery);

		currentText = (TextView)rootView.findViewById(R.id.txt_current);
		currentImage = (ImageView)rootView.findViewById(R.id.img_current);

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DevMaster.ACTION_DATA_UPDATED);
        intentFilter.addAction(DevMaster.ACTION_POWER_ON);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(deviceStatusReceiver, intentFilter);

        mHandler = new Handler();
//        mHandler.postDelayed(longPeriodRunable, LONG_PERIOD);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                if (true == isAnimating)
                    return;
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

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }
	
	@Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    Runnable longPeriodRunable = new Runnable(){
        @Override
        public void run()
        {
            ActivityMainView.evDevice.queryBoardTemperature();
            ActivityMainView.evDevice.queryChargeStatus();
            ActivityMainView.evDevice.queryLowVoltage();
            ActivityMainView.evDevice.queryVoltage();
            ActivityMainView.mService.send();

//            mHandler.postDelayed(longPeriodRunable, LONG_PERIOD);
        }
  };
  
  Runnable shortPeriodRunable = new Runnable(){
      @Override
      public void run()
      {
          ActivityMainView.evDevice.query(DevMaster.CMD_ID_CURRENT, DevMaster.DEVICE_TYPE_BIKE);
          ActivityMainView.mService.send();

//          mHandler.postDelayed(longPeriodRunable, SHORT_PERIOD);
      }
};

	   private final BroadcastReceiver deviceStatusReceiver = new BroadcastReceiver()
	    {
	         public void onReceive(Context context, Intent intent)
	        {
	             String action = intent.getAction();

	             if (action.equals(DevMaster.ACTION_DATA_UPDATED))
	             {
	                 float temp = ActivityMainView.evDevice.mainboardTemperiture;
	                 temperatureText.setText(Float.toString(temp));

	                 float vol = ActivityMainView.evDevice.voltage;
	                 if (vol != 0f)
	                 {
	                     batteryText.setText(Float.toString(vol));
	                 }
	                         
	                 float curr = ActivityMainView.evDevice.current;
	                 currentText.setText(Float.toString(curr));
	             }
	             else if(action.equals(DevMaster.ACTION_POWER_ON))
	             {
	                 /*
	                 if (ActivityMainView.evDevice.powerOnOff == 0)
	                 {
                         mHandler.removeCallbacks(longPeriodRunable);
                         mHandler.removeCallbacks(shortPeriodRunable);
	                 }
	                 else
	                 {
                         mHandler.postDelayed(longPeriodRunable, LONG_PERIOD);
                         mHandler.postDelayed(shortPeriodRunable, LONG_PERIOD);
	                 }
	                 */
	             }
	        }
	    };

//	 @Override
//     public void onGlobalLayout() {
//      }

}
