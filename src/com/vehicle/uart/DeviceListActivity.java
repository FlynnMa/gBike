/*
 * Copyright (C) 2015 Daniel.Liu Tel:13818674825
 */

package com.vehicle.uart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.color;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.vehicle.uart.R;
import android.text.style.TypefaceSpan;
import android.text.*;

public class DeviceListActivity extends Activity
{
    private BluetoothAdapter mBluetoothAdapter;

   // private BluetoothAdapter mBtAdapter;
    private TextView mEmptyList;

    List<BluetoothDevice> deviceList;
    private DeviceAdapter deviceAdapter;
    Map<String, Integer> devRssiValues;
    private static final long SCAN_PERIOD = 100000; //100 seconds
    private Handler mHandler;
    private boolean mScanning;
    BluetoothDevice detectedDevice;
    UartService     mUartService;

    RelativeLayout rLayout;
	int screenWidth;
	int screenHeight;
	int topLeftID;
	int statusBarHeight;
	Button cancelButton;
	CircularProgressButton startButton;
	TextView helpText;

	boolean isConnecting = false;

    SpannableString msp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EVLog.e("onCreate");

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mUartService = UartService.getInstance();
        setContentView(R.layout.device_list);

        mHandler = new Handler();
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

        mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
		        drawHelpScreen();
			}
		}, 100);

    }

	/*
	 * This screen shows help information to tell customer how to bind the device
	 * */
	public void drawHelpScreen()
	{
//		rLayout.removeAllViews();

        helpText = (TextView)findViewById(R.id.helpBindingText);
        helpText.setText(R.string.helpBinding);
//        helpText.setText("close your mobile to the device to start binding");
//        helpText.setTextSize(screenHeight / 16);

		startButton = (CircularProgressButton) findViewById(R.id.startConnectButton);
		startButton.setIndeterminateProgressMode(true);
		startButton.setProgress(0);
		startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	startButton.setProgress(50);
            	helpText.animate().alpha(0).setDuration(500).setListener(new AnimatorListenerAdapter(){
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    	drawSearchScreen();
                    }}).start();
            }
        });
	}

	/*
	 * This screen ask customer to move mobile phone close to device
	 * and get device searched
	 * */
	public void drawSearchScreen()
	{
		helpText.setText(R.string.scanning);
		helpText.animate().alpha(1).setDuration(500).setListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animation) {
            	scanLeDevice(true);
            }}).start();
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
        		        finish();
        			}
        		}, 200);
                
            }}).start();
	}

    public int getStatusBarHeight() {
    	Rect r = new Rect();
    	Window w = getWindow();
    	w.getDecorView().getWindowVisibleDisplayFrame(r);
    	return r.top;
    }

    public int getTitleBarHeight() {
    	int viewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
    	return (viewTop - getStatusBarHeight());
    }
    
    public void getScreenSize()
    {
        Display display = getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	display.getSize(size);
    	screenWidth = size.x;
    	screenHeight = size.y;

    	DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int nowWidth = dm.widthPixels;
        int nowHeigth = dm.heightPixels;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int sHeight = metrics.heightPixels;
        int sWidth = metrics.widthPixels;
        int dens=dm.densityDpi;
        double wi=(double)sWidth/(double)dens;
        double hi=(double)sHeight/(double)dens;
        double x = Math.pow(wi,2);
        double y = Math.pow(hi,2);
        double screenInches = Math.sqrt(x+y);

        screenWidth = screenWidth / (int)dm.density;
        screenHeight = screenHeight / (int)dm.density;
    }

    private void scanLeDevice(final boolean enable) {
//        final Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        if (enable) {
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

    private void onDeviceDiscovered(BluetoothDevice device, int rssi)
    {
    	if (isConnecting == true)
    		return;
    	String expectedDevName = new String("ble2Uart");

    	if ((rssi > -40) && (expectedDevName.equals(device.getName())))
    	{
    		detectedDevice = device;
        	isConnecting = true;
    		scanLeDevice(false);
    		drawBondingScreen(device);
    	}
    }

    private void addDevice(BluetoothDevice device, int rssi)
	{
    	onDeviceDiscovered(device, rssi);
    	/*
        boolean deviceFound = false;

        for (BluetoothDevice listDev : deviceList)
		{
            if (listDev.getAddress().equals(device.getAddress()))
			{
                deviceFound = true;
                break;
            }
        }
    	EVLog.e("device is found:" + device.getName() + "rssi:" + rssi);
    	devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound)
		{
        	EVLog.e("device is found:" + device.getName());
        	deviceList.add(device);
            deviceAdapter.notifyDataSetChanged();
        } */
    }

    @Override
    public void onStart()
    {
        super.onStart();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener()
	{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

            Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());

            Intent result = new Intent();
            result.putExtras(b);
            setResult(Activity.RESULT_OK, result);
            finish();
        }
    };

    protected void onPause()
	{
        super.onPause();
        scanLeDevice(false);
    }

    class DeviceAdapter extends BaseAdapter
	{
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BluetoothDevice> devices)
		{
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount()
        {
            return devices.size();
        }

        @Override
        public Object getItem(int position)
        {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewGroup vg;

            if (convertView != null)
			{
                vg = (ViewGroup) convertView;
            }
			else
			{
                vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
            }

            BluetoothDevice device = devices.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));
            final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);
            final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);

            tvrssi.setVisibility(View.VISIBLE);

			// RSSI
			/*
            byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
            if (rssival != 0) {
                tvrssi.setText("Rssi = " + String.valueOf(rssival));
            }
            */

            tvname.setText(device.getName());
            tvadd.setText(device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED)
			{
				EVLog.e("device::"+device.getName());
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setTextColor(Color.GRAY);
                tvpaired.setVisibility(View.VISIBLE);
                tvpaired.setText(R.string.paired);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);

            }
			else
			{
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setVisibility(View.GONE);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);
            }

            return vg;
        }
    }

    private void showMessage(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
