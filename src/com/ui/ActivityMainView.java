package com.ui;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import com.uart.ScannerServiceParser;
import com.utility.DebugLogger;
import com.vehicle.uart.DevMaster;
import com.vehicle.uart.Feature;
import com.vehicle.uart.R;

import com.vehicle.uart.UartService;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class ActivityMainView extends FragmentActivity {

    public final static UUID UART_SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");

    boolean mTwoPane = false;

//    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_CONNECTED = 20;

    private static final int UART_PROFILE_DISCONNECTED = 21;
    
    private static int mState = UART_PROFILE_DISCONNECTED;

    public static UartService mService = null;

    private BluetoothDevice mDevice = null;

    public static BluetoothAdapter mBtAdapter = null;

	public static DevMaster evDevice;
	
	FragScanner scannerView;

	FragPower powerView;

    SharedPreferences userInfo;

    String devAddress;

    final private Handler mHandler = new Handler();
    
    public static Bundle savedInstance;

    FragLoading loadingView = null;
    
    boolean exitProgram;
    
    boolean isLoading = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mainview);

		savedInstance = savedInstanceState;
		exitProgram = false;
		/*
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }*/
	
		userInfo = getSharedPreferences("userInfo", 0);
		String devName = userInfo.getString("deviceName", null);
		devAddress = userInfo.getString("deviceAddr", null);
		if (null != devName) {
		    DebugLogger.d("load saved device : [" + devName + "]");
		}
        service_init();

		evDevice = DevMaster.getInstance();
		evDevice.init();
		evDevice.startProcess();
        evDevice.update();

        loadingView = new FragLoading();
        getSupportFragmentManager().beginTransaction()
            .add(R.id.frag_mainview_container, loadingView).commit();
        isLoading = true;
        mHandler.postDelayed(loadingTimeoutRunnable, 5000);

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
	
	Runnable loadingTimeoutRunnable = new Runnable() {

        @Override
        public void run() {
            DebugLogger.w("!!loading timeout! enter_main ");
            enter_main();
        }
    };
	
	public void enter_main()
	{
	    isLoading = false;
        if (null != loadingView)
        {
            getSupportFragmentManager().beginTransaction()
                .remove(loadingView).commit();

            FragInformationViews infoView = new FragInformationViews();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.devinfo_container, infoView).commit();

            FragMilesView  milesView = new FragMilesView();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.milesview, milesView).commit();
        }

        if (mState != UART_PROFILE_CONNECTED) {
            /* get instance with UUID parameter, to be used in future */
            if (scannerView != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.control_container, scannerView).commit();
//                scannerView = FragScanner.getInstance(ActivityMainView.this,
//                        UART_SERVICE_UUID, devAddress, false, false);
            }
        } else {
            powerView = new FragPower();
            getSupportFragmentManager().beginTransaction()
              .replace(R.id.control_container, powerView).commit();

        }
        loadingView = null;

	}
	
    private void service_init()
	{
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        DebugLogger.w("-->service init ");
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

        intentFilter.addAction(DevMaster.ACTION_DATA_UPDATED);
        intentFilter.addAction(DevMaster.ACTION_PACKAGE_PUSHED);
        intentFilter.addAction(DevMaster.ACTION_POWER_ON);


        return intentFilter;
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
                          	DebugLogger.e("ACTION_GATT_CONNECTED");
 							mDevice = mService.getDevice();
 							DebugLogger.d("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                            mState = UART_PROFILE_CONNECTED;

                            /* save device name */
                            userInfo.edit().putString("deviceName", mDevice.getName()).commit();
                            /* save device address */
                            userInfo.edit().putString("deviceAddr", mDevice.getAddress()).commit();

                            /* enter main view immediately */
                            mHandler.removeCallbacks(loadingTimeoutRunnable);
                            enter_main();
                      }
             	 });
             }

             if (action.equals(UartService.ACTION_GATT_DISCONNECTED))
             {
             	 runOnUiThread(new Runnable()
 				 {
                     public void run()
 					 {
                             // I don't know why, but it does enter here on application start,
                             // almost soon after the "connection action"
                             if (exitProgram == false)
                                 return;

                     	 	 String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                     	 	 DebugLogger.d("UART_DISCONNECT_MSG");
                     	 	 DebugLogger.d("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();

                             if (exitProgram) {
                                 System.exit(0);
                                 return;
                             }
/*
                             final FragScanner scanner = FragScanner.getInstance(ActivityMainView.this,
                                                             UART_SERVICE_UUID, null, false, false);
                             */
                             if (scannerView != null)
                                 getSupportFragmentManager().beginTransaction()
                                      .replace(R.id.control_container, scannerView).commit();
                      }
                  });
             }
             /* no need further process */
             else if (exitProgram) {
                 DebugLogger.e("on exitProgram but received :" + action);
                 return;
             }

             if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED))
 			{
                 mHandler.postDelayed(new Runnable() {

                     @Override
                     public void run() {
                             mService.enableTXNotification();
                     }
                 }, 50);
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
                          	mService.send();
                          }
 						 catch (Exception e)
 						 {
 							 DebugLogger.d(e.toString());
                          }
                      }
                  });
              }
             
             if (action.equals(UartService.ACTION_DATA_SENT) || action.equals(DevMaster.ACTION_PACKAGE_PUSHED))
             {
             	/* send next package */
                 if (null != mService)
                     mService.send();
                 else {
                     DebugLogger.e("failed send : mService is null");
                 }
             }

             if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART))
 			{
             	mService.disconnect();
             }

             if (action.equals(DevMaster.ACTION_POWER_ON))
             {
                 int powerOnOff = intent.getIntExtra(DevMaster.EXTRA_DATA, -1);
                 DebugLogger.e("!@^_^@  poweronoff event" + powerOnOff);
                 evDevice.startQueryLoop();
             }
             if (action.equals(DevMaster.ACTION_DATA_UPDATED))
             {
                 final int[] cmd = intent.getIntArrayExtra(DevMaster.EXTRA_DATA);

                 if (null != cmd)
                     onCmd(cmd[0], cmd[1]);
             }

         }
     };

     private void onSetCmd(int cmd)
     {
         switch(cmd)
         {
         case DevMaster.CMD_ID_CONNECTED:
              break;
         }
     }
     
     private void onQueryCmd(int cmd)
     {
         
     }
     
     private void onCmd(int cmd, int cmdType)
     {
         switch(cmdType)
         {
         case DevMaster.CMD_TYPE_SET:
             onSetCmd(cmd);
             break;
             
         case DevMaster.CMD_TYPE_QUERY:
             onQueryCmd(cmd);
             break;
             
         case DevMaster.CMD_TYPE_ACK:
             break;
         }
     }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder rawBinder)
		{
			if (!Feature.blSimulatorMode)
			{
        		mService = ((UartService.LocalBinder) rawBinder).getService();

        		DebugLogger.e("!!onServiceConnected mService = " + mService);
        		if (!mService.initialize())
				{
        			DebugLogger.d("Unable to initialize Bluetooth");
                    finish();
                    return;
                }
        		
                if ((null != devAddress) && isLoading)
                {
                    DebugLogger.w("-->start background scan : " + mService);
                    scannerView = FragScanner.getInstance(ActivityMainView.this,
                            UART_SERVICE_UUID, devAddress, false, true);
//                  mService.connect(devAddress);
                }

			}
        }

        @Override
        public void onServiceDisconnected(ComponentName classname)
		{
            DebugLogger.e("onServiceDisconnected should never comes here ");
            if (exitProgram == true){
                System.exit(0);
                return;
            }
        }
    };
    
    @Override
    public void onStart() {
        DebugLogger.d("onStart");
        exitProgram = false;
        super.onStart();
    }
    
    @Override
    public void onDestroy() {
        if (exitProgram == false) {
            return;
        }

        unbindService(mServiceConnection);
        super.onDestroy();
        
         DebugLogger.d("onDestroy");
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            DebugLogger.e(ignore.toString());
        }
//        unbindService(mServiceConnection);
        mService = null;
        evDevice = null;
    }
    
    @Override
    protected void onStop() {
        DebugLogger.d("onStop");
//        if (exitProgram == false) {
//            return;
//        }
        evDevice.exitProcess();
        mService.stopSelf();
        super.onStop();
    }

    @Override
    protected void onPause() {
        DebugLogger.d("onPause");
        super.onPause();
    }

    @Override
    public void onResume() {
        DebugLogger.d("onResume");
        evDevice.startQueryLoop();
        super.onResume(); 
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        DebugLogger.d("onRestart");
    }
    

    @Override
    public void onBackPressed() {
        exitProgram = true;
        mHandler.removeCallbacks(loadingTimeoutRunnable);
        if (mState == UART_PROFILE_CONNECTED) {
            if (null != mService){
                mService.disconnect();
            }
            else {
                DebugLogger.e("onBackPressed service is null - exit flag: " + exitProgram);
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }
    
}

