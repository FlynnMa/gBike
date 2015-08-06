package com.ui;

import java.text.DateFormat;
import java.util.Date;

import com.carousel.CarouselContainer;
import com.utility.DebugLogger;
import com.vehicle.uart.DevMaster;
import com.vehicle.uart.Feature;
import com.vehicle.uart.R;
import com.vehicle.uart.R.id;
import com.vehicle.uart.R.layout;

import com.vehicle.uart.UartService;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;

public class ActivityMainView extends FragmentActivity {

	boolean mTwoPane = false;

//    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    
    private static int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    public static BluetoothAdapter mBtAdapter = null;
	DevMaster evDevice;
	private boolean mIsBTSending = false;
	
	FragPower powerView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mainview);

		// Show the Up button in the action bar.
//		getActionBar().setDisplayHomeAsUpEnabled(true);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//

		service_init();
		evDevice = new DevMaster();
		if (savedInstanceState == null) {
		    if (mState != UART_PROFILE_CONNECTED)
		    {
		        /* get instance with uuid parameter, to be used in future */
		        final FragScanner scanner = FragScanner.getInstance(ActivityMainView.this, null, false);
    			getSupportFragmentManager().beginTransaction()
    				.add(R.id.control_container, scanner).commit();
		    }
		    else
		    {
		        FragControlViewConnectDevice controlView = new FragControlViewConnectDevice();
		        getSupportFragmentManager().beginTransaction()
		            .add(R.id.control_container, controlView).commit();
		    }
			FragInformationViews infoView = new FragInformationViews();
			getSupportFragmentManager().beginTransaction()
			    .add(R.id.devinfo_container, infoView).commit();
			
			FragMilesView  milesView = new FragMilesView();
            getSupportFragmentManager().beginTransaction()
            .replace(R.id.milesview, milesView).commit();
		}
		
		if (findViewById(R.id.frag_detailview_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
//			((fragListFragment) getSupportFragmentManager().findFragmentById(
//					R.id.frag_list)).setActivateOnItemClick(true);
		}
	}
	
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
        intentFilter.addAction(UartService.ACTION_DATA_SENT);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);

        return intentFilter;
    }
    
    public void sendData(){
        if (mIsBTSending == true)
            return;

        byte[] pkg = evDevice.getPackage();
        if (null == pkg)
            return;
        
        mService.writeRXCharacteristic(pkg);
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver()
 	{
         public void onReceive(Context context, Intent intent)
 		{
             String action = intent.getAction();

             if (action.equals(UartService.ACTION_GATT_CONNECTED))
 			{
             	 runOnUiThread(new Runnable()
 				 {
                     public void run()
 					 {
                          	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                          	DebugLogger.d("UART_CONNECT_MSG");
 							mDevice = UartService.getInstance().getDevice();
 							DebugLogger.d("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                            mState = UART_PROFILE_CONNECTED;

 		        			evDevice.getConnection();
 		        			sendData();
 		        			
 		        			powerView = new FragPower();
 		        			getSupportFragmentManager().beginTransaction()
 		                      .replace(R.id.control_container, powerView).commit();
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
                     	 	 DebugLogger.d("UART_DISCONNECT_MSG");
                     	 	 DebugLogger.d("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                              mState = UART_PROFILE_DISCONNECTED;
                              mService.close();
                      }
                  });
             }

             if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED))
 			{
              	 mService.enableTXNotification();
             }

             if (action.equals(UartService.ACTION_DATA_AVAILABLE))
 			{
                  final byte[] rxValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                  runOnUiThread(new Runnable()
 				 {
                      public void run()
 					 {
                          try
 						 {
                          	evDevice.onDataRecv(rxValue);
                          	evDevice.update();
                          	sendData();
                          }
 						 catch (Exception e)
 						 {
 							 DebugLogger.d(e.toString());
                          }
                      }
                  });
              }
             
             if (action.equals(UartService.ACTION_DATA_SENT))
             {
             	final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
             	DebugLogger.d("DATA SENT"+ txValue);
             	
             	/* send next package */
             	sendData();
             }

             if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART))
 			{
             	mService.disconnect();
             }
         }
     };

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder rawBinder)
		{
			if (!Feature.blSimulatorMode)
			{
        		mService = ((UartService.LocalBinder) rawBinder).getService();
        		DebugLogger.d("onServiceConnected mService= " + mService);
        		if (!mService.initialize())
				{
        			DebugLogger.d("Unable to initialize Bluetooth");
                    finish();
                }
			}
        }

        public void onServiceDisconnected(ComponentName classname)
		{
			mService = null;
        }
    };
    
    private final BroadcastReceiver devStatusChangeReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            
            if (action.equals(DevMaster.ACTION_DATA_UPDATED))
            {
                runOnUiThread(new Runnable(){
                   public void run(){
                       if (powerView != null)
                       {
                           powerView.isPowerOn = evDevice.powerOnOff;
                       }
                   } 
                });
            }
        }
    };
}

