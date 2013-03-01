package AdminThread;

import java.math.BigInteger;
import java.util.Vector;
import java.io.IOException;
import java.net.ServerSocket;

import ChatProtokoll.*;
import Admin.AdminConstants;
import Server.*;
import Verschluesselung.RSA;
import Configuration.JAR_Configuration;

public class Updater implements Runnable {
	
	public AdminThread(String pw, int port) {
		con = new JAR_Configuration("Server.ini", "=", "ConfigurationFiles");
		
		if (pw != null && pw != "")
			this.pw = pw;
		else {
			this.pw = con.get("password");
		}
		
		if (port > 1023 && port < 65536)
			this.PORT = port;
		else {
			this.PORT = con.getInt("port", 1024, 65535, 2001 );
		}
		
		this.BIN_LENGTH = con.getInt("BIN_LENGTH", 100, 25000, 1024);
		int randCount = con.getInt("RAND_COUNT", 2, 100 );
		
		maxWaitTime = con.getInt("ADMIN_TIMEOUT", 5, 90, 15 );
		
		cP = new ChatProtokoll(this, randCount, "AdminThread");
		
		online = true;
		adminOnline = false;
		serverOnline = false;
		
		startChatProtokoll();
	}

}
