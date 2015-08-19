package com.ui;


import com.vehicle.uart.DevMaster;
import com.vehicle.uart.R;
import com.vehicle.uart.UartService;
import com.vehicle.uart.R.layout;

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
import android.widget.Button;
import android.widget.TextView;

public class FragMilesView extends Fragment{
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

    private static final long SHORT_PERIOD = 1000; //0.5 seconds
    
    private View rootView;
	TextView     milesView;
	TextView     recordView;
    Handler mHandler;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FragMilesView() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
//			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(
//					ARG_ITEM_ID));
//		}
	}

	private Button.OnClickListener button_listener = new Button.OnClickListener(){
		public void onClick(View v) {
//		    ((TextView) rootView.findViewById(R.id.frag_detail)).setText("I am clicked");
		}
		};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.frag_milesview,
				container, false);

		milesView = (TextView)rootView.findViewById(R.id.totalMiles);
		recordView = (TextView)rootView.findViewById(R.id.recordJorney);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DevMaster.ACTION_DATA_UPDATED);
        intentFilter.addAction(DevMaster.ACTION_POWER_ON);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(deviceStatusReceiver, intentFilter);
        
        mHandler = new Handler();
		return rootView;
	}
	
	   @Override
	    public void onDestroyView() {
	       super.onDestroyView();
           mHandler = null;
	    }
	
    Runnable shortPeriodRunable = new Runnable(){
        @Override
        public void run()
        {
            if (null == ActivityMainView.mService)
                return;
            ActivityMainView.evDevice.query(DevMaster.CMD_ID_MILE, DevMaster.DEVICE_TYPE_BIKE);
            ActivityMainView.mService.send();

            mHandler.postDelayed(shortPeriodRunable, SHORT_PERIOD);
        }
  };

    private final BroadcastReceiver deviceStatusReceiver = new BroadcastReceiver()
    {
         public void onReceive(Context context, Intent intent)
        {
             String action = intent.getAction();
             if(action.equals(DevMaster.ACTION_DATA_UPDATED))
             {
                 int mile = ActivityMainView.evDevice.mile;

                 milesView.setText(Integer.toString(mile));
             }
             else if(action.equals(DevMaster.ACTION_POWER_ON))
             {
                 /*
                 if (ActivityMainView.evDevice.powerOnOff == 0)
                 {
                     mHandler.removeCallbacks(shortPeriodRunable);
                 }
                 else
                 {
                     mHandler.postDelayed(shortPeriodRunable, SHORT_PERIOD);
                 }
                 */
             }
        }
    };
}
