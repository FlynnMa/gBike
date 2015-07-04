package com.vehicle.uart;

public class Protocol 
{
	// Command ID
	public static final short CMD_ID_QUERY_DEVICE_ID 				= 0;
	public static final short CMD_ID_QUERY_DEVICE_NAME 				= 1;
	public static final short CMD_ID_QUERY_FIRMWARE_VERSION			= 2;
	public static final short CMD_ID_QUERY_MAINBOARD_TEMPERITURE	= 3;
	public static final short CMD_ID_QUERY_BATTERY_VOLTAGE 			= 4;
	public static final short CMD_ID_QUERY_CHARGE_STATUS 			= 5;
	public static final short CMD_ID_QUERY_SPEED					= 6;
	
	public static final short CMD_ID_REPORT_DEVICE_ID				= 20;
	public static final short CMD_ID_REPORT_DEVICE_NAME     		= 21;
	public static final short CMD_ID_REPORT_FIRMWARE_VERSION		= 22;
	public static final short CMD_ID_REPORT_MAINBOARD_TEMPERITURE	= 23;
	public static final short CMD_ID_REPORT_BATTERY_VOLTAGE			= 24;
	public static final short CMD_ID_REPORT_CHARGE_STATUS     		= 25;
	public static final short CMD_ID_REPORT_CURRENT_SPEED			= 26;
	public static final short CMD_ID_REPORT_BATTERY_RANGE			= 27;
	public static final short CMD_ID_REPORT_MAX_SPEED				= 28;
	
	public static final short CMD_ID_SET_REPORT_DURATION			= 40;
	public static final short CMD_ID_SET_UART_HARDWARE_FLOW     	= 41;
	public static final short CMD_ID_SET_BLUETOOTH_CONNECTION		= 42;
	public static final short CMD_ID_RESET_STATUS					= 43;
	public static final short CMD_ID_SET_BATTERY_INTERVAL			= 44;
	public static final short CMD_ID_SET_SPEED_INTERVAL				= 45;

	public static final short ACK									= 88;

	class event_type
	{
		public static final int EV_EVENT_BLUETOOTH_CONNECTION 	= 0;
		public static final int EV_EVENT_QUERY 					= 1;
		public static final int EV_EVENT_REPLY 					= 2;
		public static final int EV_EVENT_SETTINGS 				= 3;
		public static final int EV_EVENT_MAX 					= 4;
	}

	class error_type
	{
		public static final int SUCCESS 			= 0;
		public static final int ERROR_HW_FAILURE 	= 1;
		public static final int ERROR_UNSUPPORTED 	= 2;
		public static final int ERROR_INVALID_PARAM = 3;
		public static final int ERROR_BAD_STATUS 	= 4;
		public static final int ERROR_CHECKSUM 		= 5;
		public static final int ERROR_OUT_RANGE 	= 6;
		public static final int ERROR_MAX 			= 7;
	}

	class protocol_status
	{
		public static final int PROTOCAL_STATUS_CMD 	= 0;
		public static final int PROTOCAL_STATUS_LENGTH 	= 1;
		public static final int PROTOCAL_STATUS_DATA 	= 2;
		public static final int PROTOCAL_STATUS_ACK 	= 3;
		public static final int PROTOCAL_STATUS_MAX 	= 4;
	}
	
	class protocol_package
	{
		public int cmd_type;
		public int len;
		public int cmd;
		public int data;
		public int checksum;
	}

	public Protocol() 
	{
		
	}
}
