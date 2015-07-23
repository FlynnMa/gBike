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

    RelativeLayout rLayout;
	int screenWidth;
	int screenHeight;
	int topLeftID;
	int statusBarHeight;
	Button startButton;
	TextView helpText;

    SpannableString msp = null;  

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EVLog.e("onCreate");

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        rLayout = new RelativeLayout(this);
        rLayout.setBackgroundResource(color.holo_blue_dark);
        rLayout.setBackgroundColor(getResources().getColor(R.color.DarkOrange));
		 LayoutParams rlParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	     rLayout.setLayoutParams(rlParam);
        setContentView(rLayout);

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
        
        rLayout.postDelayed(new Runnable() {

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
		
		Button cancelButton = new Button(this);
//		cancelButton.setTextColor(color.white);
		cancelButton.setBackgroundResource(color.holo_red_dark);
//		cancelButton.setText(R.string.cancel);
		cancelButton.setText("cancel");
		cancelButton.setTextSize(screenHeight / 16);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				scanLeDevice(false);
				finish();
			}
		});
        RelativeLayout.LayoutParams  cancelBtnPos = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        cancelBtnPos.setMargins(0, 0, 0, 0);
        cancelBtnPos.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        cancelBtnPos.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rLayout.addView(cancelButton, cancelBtnPos);


        helpText = new TextView(this);
        helpText.setText(R.string.helpBinding);
//        helpText.setText("close your mobile to the device to start binding");
        helpText.setTextSize(screenHeight / 16);
//        helpText.setTextColor(color.black);
        int helpTextID = View.generateViewId();
        helpText.setId(helpTextID);
        
        RelativeLayout.LayoutParams helpPos = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        helpPos.addRule(RelativeLayout.CENTER_HORIZONTAL);
        helpPos.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        helpPos.setMargins(0, 0, 0, screenHeight/16);
        rLayout.addView(helpText, helpPos);

		startButton = new Button(this);
//		startButton.setTextColor(color.white);
		startButton.setBackgroundResource(color.holo_red_dark);
		startButton.setText("start");
		startButton.setTextSize(screenHeight / 16);
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
        RelativeLayout.LayoutParams btnPos = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        btnPos.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btnPos.addRule(RelativeLayout.ABOVE, helpTextID);
        btnPos.setMargins(0, 0, 0, screenHeight/16);
        rLayout.addView(startButton, btnPos);
	}
	
	/*
	 * This screen ask customer to move mobile phone close to device
	 * and get device searched
	 * */
	public void drawSearchScreen()
	{
		int height = startButton.getHeight();
		
	    int startButtonID = View.generateViewId();
	    startButton.setId(startButtonID);
        RelativeLayout.LayoutParams btnPos = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        btnPos.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btnPos.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        btnPos.setMargins(0, 0, 0, screenHeight/32);
        startButton.setLayoutParams(btnPos);

		helpText.setText(R.string.scanning);
		int y = helpText.getTop();
		helpText.animate().y(y - height * 2).setDuration(500).start();
//        RelativeLayout.LayoutParams helpPos = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//        helpPos.addRule(RelativeLayout.CENTER_HORIZONTAL);
//        helpPos.addRule(RelativeLayout.ABOVE, startButtonID);
//        helpPos.setMargins(0, 0, 0, screenHeight/16);
//        helpText.setLayoutParams(helpPos);
	}
	
	/*
	 * This screen ask customer to confirm the device to bind
	 * */
	public void drawBondingScreen()
	{
		
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
    	EVLog.e("device is found:" + device.getName() + "rssi:" + rssi);
    	devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) 
		{
        	EVLog.e("device is found:" + device.getName());
        	deviceList.add(device);
            deviceAdapter.notifyDataSetChanged();
        }
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
