/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vehicle.uart;




import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;


import com.vehicle.uart.UartService;
import com.vehicle.uart.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity{
	private static final boolean DEMO = false;
	private static final byte[] NULL_ARRAY = new byte[0];
	private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
	public static final String TAG = "ElectronicVehicle";

	// Command Type
	public static final byte CMD_TYPE_QUERY = 1;
	public static final byte CMD_TYPE_SET = 2;
	public static final byte CMD_TYPE_ACK = 3;

	// Command ID
	public static final byte CMD_ID_DEVICE_ID = 0;
	public static final byte CMD_ID_DEVICE_NAME = 1;
	public static final byte CMD_ID_FIRMWARE_VERSION = 2;
	public static final byte CMD_ID_MAINBOARD_TEMPERITURE = 3;
	public static final byte CMD_ID_BATTERY_VOLTAGE = 4;
	public static final byte CMD_ID_CHARGE_STATUS = 5;
	public static final byte CMD_ID_SPEED = 6;
	public static final byte CMD_ID_MILE = 7;
	public static final byte CMD_ID_MAX_SPEED = 8;
	public static final byte CMD_ID_LOW_BATTERY = 9;
	public static final byte CMD_ID_SHUTDOWN_BATTERY = 10;
	public static final byte CMD_ID_FULL_BATTERY = 11;

	TextView mSpeedTxt, mLeftMilesTxt, mDrivedMilesTxt, mTemperatureTxt;
	SpeedView mSpeedView;
	ImageView mBatteryStatusImg, mBatteryNumberImg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private Button btnConnectDisconnect, btnSend;
	private received_package mReceivedPackage;
	private static final byte HEADER_MAGIC[] = {(byte)0xAD, 0x56, 0x78, (byte)0xED};
	private static int[] BatteryNumberArray = 
		{R.drawable.battery_digit_0, R.drawable.battery_digit_1, R.drawable.battery_digit_2, R.drawable.battery_digit_3, R.drawable.battery_digit_4,
		R.drawable.battery_digit_5, R.drawable.battery_digit_6, R.drawable.battery_digit_7, R.drawable.battery_digit_8, R.drawable.battery_digit_9,
		R.drawable.battery_digit_10, R.drawable.battery_digit_11, R.drawable.battery_digit_12, R.drawable.battery_digit_13, R.drawable.battery_digit_14,
		R.drawable.battery_digit_15, R.drawable.battery_digit_16, R.drawable.battery_digit_17, R.drawable.battery_digit_18, R.drawable.battery_digit_19,
		R.drawable.battery_digit_20, R.drawable.battery_digit_blue_21, R.drawable.battery_digit_blue_22, R.drawable.battery_digit_blue_23, R.drawable.battery_digit_blue_24,
		R.drawable.battery_digit_blue_25, R.drawable.battery_digit_blue_26, R.drawable.battery_digit_blue_27, R.drawable.battery_digit_blue_28, R.drawable.battery_digit_blue_29,
		R.drawable.battery_digit_blue_30, R.drawable.battery_digit_blue_31, R.drawable.battery_digit_blue_32, R.drawable.battery_digit_blue_33, R.drawable.battery_digit_blue_34,
		R.drawable.battery_digit_blue_35, R.drawable.battery_digit_blue_36, R.drawable.battery_digit_blue_37, R.drawable.battery_digit_blue_38, R.drawable.battery_digit_blue_39,
		R.drawable.battery_digit_blue_40, R.drawable.battery_digit_blue_41, R.drawable.battery_digit_blue_42, R.drawable.battery_digit_blue_43, R.drawable.battery_digit_blue_44,
		R.drawable.battery_digit_blue_45, R.drawable.battery_digit_blue_46, R.drawable.battery_digit_blue_47, R.drawable.battery_digit_blue_48, R.drawable.battery_digit_blue_49,
		R.drawable.battery_digit_blue_50, R.drawable.battery_digit_blue_51, R.drawable.battery_digit_blue_52, R.drawable.battery_digit_blue_53, R.drawable.battery_digit_blue_54,
		R.drawable.battery_digit_blue_55, R.drawable.battery_digit_blue_56, R.drawable.battery_digit_blue_57, R.drawable.battery_digit_blue_58, R.drawable.battery_digit_blue_59,
		R.drawable.battery_digit_blue_60, R.drawable.battery_digit_blue_61, R.drawable.battery_digit_blue_62, R.drawable.battery_digit_blue_63, R.drawable.battery_digit_blue_64,
		R.drawable.battery_digit_blue_65, R.drawable.battery_digit_blue_66, R.drawable.battery_digit_blue_67, R.drawable.battery_digit_blue_68, R.drawable.battery_digit_blue_69,
		R.drawable.battery_digit_blue_70, R.drawable.battery_digit_blue_71, R.drawable.battery_digit_blue_72, R.drawable.battery_digit_blue_73, R.drawable.battery_digit_blue_74,
		R.drawable.battery_digit_blue_75, R.drawable.battery_digit_blue_76, R.drawable.battery_digit_blue_77, R.drawable.battery_digit_blue_78, R.drawable.battery_digit_blue_79,
		R.drawable.battery_digit_blue_80, R.drawable.battery_digit_blue_81, R.drawable.battery_digit_blue_82, R.drawable.battery_digit_blue_83, R.drawable.battery_digit_blue_84,
		R.drawable.battery_digit_blue_85, R.drawable.battery_digit_blue_86, R.drawable.battery_digit_blue_87, R.drawable.battery_digit_blue_88, R.drawable.battery_digit_blue_89,
		R.drawable.battery_digit_blue_90, R.drawable.battery_digit_blue_91, R.drawable.battery_digit_blue_92, R.drawable.battery_digit_blue_93, R.drawable.battery_digit_blue_94,
		R.drawable.battery_digit_blue_95, R.drawable.battery_digit_blue_96, R.drawable.battery_digit_blue_97, R.drawable.battery_digit_blue_98, R.drawable.battery_digit_blue_99,
		R.drawable.battery_digit_blue_100};

	class received_package
	{
		public boolean mblMatch;
		public byte cmdType;
		public byte cmd;
		public int datalength;
		public byte[] data;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

		LinearLayout myLayout = (LinearLayout) findViewById(R.id.mainlayout);
		myLayout.setBackgroundColor(Color.WHITE);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) 
		{
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }        
        
        btnConnectDisconnect=(Button) findViewById(R.id.btn_connect);
		btnSend=(Button) findViewById(R.id.btn_send);
		
        service_init();

		((ImageView)findViewById(R.id.image_leftmiles)).setImageResource(R.drawable.leftmiles);
		((ImageView)findViewById(R.id.image_passedmiles)).setImageResource(R.drawable.passedmiles);
		((ImageView)findViewById(R.id.image_temperature)).setImageResource(R.drawable.temperature);
		
		mSpeedTxt = (TextView)findViewById(R.id.txt_speed);
		mLeftMilesTxt = (TextView)findViewById(R.id.txt_leftmiles);
		mDrivedMilesTxt = (TextView)findViewById(R.id.txt_drivedmiles);
		mTemperatureTxt = (TextView)findViewById(R.id.txt_temperature);
		mBatteryStatusImg = (ImageView)findViewById(R.id.battery_status);
		mBatteryNumberImg = (ImageView)findViewById(R.id.battery_number);
		mSpeedView = (SpeedView)findViewById(R.id.speedview);
		mReceivedPackage = new received_package();
		
		// TODO: update acccording to received strings
		if (DEMO)
		{
			mSpeedView.setDemo(true);
			
			mSpeedTxt.setText("30.5KM/h");
			updateLeftMilesText((float)68.27);
			updateDrivedMilesText((float)30.12);
			updateTemperatureText(33);
			
			mBatteryStatusImg.setBackgroundResource(R.anim.batterystatus);
			AnimationDrawable anim1 = (AnimationDrawable)mBatteryStatusImg.getBackground();
			anim1.start();

			mBatteryNumberImg.setBackgroundResource(R.anim.batterynumber);
			AnimationDrawable anim = (AnimationDrawable)mBatteryNumberImg.getBackground();
			anim.start();
		}
		else
		{
			mSpeedView.setDemo(false);
			
			// default values
			updateLeftMilesText(0);
			updateDrivedMilesText(0);
			updateTemperatureText(0);
			updateSpeedViewAndText(0);
			updateBatteryStatus(100);
		}
			
		// Handler Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {				
                if (!mBtAdapter.isEnabled()) 
				{
                    Log.e(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else 
				{
                	if (btnConnectDisconnect.getText().equals("connect"))
					{
                		//Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
            			Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
            			startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
        			} 
					else 
					{
        				//Disconnect button pressed
        				if (mDevice!=null)
        				{
        					mService.disconnect();
        				}
        			}
                }
            }
        });

		
		// TODO: test...................................................................................
		btnSend.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
				mService.writeRXCharacteristic(encodePackage(CMD_TYPE_QUERY, CMD_ID_SPEED, NULL_ARRAY));
				Log.e(TAG, "Send CMD_TYPE_QUERY CMD_ID_SPEED");
				
            }
        });
		// TODO: test end.......................................................................
		
		// send message
		/*
            	String message = editText.getText().toString();
            	byte[] value;
				try {
					//send data to service
					value = message.getBytes("UTF-8");
					mService.writeRXCharacteristic(value);
					//Update the log with time stamp
					String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
					Log.e(TAG, "[" + currentDateTimeString + "] RX: " + message);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		*/        
    }
    
    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
        		mService = ((UartService.LocalBinder) rawBinder).getService();
        		Log.e(TAG, "onServiceConnected mService= " + mService);
        		if (!mService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }

        }

        public void onServiceDisconnected(ComponentName classname) {
       ////     mService.disconnect(mDevice);
        		mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        
        //Handler events that received from UART service 
        public void handleMessage(Message msg) {
  
        }
    };
	
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
           //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) 
			{
            	 runOnUiThread(new Runnable() {
                     public void run() {
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.e(TAG, "UART_CONNECT_MSG");
                             btnConnectDisconnect.setText("Disconnect");
							 Log.e(TAG, "[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                             mState = UART_PROFILE_CONNECTED;
                     }
            	 });
            }
           
          //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) 
			{
            	 runOnUiThread(new Runnable() {
                     public void run() {
                    	 	 String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.e(TAG, "UART_DISCONNECT_MSG");
                             btnConnectDisconnect.setText("Connect");
							 Log.e(TAG, "[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();
                            //setUiState();
                         
                     }
                 });
            }
            
          
          //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED))
			{
             	 mService.enableTXNotification();
            }
			
          //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) 
			{
                 final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                 runOnUiThread(new Runnable() 
				 {
                     public void run() 
					 {
                         try 
						 {
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
							mReceivedPackage = decodePackage(txValue);

							Log.e(TAG, "[" + currentDateTimeString + "] Receive blMatch=" + mReceivedPackage.mblMatch);
							if (mReceivedPackage.mblMatch)
							{
								switch (mReceivedPackage.cmdType)
								{
									case CMD_TYPE_ACK:
										{
											Log.e(TAG, "Receive CMD_TYPE_ACK");
										}
										break;

									case CMD_TYPE_QUERY:
										{
											Log.e(TAG, "Receive CMD_TYPE_QUERY");
										}
										break;

									case CMD_TYPE_SET:
										{
											Log.e(TAG, "Receive CMD_TYPE_SET");
										}
										break;

									default:
										break;
								}

								switch (mReceivedPackage.cmd)
								{
									case CMD_ID_DEVICE_ID:
										{
											Log.e(TAG, "Receive CMD_ID_DEVICE_ID");
										}
										break;
										
									case CMD_ID_DEVICE_NAME:
										{
											Log.e(TAG, "Receive CMD_ID_DEVICE_NAME");
										}
										break;
										
									case CMD_ID_FIRMWARE_VERSION:
										{
											Log.e(TAG, "Receive CMD_ID_FIRMWARE_VERSION");
										}
										break;

									case CMD_ID_MAINBOARD_TEMPERITURE:
										{
											Log.e(TAG, "Receive CMD_ID_MAINBOARD_TEMPERITURE");
										}
										break;

									case CMD_ID_BATTERY_VOLTAGE:
										{
											Log.e(TAG, "Receive CMD_ID_BATTERY_VOLTAGE");
										}
										break;

									case CMD_ID_CHARGE_STATUS:
										{
											Log.e(TAG, "Receive CMD_ID_CHARGE_STATUS");
										}
										break;

									case CMD_ID_SPEED:
										{
											Log.e(TAG, "Receive CMD_ID_SPEED");
										}
										break;

									case CMD_ID_MILE:
										{
											Log.e(TAG, "Receive CMD_ID_MILE");
										}
										break;

									case CMD_ID_MAX_SPEED:
										{
											Log.e(TAG, "Receive CMD_ID_MAX_SPEED");
										}
										break;

									case CMD_ID_LOW_BATTERY:
										{
											Log.e(TAG, "Receive CMD_ID_LOW_BATTERY");
										}
										break;

									case CMD_ID_SHUTDOWN_BATTERY:
										{
											Log.e(TAG, "Receive CMD_ID_SHUTDOWN_BATTERY");
										}
										break;

									case CMD_ID_FULL_BATTERY:
										{
											Log.e(TAG, "Receive CMD_ID_FULL_BATTERY");
										}
										break;

									default:
										break;
								}
							}
                         } 
						 catch (Exception e) 
						 {
                             Log.e(TAG, e.toString());
                         }
                     }
                 });
             }
           //*********************//
           
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART))
			{
            	showMessage("Device doesn't support UART. Disconnecting");
            	mService.disconnect();
            }
        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
  
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
    	 super.onDestroy();
        Log.e(TAG, "onDestroy()");
        
        try {
        	LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        } 
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
       
    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.e(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        case REQUEST_SELECT_DEVICE:
        	//When the DeviceListActivity return, with the selected device address
            if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
               
                Log.e(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                mService.connect(deviceAddress);      
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.e(TAG, "BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
            Log.e(TAG, "wrong request code");
            break;
        }
    }

    
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
  
    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("ElectronicVehicle's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.popup_title)
            .setMessage(R.string.popup_message)
            .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
   	                finish();
                }
            })
            .setNegativeButton(R.string.popup_no, null)
            .show();
        }
    }

	private void updateSpeedViewAndText(float nSpeed)
	{
		do
		{
			if (null == mSpeedTxt)
			{
				break;
			}
			
			if (null == mSpeedView)
			{
				break;
			}

			if (nSpeed<0)
			{
				break;
			}

			if (nSpeed>100)
			{
				break;
			}
			
			mSpeedView.setBigDialDegrees((int)nSpeed*2);
			mSpeedTxt.setText(nSpeed + "KM/h");
		} while(false);
	}

	private void updateLeftMilesText(float nLeftMiles)
	{
		if (null != mLeftMilesTxt)
		{
			mLeftMilesTxt.setText(nLeftMiles + "KM");
		}
	}

	private void updateDrivedMilesText(float nDrivedMiles)
	{
		if (null != mDrivedMilesTxt)
		{			mDrivedMilesTxt.setText(nDrivedMiles + "KM");
		}
	}		

	private void updateTemperatureText(int nTemperature)
	{
		if (null != mTemperatureTxt)
		{	
			mTemperatureTxt.setText(nTemperature + "摄氏度");
		}
	}

	private void updateBatteryStatus(int nBatteryNumber)
	{
		if (nBatteryNumber>=0 && nBatteryNumber<=100)
		{
			if (nBatteryNumber>=0 && nBatteryNumber<=10)
			{
				mBatteryStatusImg.setImageResource(R.drawable.battery_status0);
			}
			else if(nBatteryNumber<=20)
			{
				mBatteryStatusImg.setImageResource(R.drawable.battery_status1);
			}
			else if(nBatteryNumber<=40)
			{
				mBatteryStatusImg.setImageResource(R.drawable.battery_status2);
			}
			else if(nBatteryNumber<=60)
			{
				mBatteryStatusImg.setImageResource(R.drawable.battery_status3);
			}
			else if(nBatteryNumber<=80)
			{
				mBatteryStatusImg.setImageResource(R.drawable.battery_status4);
			}
			else if(nBatteryNumber<=100)
			{
				mBatteryStatusImg.setImageResource(R.drawable.battery_status5);
			}
			
			mBatteryNumberImg.setImageResource(BatteryNumberArray[nBatteryNumber]);
		}
	}

	private byte[] encodePackage(byte cmdType, byte cmd, byte[] nData)
	{
		int nPackageHeaderSize = 7;
		byte[] package_header = new byte[nPackageHeaderSize];
		byte[] nCheckSum = new byte[1];
		
		System.arraycopy(HEADER_MAGIC, 0, package_header, 0, HEADER_MAGIC.length);
		package_header[4] = cmdType;
		package_header[5] = (byte) (nData.length + 2);
		package_header[6] = cmd;

		for (int i = 4; i < nPackageHeaderSize; i++)
	    {
	        nCheckSum[0] += package_header[i];
	    }

		for (int j = 0; j < nData.length; j++)
		{
			nCheckSum[0] += nData[j];
		}

		Log.e(TAG, "encodePackage nCheckSum=" + nCheckSum[0]);

		byte[] whole_package = new byte[package_header.length + nData.length + 1];
		System.arraycopy(package_header, 0, whole_package, 0, package_header.length);
		System.arraycopy(nData, 0, whole_package, package_header.length, nData.length);
		System.arraycopy(nCheckSum, 0, whole_package, package_header.length + nData.length, nCheckSum.length);

		for (int y = 0; y < whole_package.length; y++)
		{
			Log.e(TAG, "encodePackage nData[" + y + "]=" + whole_package[y]);
		}
		
		return whole_package;
	}

	private received_package decodePackage(byte[] nData)
	{
		byte checksum = 0;
		received_package receivepkg = new received_package();
		receivepkg.mblMatch = false;

		Log.e(TAG, "decodePackage nData.length=" + nData.length);

		for (int y = 0; y < nData.length; y++)
		{
			Log.e(TAG, "decodePackage nData[" + y + "]=" + nData[y]);
		}
		
		if (nData.length >= 8) // minimum package size is at least 8 elements
		{
			for (int i = 0; i < nData.length; i++)
			{
				if (!receivepkg.mblMatch)
				{
					if (HEADER_MAGIC[0] == nData[i])
					{
						if (HEADER_MAGIC[1] == nData[i+1]
							&& HEADER_MAGIC[2] == nData[i+2]
							&& HEADER_MAGIC[3] == nData[i+3])
						{
							int datalength = nData[i+5] - 2;

							for (int j = i+4; j < datalength; j++)
							{
								checksum += nData[j];
							}

							if (checksum == nData[datalength-1])
							{
								receivepkg.mblMatch = true;
								receivepkg.cmdType = nData[i+4];
								receivepkg.datalength = datalength;
								receivepkg.cmd = nData[i+6]; 
								receivepkg.data = new byte[receivepkg.datalength];
								System.arraycopy(nData, i+7, receivepkg.data, 0, receivepkg.datalength);
							}
						}
					}
				}
			}
		}

		return receivepkg;
	}
}
