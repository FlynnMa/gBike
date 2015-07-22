/*
 * This file implements BT binding feature
 * */
package com.vehicle.uart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vehicle.uart.DeviceListActivity.DeviceAdapter;

import android.R;
import android.R.color;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

public class BindBT extends Activity{
	RelativeLayout rLayout;
	int screenWidth;
	int screenHeight;
	int topLeftID;
	int statusBarHeight;
	Button startButton;

    private BluetoothAdapter mBluetoothAdapter;
    List<BluetoothDevice> deviceList;
    private DeviceAdapter deviceAdapter;
    Map<String, Integer> devRssiValues;
    private static final long SCAN_PERIOD = 10000; //10 seconds
    private Handler mHandler;
    private boolean mScanning;

	
	public void onCreate(Bundle savedInstanceState)
	{
		rLayout = new RelativeLayout(this);
        rLayout.setBackgroundResource(color.holo_blue_dark);
        setContentView(rLayout);
        
        rLayout.setBackgroundColor(getResources().getColor(R.color.DarkOrange));
        
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) 
		{
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null)
		{
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        deviceList = new ArrayList<BluetoothDevice>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        devRssiValues = new HashMap<String, Integer>();
        
        rLayout.postDelayed(new Runnable() {

			@Override
			public void run() {
				drawHelpScreen();
			}
		}, 10);
        
	}
	
	/*
	 * This screen shows help information to tell customer how to bind the device
	 * */
	public void drawHelpScreen()
	{
		rLayout.removeAllViews();
		
		Button cancelButton = new Button(this);
		cancelButton.setTextColor(color.white);
		cancelButton.setBackgroundResource(color.holo_red_dark);
		cancelButton.setText(R.string.cancel);
		cancelButton.setTextSize(36);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				scanLeDevice(false);
				finish();
			}
		});
		
        RelativeLayout.LayoutParams cancelBtnPos = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,50);
        cancelBtnPos.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        cancelBtnPos.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rLayout.addView(cancelButton, cancelBtnPos);


        TextView helpText = new TextView(this);
        helpText.setText(R.string.helpBinding);
        helpText.setTextSize(screenHeight / 16);
        int helpTextID = View.generateViewId();
        helpText.setId(helpTextID);
        
        RelativeLayout.LayoutParams helpPos = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,50);
        helpPos.addRule(RelativeLayout.CENTER_HORIZONTAL);
        helpPos.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        helpPos.setMargins(0, 0, 0, 40);
        rLayout.addView(helpText, helpPos);

		startButton = new Button(this);
		startButton.setTextColor(color.white);
		startButton.setBackgroundResource(color.holo_red_dark);
		startButton.setText("start");
		startButton.setAlpha(0.5f);
		startButton.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                startButton.animate().alpha(0).setDuration(500).setListener(new AnimatorListenerAdapter(){
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    	drawSearchScreen();
                    	scanLeDevice(true);
                    }}).start();
            }
        });
        RelativeLayout.LayoutParams btnPos = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,50);
        btnPos.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btnPos.addRule(RelativeLayout.ABOVE, helpTextID);
        rLayout.addView(startButton, btnPos);
        
	}
	
	/*
	 * This screen ask customer to move mobile phone close to device
	 * and get device searched
	 * */
	public void drawSearchScreen()
	{
		rLayout.removeAllViews();
		
	}
	
	/*
	 * This screen ask customer to confirm the device to bind
	 * */
	public void drawBondingScreen()
	{
		
	}
	
	private void scanLeDevice(final boolean enable)
	{
        if (enable) 
		{
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() 
            {
                @Override
                public void run() 
                {
					mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);  
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } 
		else 
		{
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
	
    public int getStatusBarHeight() {
    	Rect r = new Rect();
    	Window w = getWindow();
    	w.getDecorView().getWindowVisibleDisplayFrame(r);
    	return r.top;
    }
    
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() 
    {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) 
        {
            runOnUiThread(new Runnable() 
			{
                @Override
                public void run() 
                {
                	  runOnUiThread(new Runnable() 
					  	{
                          @Override
                          public void run() 
                          {
                              addDevice(device,rssi);
                          }
                      });
                }
            });
        }
    };
    
    private void addDevice(BluetoothDevice device, int rssi) 
	{
        boolean deviceFound = false;

        for (BluetoothDevice listDev : deviceList) 
		{
            if (listDev.getAddress().equals(device.getAddress()))
			{
                deviceFound = true;
                break;
            }
        }
        
        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) 
		{
        	deviceList.add(device);
            deviceAdapter.notifyDataSetChanged();
        }
    }

}
