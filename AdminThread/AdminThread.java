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

/**
 * @author Marcel Kramer
 * @serial 0.1
 */
public class AdminThread implements ChatListener, ServerListener, Runnable {
	
	public final int PORT;
	public final int BIN_LENGTH;
	
	protected String pw;
	
	protected Server server;
	protected String serverPw;
	protected int serverPort;
	
	protected int maxWaitTime;
	
	protected boolean online;
	protected boolean adminOnline;
	protected boolean serverOnline;
	protected boolean msgRecived;
	
	protected ChatProtokoll cP;
	public JAR_Configuration con;
	
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
	
	public void connected(ChatProtokoll cP) {}
	
	public void logedIn(ChatProtokoll cP) {
		adminOnline=true;
		if (serverOnline) {
			cP.send(AdminConstants.SERVER_IS_ON + server.PORT + AdminConstants.TRENNZEICHEN + server.LOG_IN_PW + ")");
		}
		else {
			cP.send(AdminConstants.SERVER_IS_OFF);
		}
		new Thread(this).start();
	}
		
	public void disconnected(ChatProtokoll cP) {	
		if (online)
			startChatProtokoll();
	}
	
	public void failedToConnect(Exception e, ChatProtokoll cP) {
		e.printStackTrace();
		if (online)
			startChatProtokoll();
	}
	
	public void wrongPassword(ChatProtokoll cP) {
		if (online)
			startChatProtokoll();
	}
	
	public void startChatProtokoll() {
		if (!cP.areKeysReady()) {
			BigInteger[] keys = RSA.erstellen(BIN_LENGTH);
			cP.setKeys(keys[0], keys[1], keys[2]);
		}
		cP.waitAsHost(PORT, pw, 30000);
	}
	
	public void recive(String msg, ChatProtokoll cP) {
		if (msg.startsWith(AdminConstants.RESPONSE)) {
			msgRecived = true;
		}
		else if (msg.startsWith(AdminConstants.START_SERVER)) {
			if (! serverOnline) {
				msg = msg.substring(AdminConstants.START_SERVER.length(), msg.length()-1);
				String[] param = msg.split(AdminConstants.TRENNZEICHEN);
				try {
					int port = Integer.parseInt(param[1]);
					startServer(param[0], port, param[2]);
				}
				catch (Exception e) {
					cP.send(AdminConstants.ERROR + AdminConstants.E_BAD_PARAMETER);
					cP.send(AdminConstants.SERVER_IS_OFF);
				}
			}
		}
		else if (msg.startsWith(AdminConstants.STOP_SERVER)) {
			if (serverOnline) {
				stopServer();
				cP.send(AdminConstants.SERVER_IS_OFF);
			}
		}
		else if (msg.startsWith(AdminConstants.UP_USER)) {
			updateUser();
		}
		else if (msg.startsWith(AdminConstants.KILL)) { 
			//TODO: propper ip locking
			if (serverOnline) {
				msg = msg.substring(AdminConstants.KILL.length(), msg.length()-1);
				server.lockIP(msg.split("|")[0]);
			}
		}
	}
	
	protected void startServer(String name, int port, String pw) {
    	if (pw != null && pw != "" && pw != null && pw != "" && port >= 1024 && port <=65536) {	
			if (port == PORT) {
				cP.send(AdminConstants.ERROR + AdminConstants.E_PORT_IN_USE);
				cP.send(AdminConstants.SERVER_IS_OFF);
				return;
			}
			try {
				ServerSocket serSock = new ServerSocket(port);
				serSock.close();
			}
			catch (IOException e) {
				cP.send(AdminConstants.ERROR + AdminConstants.E_PORT_IN_USE);
				cP.send(AdminConstants.SERVER_IS_OFF);
				return;
			}
			server = new Server(pw, port, this, name);
	    	serverOnline = true;
			cP.send(AdminConstants.SERVER_IS_ON + pw + AdminConstants.TRENNZEICHEN + port +")");
    	}
		else {
			cP.send(AdminConstants.ERROR + AdminConstants.E_BAD_PARAMETER);
			cP.send(AdminConstants.SERVER_IS_OFF);
		}
    }
	
	protected void stopServer() {
		server.shutdown();
		serverOnline = false;
		server = null;
		cP.send(AdminConstants.SERVER_IS_OFF);
	}
	
	public void updateUser() {
		if (serverOnline && adminOnline) {
			String msg = AdminConstants.UP_USER;
			for (ChatProtokoll cP: server.clients) {
				msg += cP.getName() + ("|".equals(AdminConstants.TRENNZEICHEN) ? "-" : "|") + cP.getIP() + AdminConstants.TRENNZEICHEN;
			}
			cP.send(msg.substring(msg.length()-1)+")");
		}
	}
	
	public void handleError(Exception e) {
		
	}
	
	public void run() {
		try {
			Thread.sleep(15000);
		}
		catch (InterruptedException e) {}
		while (adminOnline) {
			int t = 0;
			msgRecived = false;
			cP.send(AdminConstants.TEST);
			while (t < maxWaitTime && !msgRecived) {
				t++;
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {}
			}
			if (t >= maxWaitTime) {
				adminOnline = false;
				cP.disconnect();
			}
			else {
				try {
					Thread.sleep(15000);
				}
				catch (InterruptedException e) {}
			}
		}
	}

}
