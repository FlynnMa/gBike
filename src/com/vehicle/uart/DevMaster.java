package com.vehicle.uart;

public class DevMaster {
	/* ===========device status =====================*/
    String        name = "BIC technology";
    byte[]        version = {0,0,0,0};
    String        copyRight = "All rights reserved @ BIC technology";
    int         deviceID;
    int         mile;
    int         powerOnOff;
    int         chargerIn;
    int         driveMode;
    int         connection;
    float       speed;
    float       maxSpeed;
    float       voltage;
    float       maxVoltage;
    float       minVoltage;
    float       shutdownVoltage;
    float       fullVoltage;
    float       mainboardTemperiture;

   /* write section, data will write to device */
    byte[]       ApkVersion = {0,0,0,0};
    
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
    public native void setPowerOnOff();
    
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
    public native void setDeviceMode();
    
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
    public native void setConnection();
    
    /*!
     * This function generates package for query version
     * 
     * @param none
     * 
     * @return none
     * */
    public native void getConnection();
    
   static {
	   System.loadLibrary("native-ev-jni"); // myjni.dll (Windows) or libmyjni.so (Unixes)
   }
    
};
