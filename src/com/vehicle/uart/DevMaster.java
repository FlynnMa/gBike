package com.vehicle.uart;

import com.utility.DebugLogger;
import com.vehicle.uart.UartService.LocalBinder;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class DevMaster extends Service {
    
 // Command ID
    public static final int   CMD_ID_DEVICE_ID = 0;
    public static final int   CMD_ID_DEVICE_NAME = 1;
    public static final int   CMD_ID_FIRMWARE_VERSION = 2;
    public static final int   CMD_ID_MAINBOARD_TEMPERITURE = 3;
    public static final int   CMD_ID_BATTERY_VOLTAGE = 4;
    public static final int   CMD_ID_CHARGE_STATUS = 5;
    public static final int   CMD_ID_SPEED = 6;
    public static final int   CMD_ID_MILE = 7;
    public static final int   CMD_ID_MAX_SPEED = 8;
    public static final int   CMD_ID_LOW_BATTERY = 9;
    public static final int   CMD_ID_SHUTDOWN_BATTERY = 10;
    public static final int   CMD_ID_FULL_BATTERY =   11;
    public static final int   CMD_ID_POWER_ONOFF  =   12; /**< 1 byte power on off status */
    public static final int   CMD_ID_DRIVE_MODE   =   13; /**< 1 byte drive mode */
    public static final int   CMD_ID_CURRENT      =   14; /**< 4 bytes integer current in mA */
    public static final int   CMD_ID_PERIOD_LONG =   15; /**< 4 bytes int32 value in ms
                                                                this defines update rate of long period
                                                                range 1000 ~ 10000 */
    public static final int   CMD_ID_PERIOD_SHORT =   16; /**< 4 bytes int32 value in ms
                                                            this defines update rate of short period
                                                            range 200 ~ 1000 */

    public static final int   CMD_ID_GENERAL_LONG =   17; /**< a combined command, contains 
                                                        attery, temperature, charge status */
    public static final int   CMD_ID_GENERAL_SHORT =   18; /**< a combined command queried in short
                                                         duration, contains speed, current, mile */

    public static final int   CMD_ID_CONNECTED   =   101;

    public static final int   CMD_TYPE_QUERY     =   0;
    public static final int   CMD_TYPE_SET       =   1;
    public static final int   CMD_TYPE_ACK       =   2;

    public static final int   DEVICE_TYPE_BT         =   1;
    public static final int   DEVICE_TYPE_BIKE       =   2;

    int           jniEvent; /* event received from jni*/
    int           jniEventType; /* 0 query, 1 set, 2 ack */

	/* ===========device status =====================*/
    public String        name = "BIC technology";
    public byte[]        version = {0,0,0,0};
    public String        copyRight = "All rights reserved @ BIC technology";
    public int           deviceID;
    public int           mile;
    public int           powerOnOff;
    public int           chargerIn;
    public int           driveMode;
    public int           connection;
    public float         speed;
    public float         maxSpeed;
    public float         voltage;
    public float         maxVoltage;
    public float         minVoltage;
    public float         shutdownVoltage;
    public float         fullVoltage;
    public float         mainboardTemperiture;
    public float         current = 0;
    public boolean       exit = false;
    private static Thread mThread = null;
    private static final long SHORT_PERIOD = 1200; //0.5 seconds
    int periodCount = 0;
    boolean isQueryStarted;
    
    public final static String ACTION_DATA_UPDATED = "devMaster.ACTION_DATA_UPDATED";
    public final static String ACTION_PACKAGE_PUSHED = "devMaster.ACTION_PACKAGE_PUSHED";
    public final static String ACTION_POWER_ON     = "devMaster.ACTION_POWER_ON";
    public final static String EXTRA_DATA = "devMaster.EXTRA_DATA";

    Handler mHandler = new Handler();

    private static final DevMaster elecVehicleInstance = new DevMaster();

    public DevMaster(){

    }

    public static DevMaster getInstance()
    {
    	return elecVehicleInstance;
    }

    private void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        final int[] jniEvt = {jniEvent, jniEventType};

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        intent.putExtra(EXTRA_DATA, jniEvt);

    }
    
    private void broadcastPowerOnOff()
    {
        final Intent intent = new Intent(ACTION_POWER_ON);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        intent.putExtra(EXTRA_DATA, powerOnOff);

    }

   /* write section, data will write to device */
    byte[]       ApkVersion = {0,0,0,0};

    public void onDataReceived(byte[] data){
        onDataRecv(data);
        update();
    }

    public void startProcess() {
        if (null == mThread)
        {
            mThread = new Thread(readEventProcess);
            mThread.start();
        }
    }

    Runnable readEventProcess = new Runnable() {
        @Override
        public void run() {
            
            while(exit == false) {
                jniEvent = -1;
                readEvent();

                broadcastUpdate(ACTION_DATA_UPDATED);

                if (CMD_ID_POWER_ONOFF == jniEvent)
                {
                    broadcastPowerOnOff();
                    if (powerOnOff == 0){
                        isQueryStarted = false;

                        mHandler.removeCallbacks(regularQueryProcess);
                        mHandler.removeCallbacksAndMessages(null);
                    }
                }
            }
        }
    };

    Runnable regularQueryProcess = new Runnable() {
      @Override
      public void run() {

          if(exit == false) {
              if (((periodCount ++) % 4) == 0)
              {
                  query(CMD_ID_GENERAL_LONG, DevMaster.DEVICE_TYPE_BIKE);
              }
              else
              {
                  query(CMD_ID_GENERAL_SHORT, DevMaster.DEVICE_TYPE_BIKE);
              }

              broadcastUpdate(ACTION_PACKAGE_PUSHED);
              mHandler.postDelayed(regularQueryProcess, SHORT_PERIOD);
          }
      }
    };
    
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
        startProcess();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        mThread = null;
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public void queryEx(int cmd, int dev)
    {
        query(cmd, dev);
        broadcastUpdate(ACTION_PACKAGE_PUSHED);
    }
    
    public void setIntEx(int cmd, int dev, int data)
    {
        setInt(cmd, dev, data);
        broadcastUpdate(ACTION_PACKAGE_PUSHED);
    }
    
    public void startQueryLoop(){
        if (isQueryStarted)
            return;

        mHandler.postDelayed(regularQueryProcess, SHORT_PERIOD);
        isQueryStarted = true;

    }

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

    public native void readEvent();
    
    public native void sleepMs();
    
    public native void query(int cmd, int dev);

    public native void setInt(int cmd, int dev, int data);
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
