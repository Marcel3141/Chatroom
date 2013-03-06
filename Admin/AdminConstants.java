package Admin;

public class AdminConstants  {
	
	//Allgemeine Befehle
	public static final String DISCONNECT = "DISCONNECT()";
	public static final String CONNECTED = "LOGIN_ACCEPTED";
	
	public static final String SERVERUPDATE = "SERVERUPDATE";
	
	//Admin-Server Befehle
	public static final String TEST = "TEST()";
	public static final String RESPONSE = "RESPONSE()";
	
	public static final String STOP_SERVER = "SERVER_OFF()";
	public static final String START_SERVER = "SERVER_ON(";
	
	public static final String SERVER_IS_ON = "SERVER_IS_ON(";
	public static final String SERVER_IS_OFF = "SERVER_IS_OFF()";
	
	public static final String KILL = "KILL(";
	
	public static final String UP_USER = "UPDATE_USER(";
	public static final String UP_LOCKED_IP = "UPDATE_LOCKED_IP()";
	public static final String UP_CONFIG = "UPDATE_CONFIG(";
	
	public static final String SEND_CONFIG = "SEND_CONFIG(";	
	
	public static final String RESTART = "RESTART(";
	public static final String CHANGE_PASSWORD = "SET_PASSWORD(";
	
	

	//Fehlermeldungen
	public static final String ERROR = "ERROR: ";
	public static final String E_BAD_PARAMETER = "BadParameters";
	public static final String E_PORT_IN_USE = "this port is allready occupied";
	public static final String E_UNKNOWN_COMMAND = "WTF???";
	public static final String E_SYNTAX = "syntax is wrong";
	
	//Syntax
	public static final String TRENNZEICHEN = ",";
	public static final String TRENNZEICHEN_2 = "-";
	public static final String ZEILENUMBRUCH = ";";
	
}