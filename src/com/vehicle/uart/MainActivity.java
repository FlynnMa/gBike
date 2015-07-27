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
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Window;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import com.vehicle.uart.CarouselContainer;
import com.vehicle.uart.CarouselPagerAdapter;
import com.vehicle.uart.DevMaster;;

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

	private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;

    private static int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    public static BluetoothAdapter mBtAdapter = null;
	private CarouselContainer mCarousel = null;
	DevMaster evDevice;

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
        mCarousel = (CarouselContainer) findViewById(R.id.carousel_header);
        // Indicates that the carousel should only show a fraction of the
        // secondary tab
        mCarousel.setUsesDualTabs(true);
        // Add some text to the labels
        updateConnectionStatusAndSpeedText();

		// Add some text to the text
		mCarousel.setText(FIRST_TAB, this.getString(R.string.totalmiles));
		mCarousel.setText(SECOND_TAB, this.getString(R.string.batterytext));

		// Add some text to the sign
		mCarousel.setSign(FIRST_TAB, this.getString(R.string.miles));
		mCarousel.setSign(SECOND_TAB, this.getString(R.string.batterysign));

		// TODO: need to replace by real value from driver
		// Add some numbers
		mCarousel.setNumbers(FIRST_TAB, 99);
		mCarousel.setNumbers(SECOND_TAB, 66);
		
        // Initialize the pager adatper
        final PagerAdapter pagerAdapter = new PagerAdapter(this);
        pagerAdapter.add(DummyListFragment.class, new Bundle());
		pagerAdapter.add(DummyListFragment.class, new Bundle());

        // Initialize the pager
        final ViewPager carouselPager = (ViewPager) findViewById(R.id.carousel_pager);
        // This is used to communicate between the pager and header
        carouselPager.setOnPageChangeListener(new CarouselPagerAdapter(carouselPager, mCarousel));
        carouselPager.setAdapter(pagerAdapter);

		service_init();
		
		evDevice = DevMaster.getInstance();
		evDevice.update();
		evDevice.queryPowerOnOff();
		byte[] pkg = evDevice.getPackage();

		if (!Feature.blSimulatorMode)
		{
	        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
	        if (mBtAdapter == null)
			{
				EVLog.e("Bluetooth is not available");
	            Toast.makeText(this, this.getString(R.string.bluetooth_unavailable), Toast.LENGTH_LONG).show();
	            finish();
	            return;
	        }
		}
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
        		if (!mService.initialize())
				{
					EVLog.e("Unable to initialize Bluetooth");
                    finish();
                }
			}
        }

        public void onServiceDisconnected(ComponentName classname)
		{
			mService = null;
        }
    };

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
							EVLog.e("UART_CONNECT_MSG");
							mDevice = UartService.getInstance().getDevice();
							EVLog.e("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                            mState = UART_PROFILE_CONNECTED;
							updateConnectionStatusAndSpeedText();
							 
		        			evDevice.getConnection();
		        			byte[] pkg = evDevice.getPackage();
		        			mService.writeRXCharacteristic(pkg);

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
							 EVLog.e("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();
							 updateConnectionStatusAndSpeedText();
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
//                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                         	evDevice.onDataRecv(rxValue);
                         	evDevice.update();
							//mReceivedPackage = decodePackage(txValue);
							//EVLog.e("[" + currentDateTimeString + "] Receive blMatch=" + mReceivedPackage.mblMatch);
                         }
						 catch (Exception e)
						 {
							 EVLog.e(e.toString());
                         }
                     }
                 });
             }
            
            if (action.equals(UartService.ACTION_DATA_SENT))
            {
            	final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
            	EVLog.e("DATA SENT"+ txValue);
            	
            	byte[] pkg = evDevice.getPackage();
            	if (null != pkg){
            		mService.writeRXCharacteristic(pkg);
            	}
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
        intentFilter.addAction(UartService.ACTION_DATA_SENT);
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
            if (resultCode == Activity.RESULT_OK)
			{
				EVLog.e("BT has turned on");
                Toast.makeText(this, this.getString(R.string.bluetooth_turned_on), Toast.LENGTH_SHORT).show();
            }
			else
			{
                // User did not enable Bluetooth or an error occurred
				EVLog.e("BT not enabled");
                Toast.makeText(this, this.getString(R.string.bluetooth_disabled), Toast.LENGTH_SHORT).show();
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

	public static boolean IsBluetoothConnected()
	{
		EVLog.e("" + mState);
		return UART_PROFILE_CONNECTED == mState;
	}

	// if disconnected, show disconnected text
	// if connected, show speed text
	private void updateConnectionStatusAndSpeedText()
	{
		if (null != mCarousel)
		{
			if (UART_PROFILE_CONNECTED == mState)	// show speed
			{
				// TODO: get speed
				mCarousel.setLabel(FIRST_TAB, this.getString(R.string.connected));
		        mCarousel.setLabel(SECOND_TAB, this.getString(R.string.connected));
			}
			else	
			{
				mCarousel.setLabel(FIRST_TAB, this.getString(R.string.disconnected));
		        mCarousel.setLabel(SECOND_TAB, this.getString(R.string.disconnected));
			}
		}
	}

	private void updateTab_MilesInfo()
	{
		if (null != mCarousel)
		{
			// TODO: get drivedmiles and totalmiles, calculate percentage
			//int percentage = drivedmiles / totalmiles * 100;
			//mCarousel.setNumbers(FIRST_TAB, percentage);
		}
	}

	private void updateTab_BatteryInfo()
	{
		if (null != mCarousel)
		{
			// TODO: calculate battery percentage
			//int percentage
			//mCarousel.setNumbers(SECOND_TAB, percentage);
		}
	}
}
