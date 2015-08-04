package com.ui;

import com.dd.CircularProgressButton;
import com.vehicle.uart.DevMaster;
import com.vehicle.uart.R;
import com.vehicle.uart.UartService;
//import com.vehicle.uart.DeviceListActivity.DeviceAdapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;

public class FragControlViewConnectDevice extends Fragment{
    private boolean mScanning;
    BluetoothDevice detectedDevice;
    UartService     mUartService;
    DevMaster       evDevice;
	CircularProgressButton startButton;
	boolean isBindingStarted = false;
	boolean isConnecting = false;
	Runnable scanTimeOutRunable;
	
	private View rootView;
	
	TextView helpText = null;
	
	private BluetoothAdapter  mBluetoothAdapter;
	
	Handler mHandler;
	
    private static final long SCAN_PERIOD = 30000; //30 seconds
    private static final String requiredDevName = "ble2Uart"; //30 seconds

	public FragControlViewConnectDevice(){
        mUartService = UartService.getInstance();
        
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
//        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
		{
//            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
//            finish();
//        	return;
        }
        
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
//        final BluetoothManager bluetoothManager =
 //               (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
//        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null)
		{
//            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
//            finish();
            return;
        }
        

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
	      final BluetoothManager manager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
	        mBluetoothAdapter = manager.getAdapter();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.frag_controlview_connect,
				container, false);

		// Show the dummy content as text in a TextView.
//		if (mItem != null) {
//			((TextView) rootView.findViewById(R.id.frag_detail))
//					.setText(mItem.content);
//		}
		
//		Button myButton = (Button)rootView.findViewById(R.id.button1);
//		myButton.setOnClickListener(button_listener);

		helpText = (TextView)rootView.findViewById(R.id.helpConnectText);
		startButton = (CircularProgressButton) rootView.findViewById(R.id.startConnectButton);
		startButton.setIdleText(getResources().getString(R.string.start));
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBindingStarted == true)
                    return;
                isBindingStarted = true;
                startButton.setProgress(50);
                helpText.animate().alpha(0).setDuration(500).setListener(new AnimatorListenerAdapter(){
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        drawSearchScreen();
                    }}).start();
            }
        });

		return rootView;
	}

	/*
	 * This screen shows help information to tell customer how to bind the device
	 * */
	public void drawHelpScreen()
	{
		helpText.setText(R.string.helpBinding);
		
		startButton.setIndeterminateProgressMode(true);
		startButton.setProgress(0);
		startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if (isBindingStarted == true)
            		return;
            	isBindingStarted = true;
            	startButton.setProgress(50);
            	helpText.animate().alpha(0).setDuration(500).setListener(new AnimatorListenerAdapter(){
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    	drawSearchScreen();
                    }}).start();
            }
        });

	}
	
	public void drawSearchScreen(){
		helpText.setText(R.string.scanning);
		helpText.animate().alpha(1).setDuration(500).setListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animation) {
            	scanLeDevice(true);
            }}).start();
	}
	
    private void scanLeDevice(final boolean enable) {
//      final Button cancelButton = (Button) findViewById(R.id.btn_cancel);
      if (enable) {
          // Stops scanning after a pre-defined scan period.
      	scanTimeOutRunable = new Runnable(){
              @Override
              public void run()
              {
			      mScanning = false;
                  mBluetoothAdapter.stopLeScan(mLeScanCallback);
              }
      	};
          mHandler.postDelayed(scanTimeOutRunable, SCAN_PERIOD);

          mScanning = true;
          mBluetoothAdapter.startLeScan(mLeScanCallback);
      }
		else
		{
          mScanning = false;
          mBluetoothAdapter.stopLeScan(mLeScanCallback);
      }
  }
    
	/*
	 * This screen ask customer to confirm the device to bind
	 * */
	public void drawBondingScreen(BluetoothDevice device)
	{
		helpText.setText(this.getString(R.string.bleDetected) + " "
			+ device.getName() + this.getString(R.string.bindingProcess));

		helpText.animate().yBy(-10).setDuration(500).setListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animation) {

            	helpText.setText(getResources().getString(R.string.connected));
            	startButton.setProgress(100);

                mHandler.postDelayed(new Runnable() {

        			@Override
        			public void run() {
        				mUartService.connect(detectedDevice.getAddress());
//        				evDevice.getConnection();
//        				byte[] pkg = evDevice.getPackage();
//        				mUartService.writeRXCharacteristic(pkg);
//        		        finish();
        			}
        		}, 200);

            }}).start();
	}
    
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord)
        {
        	getActivity().runOnUiThread(new Runnable()
			{
                @Override
                public void run()
                {
                      onDeviceDiscovered(device, rssi);
                }
            });
        }
    };

    private void onDeviceDiscovered(BluetoothDevice device, int rssi)
    {
    	if (isConnecting == true)
    		return;

    	if ((rssi > -35) && (requiredDevName.equals(device.getName())))
    	{
    		detectedDevice = device;
        	isConnecting = true;
    		scanLeDevice(false);
    		mHandler.removeCallbacks(scanTimeOutRunable);
    		drawBondingScreen(device);
    	}
    }

}
