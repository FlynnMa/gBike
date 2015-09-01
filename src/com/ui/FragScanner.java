/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Set;
import java.util.UUID;

import com.vehicle.uart.R;
import com.vehicle.uart.UartService;
import com.dd.CircularProgressButton;
import com.uart.ScannerServiceParser;
import com.utility.DebugLogger;

/**
 * ScannerFragment class scan required BLE devices and shows them in a list. This class scans and filter devices with standard BLE Service UUID and devices with custom BLE Service UUID It contains a
 * list and a button to scan/cancel. There is a interface {@link OnDeviceSelectedListener} which is implemented by activity in order to receive selected device. The scanning will continue for 5
 * seconds and then stop
 */
@SuppressLint("InlinedApi")
public class FragScanner extends Fragment{
	private final static String PARAM_UUID = "param_uuid";
	private final static String DISCOVERABLE_REQUIRED = "discoverable_required";
    private final static String PARAM_RECORD_ADDRESS = "recorded_addr";
    private final static String PARAM_RUN_INBACKGROUND = "background";

	private final static long SCAN_DURATION = 5000;
	
	private final static long FAST_SCAN_DURATION = 800;

    private final int SCANNER_STATE_NONE         = 0;

    private final int SCANNER_STATE_SCAN_STOP    = 1;

    private final int SCANNER_STATE_SCANNING     = 2;
	
	private final int SCANNER_STATE_CONNECTING   = 3;
	
	private final int SCANNER_STATE_CONNECTED    = 4;
	
	private int scannerState;

	private BluetoothAdapter mBluetoothAdapter;
//	private OnDeviceSelectedListener mListener;
	Handler mHandler;

	private boolean mDiscoverableRequired;
	UUID mUuid;

	private boolean mIsInBackground = true;

	private boolean mIsScanning = false;
	
	private boolean mIsConnecting = false;
	
    private static final String expectedDevName = "ysport";
    
    BluetoothDevice detectedDevice;
    
    private View rootView;
	    
    TextView helpText = null;

    CircularProgressButton startButton;
    
    Runnable scanTimeOutRunable;
    
    UartService mUartService;
 	/* package */static final int NO_RSSI = -1000;
    String mDevAddress;

    public FragScanner(){
        mHandler = new Handler();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }
	/**
	 * Static implementation of fragment so that it keeps data when phone orientation is changed For standard BLE Service UUID, we can filter devices using normal android provided command
	 * startScanLe() with required BLE Service UUID For custom BLE Service UUID, we will use class ScannerServiceParser to filter out required device.
	 */
	public static FragScanner getInstance(final Context context, final UUID uuid,
	        final String addr, final boolean discoverableRequired, final boolean isBackground) {
		final FragScanner fragment = new FragScanner();

		fragment.mDevAddress = addr;
		fragment.mUuid = uuid;
		fragment.mDiscoverableRequired = discoverableRequired;
		if (isBackground)
		    fragment.startBackgroundScan();
		return fragment;
	}

	/**
	 * Interface required to be implemented by activity.
	 */
	public static interface OnDeviceSelectedListener {
		/**
		 * Fired when user selected the device.
		 * 
		 * @param device
		 *            the device to connect to
		 * @param name
		 *            the device name. Unfortunately on some devices {@link BluetoothDevice#getName()} always returns <code>null</code>, f.e. Sony Xperia Z1 (C6903) with Android 4.3. The name has to
		 *            be parsed manually form the Advertisement packet.
		 */
		public void onDeviceSelected(final BluetoothDevice device, final String name);

		/**
		 * Fired when scanner dialog has been cancelled without selecting a device.
		 */
		public void onDialogCanceled();
	}

	/**
	 * This will make sure that {@link OnDeviceSelectedListener} interface is implemented by activity.
	 */
	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		try {
//			this.mListener = (OnDeviceSelectedListener) activity;
		} catch (final ClassCastException e) {
//			throw new ClassCastException(activity.toString() + " must implement OnDeviceSelectedListener");
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	
	public void setKnownAddress(String addr) {
	    mDevAddress = addr;
	}

	@Override
	public void onDestroyView() {
		stopScan();
		mUartService = null;
		
		super.onDestroyView();
	}

	
	@Override
	public void onStop(){
	    mHandler.removeCallbacksAndMessages(null);
	    super.onStop();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {

	    rootView = inflater.inflate(R.layout.frag_controlview_connect,
	                container, false);
        helpText = (TextView)rootView.findViewById(R.id.helpConnectText);
        startButton = (CircularProgressButton) rootView.findViewById(R.id.startConnectButton);

        mIsInBackground = false;
        stopScan();
        scannerState = SCANNER_STATE_SCAN_STOP;
        drawStopScreen();

	    return rootView;
	}
	
	public void drawStopScreen() {
        startButton.setIdleText(getResources().getString(R.string.start));
        startButton.setIndeterminateProgressMode(true);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsScanning == true)
                    return;
                mIsScanning = true;
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
	                   startScan();
	            }}).start();
	    }

	    /*
	     * This screen ask customer to confirm the device to bind
	     * */
	    public void drawConnectingScreen(BluetoothDevice device)
	    {
	        String detectedStr = getResources().getString(R.string.bleDetected);
//	        String deviceStr = device.getName();
	        String bindStr = getResources().getString(R.string.bindingProcess);
	        String allStr = detectedStr + " "  + bindStr;
	        helpText.setText(allStr);

	        helpText.animate().yBy(-10).setDuration(500).setListener(new AnimatorListenerAdapter(){
	            @Override
	            public void onAnimationEnd(Animator animation) {

	                helpText.setText(getResources().getString(R.string.connected));
	                startButton.setProgress(100);

	                mHandler.postDelayed(new Runnable() {

	                    @Override
	                    public void run() {
	                        if (null == mUartService)
	                        {
	                            return;
	                        }
	                        mUartService.connect(detectedDevice.getAddress());
	                    }
	                }, 200);

	            }}).start();
	    }
	   
	/* simply start scan without */
	public void startBackgroundScan() {
	    mIsInBackground = true;
	    stopScan();
	    startScan();
	}

	/**
	 * Scan for 5 seconds and then stop scanning when a BluetoothLE device is found then mLEScanCallback is activated This will perform regular scan for custom BLE Service UUID and then filter out.
	 * using class ScannerServiceParser
	 */
	private void startScan() {

	    if (mBluetoothAdapter == null) {
	        DebugLogger.w("start scan failed! mBluetoothAdapter is null");	        
	        return;
	    }

		// Samsung Note II with Android 4.3 build JSS15J.N7100XXUEMK9 is not filtering by UUID at all. We must parse UUIDs manually
		mBluetoothAdapter.startLeScan(mLEScanCallback);

		mIsScanning = true;
        mUartService = ActivityMainView.mService;

        DebugLogger.w("start scan UartService: " + mUartService);

        scanTimeOutRunable = new Runnable(){
            @Override
            public void run()
            {
                DebugLogger.w("!! scan timeout");
                stopScan();
                if (mIsInBackground == false) {
                    startButton.setIdleText(getResources().getString(R.string.start));
                    startButton.setProgress(0);
                    helpText = (TextView)rootView.findViewById(R.id.helpConnectText);
                    helpText.setText(getResources().getString(R.string.helpBinding));
                }
            }
      };

	  mHandler.postDelayed(scanTimeOutRunable, SCAN_DURATION);
	}

	/**
	 * Stop scan if user tap Cancel button.
	 */
	private void stopScan() {
		if (mIsScanning) {
			mBluetoothAdapter.stopLeScan(mLEScanCallback);
			mIsScanning = false;
		}
	}

    private void onDeviceDiscovered(BluetoothDevice device, int rssi)
    {
        if (mIsConnecting == true)
            return;

        if ((rssi > -50) && (expectedDevName.equals(device.getName())))
        {
            detectedDevice = device;
            mIsConnecting = true;
            stopScan();
            mHandler.removeCallbacks(scanTimeOutRunable);
            if (mIsInBackground) {
                mUartService.connect(detectedDevice.getAddress());
            } else {
                drawConnectingScreen(device);
            }
        }
    }

	/**
	 * Callback for scanned devices class {@link ScannerServiceParser} will be used to filter devices with custom BLE service UUID then the device will be added in a list.
	 */
	private final BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {

		    DebugLogger.w("discovered :" + device.getName() + " -> " + device.getUuids());
		    
		    if (mIsInBackground) {
		        if (mDevAddress.equals(device.getAddress())) {
		            mUartService.connect(mDevAddress);
		            mHandler.removeCallbacksAndMessages(null);
		        }
		        return;
		    }
		    
		    /* if device contains required service short UUID */
		    if (ScannerServiceParser.decodeDeviceAdvData(scanRecord, mUuid, mDiscoverableRequired)) {
		        DebugLogger.w("required device discovered!" + mUuid);
	            getActivity().runOnUiThread(new Runnable()
	            {
	                @Override
	                public void run()
	                {
	                    onDeviceDiscovered(device, rssi);
	                }
	            });

		    }
		}
	};
}
