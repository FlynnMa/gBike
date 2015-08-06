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
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
//import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
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
public class FragScanner extends Fragment{
	private final static String PARAM_UUID = "param_uuid";
	private final static String DISCOVERABLE_REQUIRED = "discoverable_required";
	private final static long SCAN_DURATION = 15000;

	private BluetoothAdapter mBluetoothAdapter;
//	private OnDeviceSelectedListener mListener;
	private final Handler mHandler = new Handler();

	private boolean mDiscoverableRequired;
	private UUID mUuid;

	private boolean mIsScanning = false;
	
	private boolean mIsConnecting = false;
	
    private static final String expectedDevName = "ble2Uart";
    
    BluetoothDevice detectedDevice;
    
    private View rootView;
	    
    TextView helpText = null;

    CircularProgressButton startButton;
    
    Runnable scanTimeOutRunable;
    
    UartService mUartService;

	private static final boolean DEVICE_IS_BONDED = true;
	private static final boolean DEVICE_NOT_BONDED = false;
	/* package */static final int NO_RSSI = -1000;

    public FragScanner(){
        
    }
	/**
	 * Static implementation of fragment so that it keeps data when phone orientation is changed For standard BLE Service UUID, we can filter devices using normal android provided command
	 * startScanLe() with required BLE Service UUID For custom BLE Service UUID, we will use class ScannerServiceParser to filter out required device.
	 */
	public static FragScanner getInstance(final Context context, final UUID uuid, final boolean discoverableRequired) {
		final FragScanner fragment = new FragScanner();

		final Bundle args = new Bundle();
		args.putParcelable(PARAM_UUID, new ParcelUuid(uuid));
		args.putBoolean(DISCOVERABLE_REQUIRED, discoverableRequired);
		fragment.setArguments(args);
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

		final Bundle args = getArguments();
		if (args.containsKey(PARAM_UUID)) {
			final ParcelUuid pu = args.getParcelable(PARAM_UUID);
			mUuid = pu.getUuid();
		}
		mDiscoverableRequired = args.getBoolean(DISCOVERABLE_REQUIRED);

		final BluetoothManager manager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = manager.getAdapter();
	}

	@Override
	public void onDestroyView() {
		stopScan();
		super.onDestroyView();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	    rootView = inflater.inflate(R.layout.frag_controlview_connect,
	                container, false);

	        helpText = (TextView)rootView.findViewById(R.id.helpConnectText);
	        startButton = (CircularProgressButton) rootView.findViewById(R.id.startConnectButton);
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

	        return rootView;
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
	        String deviceStr = device.getName();
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
//	                      evDevice.getConnection();
//	                      byte[] pkg = evDevice.getPackage();
//	                      mUartService.writeRXCharacteristic(pkg);
//	                      finish();
	                    }
	                }, 200);

	            }}).start();
	    }

	/**
	 * When dialog is created then set AlertDialog with list and button views.
	 */
 /*
	@NonNull
    @Override
	public AlertDialog onCreateDialog(final Bundle savedInstanceState) {
//		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//		final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_device_selection, null);
//		final ListView listview = (ListView) dialogView.findViewById(android.R.id.list);

//		listview.setEmptyView(dialogView.findViewById(android.R.id.empty));
//		listview.setAdapter(mAdapter = new DeviceListAdapter(getActivity()));

//		builder.setTitle(R.string.scanner_title);
		final AlertDialog dialog = new AlertDialog();//builder.setView(dialogView).create();
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				stopScan();
				dialog.dismiss();
				final ExtendedBluetoothDevice d = (ExtendedBluetoothDevice) mAdapter.getItem(position);
				mListener.onDeviceSelected(d.device, d.name);
			}
		});

//		mScanButton = (Button) dialogView.findViewById(R.id.action_cancel);
		mScanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.action_cancel) {
					if (mIsScanning) {
						dialog.cancel();
					} else {
						startScan();
					}
				}
			}
		});

		addBondedDevices();
		if (savedInstanceState == null)
			startScan();
		return dialog;
	}
*/
	/*
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		mListener.onDialogCanceled();
	}
*/
	/**
	 * Scan for 5 seconds and then stop scanning when a BluetoothLE device is found then mLEScanCallback is activated This will perform regular scan for custom BLE Service UUID and then filter out.
	 * using class ScannerServiceParser
	 */
	private void startScan() {

		// Samsung Note II with Android 4.3 build JSS15J.N7100XXUEMK9 is not filtering by UUID at all. We must parse UUIDs manually
		mBluetoothAdapter.startLeScan(mLEScanCallback);

		mIsScanning = true;
        mUartService = UartService.getInstance();

        scanTimeOutRunable = new Runnable(){
            @Override
            public void run()
            {
                if (mIsScanning) {
                    stopScan();
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
//			mScanButton.setText(R.string.scanner_action_scan);
			mBluetoothAdapter.stopLeScan(mLEScanCallback);
			mIsScanning = false;
		}
	}

	private void addBondedDevices() {
		final Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
		for (BluetoothDevice device : devices) {
//			mAdapter.addBondedDevice(new ExtendedBluetoothDevice(device, device.getName(), NO_RSSI, DEVICE_IS_BONDED));
		}
	}

	/**
	 * if scanned device already in the list then update it otherwise add as a new device
	 */
	private void addScannedDevice(final BluetoothDevice device, final String name, final int rssi, final boolean isBonded) {
		if (getActivity() != null)
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
//					mAdapter.addOrUpdateDevice(new ExtendedBluetoothDevice(device, name, rssi, isBonded));
				}
			});
	}

	/**
	 * if scanned device already in the list then update it otherwise add as a new device.
	 */
	private void updateScannedDevice(final BluetoothDevice device, final int rssi) {
		if (getActivity() != null)
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
//					mAdapter.updateRssiOfBondedDevice(device.getAddress(), rssi);
				}
			});
	}

    private void onDeviceDiscovered(BluetoothDevice device, int rssi)
    {
        if (mIsConnecting == true)
            return;

        if ((rssi > -35) && (expectedDevName.equals(device.getName())))
        {
            detectedDevice = device;
            mIsConnecting = true;
            stopScan();
            mHandler.removeCallbacks(scanTimeOutRunable);
            drawConnectingScreen(device);
        }
    }

	/**
	 * Callback for scanned devices class {@link ScannerServiceParser} will be used to filter devices with custom BLE service UUID then the device will be added in a list.
	 */
	private final BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {

	          getActivity().runOnUiThread(new Runnable()
	            {
	                @Override
	                public void run()
	                {
	                      onDeviceDiscovered(device, rssi);
	                }
	            });
	          /*
		    if (device != null) {
//				updateScannedDevice(device, rssi);
				onDeviceDiscovered(device, rssi);
				try {
					if (ScannerServiceParser.decodeDeviceAdvData(scanRecord, mUuid, mDiscoverableRequired)) {
						// On some devices device.getName() is always null. We have to parse the name manually :(
						// This bug has been found on Sony Xperia Z1 (C6903) with Android 4.3.
						// https://devzone.nordicsemi.com/index.php/cannot-see-device-name-in-sony-z1
//						addScannedDevice(device, ScannerServiceParser.decodeDeviceName(scanRecord), rssi, DEVICE_NOT_BONDED);
//					    onDeviceDiscovered(device, rssi);
					}
				} catch (Exception e) {
//					DebugLogger.e(TAG, "Invalid data in Advertisement packet " + e.toString());
				}
			}
		    */
		}
	};
}
