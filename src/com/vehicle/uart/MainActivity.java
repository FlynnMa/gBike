/*
 * Copyright (C) 2015 Daniel.Liu Tel:13818674825
 */

package com.vehicle.uart;

import java.text.DateFormat;
import java.util.Date;
import com.vehicle.uart.UartService;
import com.vehicle.uart.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import com.vehicle.uart.CarouselContainer;
import com.vehicle.uart.CarouselPagerAdapter;
import com.vehicle.uart.DevMaster;

public class MainActivity extends FragmentActivity
{
	/**
	* First tab index
	*/
    private static final int FIRST_TAB = CarouselContainer.TAB_INDEX_FIRST;

   /**
	* Second tab index
	*/
    private static final int SECOND_TAB = CarouselContainer.TAB_INDEX_SECOND;

	private static final byte[] NULL_ARRAY = new byte[0];
	private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;

	TextView mSpeedTxt, mLeftMilesTxt, mDrivedMilesTxt, mTemperatureTxt;
	SpeedView mSpeedView;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private Button btnConnectDisconnect, btnSend;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Set the layout
        setContentView(R.layout.carousel_container);

        // Resources
        final Resources res = getResources();

        // Initialize the header
        final CarouselContainer carousel = (CarouselContainer) findViewById(R.id.carousel_header);
        // Indicates that the carousel should only show a fraction of the
        // secondary tab
        carousel.setUsesDualTabs(true);
        // Add some text to the labels
       	carousel.setLabel(FIRST_TAB, this.getString(R.string.disconnected));
        carousel.setLabel(SECOND_TAB, this.getString(R.string.disconnected));

		// TODO: create a circle widget and update to this widget
        // Add some images to the tabs
        //carousel.setImageDrawable(FIRST_TAB, res.getDrawable(R.drawable.temp1));
        carousel.setImageDrawable(SECOND_TAB, res.getDrawable(R.drawable.temp2));

        // The Bundle for the color fragment
        final Bundle blue = new Bundle();
        blue.putInt("color", Color.parseColor("#ff33b5e5"));

        // Initialize the pager adatper
        final PagerAdapter pagerAdapter = new PagerAdapter(this);
        pagerAdapter.add(DummyListFragment.class, new Bundle());
		pagerAdapter.add(DummyListFragment.class, new Bundle());
        //pagerAdapter.add(ColorFragment.class, blue);

        // Initialize the pager
        final ViewPager carouselPager = (ViewPager) findViewById(R.id.carousel_pager);
        // This is used to communicate between the pager and header
        carouselPager.setOnPageChangeListener(new CarouselPagerAdapter(carouselPager, carousel));
        carouselPager.setAdapter(pagerAdapter);
		
		service_init();
		
		if (!Feature.blSimulatorMode)
		{
	        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
	        if (mBtAdapter == null) 
			{
				EVLog.e("Bluetooth is not available");
	            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
	            finish();
	            return;
	        }        
		}
		
		/*
        super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        DevMaster dev = new DevMaster();
        dev.update();
        
		LinearLayout myLayout = (LinearLayout) findViewById(R.id.mainlayout);
		myLayout.setBackgroundColor(Color.WHITE);
        
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
		mSpeedView = (SpeedView)findViewById(R.id.speedview);
		
		// TODO: update acccording to received strings
		// default values
		updateLeftMilesText(0);
		updateDrivedMilesText(0);
		updateTemperatureText(0);
		updateSpeedViewAndText(0);
			
		// Handler Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {				
            	if (!Feature.blSimulatorMode)
            	{
	                if (!mBtAdapter.isEnabled()) 
					{
						EVLog.e("onClick - BT not enabled yet");
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
            }
        });

		// TODO: test...................................................................................
		btnSend.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
				EVLog.e("Send CMD_TYPE_QUERY CMD_ID_SPEED");
				
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
					EVLog.e("[" + currentDateTimeString + "] RX: " + message);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		*/        
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() 
    {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) 
		{
			if (!Feature.blSimulatorMode)
			{
        		mService = ((UartService.LocalBinder) rawBinder).getService();
				EVLog.e("onServiceConnected mService= " + mService);
        		if (!mService.initialize()) {
					EVLog.e("Unable to initialize Bluetooth");
                    finish();
                }
			}
        }

        public void onServiceDisconnected(ComponentName classname) 
		{
      		// mService.disconnect(mDevice);
       		mService = null;
        }
    };
	
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() 
	{
        public void onReceive(Context context, Intent intent)
		{
            String action = intent.getAction();

            final Intent mIntent = intent;

            if (action.equals(UartService.ACTION_GATT_CONNECTED)) 
			{
            	 runOnUiThread(new Runnable() 
				 {
                     public void run() 
					 {
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
							 EVLog.e("UART_CONNECT_MSG");
                             //btnConnectDisconnect.setText("Disconnect");
							 EVLog.e("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                             mState = UART_PROFILE_CONNECTED;
                     }
            	 });
            }
           
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) 
			{
            	 runOnUiThread(new Runnable() 
				 {
                     public void run() 
					 {
                    	 	 String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
							 EVLog.e("UART_DISCONNECT_MSG");
                             //btnConnectDisconnect.setText("Connect");
							 EVLog.e("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();
                            //setUiState();
                         
                     }
                 });
            }
            
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED))
			{
             	 mService.enableTXNotification();
            }

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
							//mReceivedPackage = decodePackage(txValue);

                         } 
						 catch (Exception e) 
						 {
							 EVLog.e(e.toString());
                         }
                     }
                 });
             }
           
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART))
			{
            	showMessage("Device doesn't support UART. Disconnecting");
            	mService.disconnect();
            }
        }
    };

    private void service_init()
	{
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
  
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
	
    private static IntentFilter makeGattUpdateIntentFilter() 
	{
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public void onDestroy() 
    {
    	super.onDestroy();
		EVLog.e("onDestroy()");

		if (!Feature.blSimulatorMode)
		{
	        try 
			{
	        	LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
	        } 
			catch (Exception ignore) 
			{
				EVLog.e(ignore.toString());
	        } 
			
	        unbindService(mServiceConnection);
	        mService.stopSelf();
	        mService= null;
		}
    }

    @Override
    public void onResume() 
    {
        super.onResume();
		EVLog.e("onResume");
		if (!Feature.blSimulatorMode)
		{
	        if (!mBtAdapter.isEnabled()) 
			{
				EVLog.e("onResume - BT not enabled yet");
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	        }
		}
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        switch (requestCode) 
		{
        case REQUEST_SELECT_DEVICE:
        	//When the DeviceListActivity return, with the selected device address
            if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
               
				EVLog.e("... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                mService.connect(deviceAddress);      
            }
            break;
			
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

            } else {
                // User did not enable Bluetooth or an error occurred
				EVLog.e("BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
			
        default:
			EVLog.e("wrong request code");
            break;
        }
    }
    
    private void showMessage(String msg) 
	{
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() 
    {
        if (mState == UART_PROFILE_CONNECTED) 
		{
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("ElectronicVehicle's running in background.\n             Disconnect to exit");
        }
        else 
		{
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.popup_title)
            .setMessage(R.string.popup_message)
            .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
   	                finish();
                }
            })
            .setNegativeButton(R.string.popup_no, null)
            .show();
        }
    }

	/*
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
			mTemperatureTxt.setText(nTemperature + "");
		}
	}
	*/
}
