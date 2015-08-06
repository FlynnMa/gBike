package com.vehicle.uart;

import com.vehicle.uart.UartService.LocalBinder;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class DevMaster extends Service{
	/* ===========device status =====================*/
    String        name = "BIC technology";
    byte[]        version = {0,0,0,0};
    String        copyRight = "All rights reserved @ BIC technology";
    int           deviceID;
    int           mile;
    public int    powerOnOff;
    int           chargerIn;
    int           driveMode;
    int           connection;
    float         speed;
    float         maxSpeed;
    float         voltage;
    float         maxVoltage;
    float         minVoltage;
    float         shutdownVoltage;
    float         fullVoltage;
    float         mainboardTemperiture;
    
    public final static String ACTION_DATA_UPDATED = "devMaster.ACTION_DATA_UPDATED";
    
    private static final DevMaster elecVehicleInstance = new DevMaster();
    
    public static DevMaster getInstance()
    {
    	return elecVehicleInstance;
    }

    private void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

   /* write section, data will write to device */
    byte[]       ApkVersion = {0,0,0,0};
    
    public void onDataReceived(byte[] data){
        onDataRecv(data);
        update();
        
        broadcastUpdate(ACTION_DATA_UPDATED);
    }
    

    public class LocalBinder extends Binder
    {
        public DevMaster getService()
        {
            return DevMaster.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /*!
     * This function process received data
     *
     * @param[i] data Array of data in bytes
     * 
     * @return none
     * */
    public native void onDataRecv(byte[] recvData);
    
    /*!
     * This function returns package needs to be send to remote device
     * 
     * @param none
     * 
     * @return byte array to be sent
     * */
    public native byte[] getPackage();
    
    /*!
     * This function updates device status
     * 
     * @param none
     * 
     * @return none
     * */
    public native void update();
    
    /*!
     * This function generates package for query speed
     * 
     * @param none
     * 
     * @return none
     * */
    public native void querySpeed();
    
    /*!
     * This function generates package for query maximum speed
     * 
     * @param none
     * 
     * @return none
     * */
    public native void queryMaxSpeed();
    
    /*!
     * This function generates package for query voltage
     * 
     * @param none
     * 
     * @return none
     * */
    public native void queryVoltage();
    
    /*!
     * This function generates package for query voltage
     * 
     * @param none
     * 
     * @return none
     * */
    public native void queryLowVoltage();
    
    /*!
     * This function generates package for query shutdown voltage
     * 
     * @param none
     * 
     * @return none
     * */
    public native void queryShutDownVoltage();
    
    /*!
     * This function generates package for query version
     * 
     * @param none
     * 
     * @return none
     * */
    public native void queryVersion();
    
    /*!
     * This function generates package for query version
     * 
     * @param none
     * 
     * @return none
     * */
    public native void queryDeviceID();
    
    /*!
     * This function generates package for query version
     * 
     * @param none
     * 
     * @return none
     * */
    public native void queryBoardTemperature();
    
    /*!
     * This function generates package for query version
     * 
     * @param none
     * 
     * @return none
     * */
    public native void queryMile();
    
    /*!
     * This function generates package for query version
     * 
     * @param none
     * 
     * @return none
     * */
    public native void queryPowerOnOff();
    
    /*!
     * This function generates package for query version
     * 
     * @param none
     * 
     * @return none
     * */
    public native void setPowerOnOff(int onOff);
    
    /*!
     * This function generates package for query version
     * 
     * @param none
     * 
     * @return none
     * */
    public native void queryDeviceMode();
    
    /*!
     * This function generates package for query version
     * 
     * @param none
     * 
     * @return none
     * */
    public native void setDeviceMode(int devMode);
    
    /*!
     * This function generates package for query version
     * 
     * @param none
     * 
     * @return none
     * */
    public native void queryChargeStatus();
    
    /*!
     * This function generates package for query version
     * 
     * @param none
     * 
     * @return none
     * */
    public native void setConnection(int connection);
    
    /*!
     * This function generates package for query version
     * 
     * @param none
     * 
     * @return none
     * */
    public native void getConnection();
    
   static {
	   System.loadLibrary("native-ev-jni");
   }
    
};
