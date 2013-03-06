package AdminThread;

import java.math.BigInteger;
import java.util.Vector;
import java.util.Calendar;
import java.io.IOException;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.ServerSocket;

import ChatProtokoll.*;
import Admin.AdminConstants;
import Server.*;
import Verschluesselung.RSA;
import Configuration.JAR_Configuration;
import Configuration.Configuration;
import Configuration.ReadWriteFile;

/**
 * @author Marcel Kramer
 */
public class AdminThread implements ChatListener, ServerListener, Runnable {
	
	public final int PORT;
	public final int BIN_LENGTH;
	
	protected String pw;
	
	protected Server server;
	protected String serverPw;
	public String serverName;
	protected int serverPort;
	
	protected int maxWaitTime;
	
	protected boolean online;
	protected boolean adminOnline;
	protected boolean serverOnline;
	protected boolean msgRecived;
	
	protected ChatProtokoll cP;
	public JAR_Configuration con;
	
	public final boolean VERBOSE;
	
	public AdminThread(String pw, int port, boolean verbose) {
		this.VERBOSE = verbose;
		
		con = new JAR_Configuration("Server.ini", "=");
		
		if (pw != null && pw != "")
			this.pw = pw;
		else {
			this.pw = con.get("default_password", "1");
		}
		
		if (port > 1023 && port < 65536)
			this.PORT = port;
		else {
			this.PORT = con.getInt("default_port", 1024, 65535, 2001 );
		}
		
		this.BIN_LENGTH = con.getInt("BIN_LENGTH", 100, 25000, 1024);
		startup();
		startChatProtokoll();
		pl("AT started");
	}
	
	public AdminThread(Configuration config, boolean verbose) {
		this.VERBOSE = verbose;
		
		con = new JAR_Configuration("Server.ini", "=");
		
		pw = config.get("password", con.get("default_password", "1"));
		
		PORT = config.getInt("port", 1024, 65535, con.getInt("default_port", 1024, 65535, 2001 ));
		con.change("default_port", PORT+"");
		
		BIN_LENGTH = con.getInt("BIN_LENGTH", 100, 25000, 1024);
		
		startup();
		serverOnline = config.getBool("server_online", false);
		if (serverOnline) {
			try {
				String name = config.get("server_name", "Server");;
				int port = con.getInt("server_port");
				String pw = config.get("server_password", "");
				startServer(name, port, pw);
			}
			catch (IOException e) {
				
			}
		}
		startChatProtokoll();
		pl("AT started");
	}
	
	protected void startup() {
		maxWaitTime = con.getInt("ADMIN_TIMEOUT", 5, 90, 15 );
		
		int randCount = con.getInt("RAND_COUNT", 2, 100 );
		cP = new ChatProtokoll(this, randCount, "AdminThread");
		
		online = true;
		adminOnline = false;
		serverOnline = false;
	}
	
	public void connected(ChatProtokoll cP) {}
	
	public void logedIn(ChatProtokoll cP) {
		adminOnline=true;
		pl("Admin logged in");
		if (serverOnline) {
			cP.send(AdminConstants.SERVER_IS_ON + server.PORT + AdminConstants.TRENNZEICHEN + server.LOG_IN_PW + ")");
		}
		else {
			cP.send(AdminConstants.SERVER_IS_OFF);
		}
		new Thread(this).start();
	}
		
	public void disconnected(ChatProtokoll cP) {
		pl("Admin logged out");
		if (online)
			startChatProtokoll();
	}
	
	public void failedToConnect(Exception e, ChatProtokoll cP) {
		pl("Admin failed to connect");
		pe(e);
		if (online)
			startChatProtokoll();
	}
	
	public void wrongPassword(ChatProtokoll cP) {
		pl(cP.getIP()+" entered wrong Password");
		if (online)
			startChatProtokoll();
	}
	
	public void startChatProtokoll() {
		adminOnline = false;
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
				catch (IOException e) {
					cP.send(AdminConstants.ERROR + AdminConstants.E_PORT_IN_USE);
					cP.send(AdminConstants.SERVER_IS_OFF);
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
			if (serverOnline) {
				msg = msg.substring(AdminConstants.KILL.length(), msg.length()-1);
				server.lockIP(msg.split(AdminConstants.TRENNZEICHEN)[0]);
			}
		}
	}
	
	protected void startServer(String name, int port, String pw) throws IOException {
    	if (pw != null && pw != "" && pw != null && pw != "" && port >= 1024 && port <=65536) {	
			if (port == PORT) {
				throw new IOException();
			}
			ServerSocket serSock = new ServerSocket(port);
			serSock.close();
			
			serverPw = pw;
			serverName = name;
			serverPort = port;
			
			server = new Server(pw, port, this, name);
			
	    	serverOnline = true;
			
			if (adminOnline)
				cP.send(AdminConstants.SERVER_IS_ON + pw + AdminConstants.TRENNZEICHEN + port +")");
    	}
		else {
			if (adminOnline) {
				cP.send(AdminConstants.ERROR + AdminConstants.E_BAD_PARAMETER);
				cP.send(AdminConstants.SERVER_IS_OFF);
			}
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
			boolean foundUser = false;
			for (ChatProtokoll cP: server.clients) {
				if (! (cP.isReadyToChat() || server.nameOk(cP.getName())) ) 
					continue;
				msg += cP.getName() + AdminConstants.TRENNZEICHEN_2 + cP.getIP() + AdminConstants.TRENNZEICHEN;
				foundUser = true;
			}
			if (foundUser) {
				cP.send(msg.substring(0, msg.length()-1)+")");
			}
			else {
				cP.send(msg + ")");
			}
			
		}
	}
	
	public void handleError(Exception e) {
		pe(e);
	}
	
	public void goOffline() {
		online = false;
		if (!adminOnline) {
			cP.abortConnecting();
			cP.disconnect();
		}
		else {
			cP.send(AdminConstants.SERVERUPDATE);
		}
		server.goOffline();
		while (! cP.isSocketFree()) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {}
		}
	}
	
	public void shutdown() {
		if (adminOnline) {
			cP.disconnect();
		}
		server.shutdown(ChatConstants.SERVERUPDATE);
		pl("AT has been shutdown");
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

	public Configuration getConfiguration() {
		con.save();
		Configuration config = new Configuration();
		config.add("password", pw);
		config.add("server_pw", serverPw);
		config.add("port", PORT+"");
		config.add("server_port", serverPort+"");
		config.add("server_online", (serverOnline ? "true" : "false"));
		config.add("server_name", serverName);
		return config;
	}
	
	public void pl(String s) {
		pl(s, "AdminThread");
	}
	
	public void pl(String s, String name) {
		if (VERBOSE)
			appentToDebugfile( calendarToString(Calendar.getInstance()) + " " + name + ": " + s);
	}
	
	public String calendarToString(Calendar c) {
		int t = c.get(Calendar.DAY_OF_MONTH);
		String date = (t < 10 ? "0" + t : "" + t) + ".";
		t = c.get(Calendar.MONTH) + 1;
		date += (t < 10 ? "0" + t : "" + t) + ".";
		t = c.get(Calendar.YEAR);
		date += t + " ";
		t = c.get(Calendar.HOUR_OF_DAY);
		date += (t < 10 ? "0" + t : "" + t) + ":";
		t = c.get(Calendar.MINUTE);
		date += (t < 10 ? "0" + t : "" + t) + ":";
		t = c.get(Calendar.SECOND);
		date += (t < 10 ? "0" + t : "" + t);
		return date;
	}
	
	public void pe(Throwable e) {
		if (VERBOSE) {
			StackTraceElement[] stackTrace = e.getStackTrace();
			pl("Error dedected");
			appentToDebugfile("-");
			appentToDebugfile(e.toString());
			for (StackTraceElement ste: stackTrace)
				appentToDebugfile(ste.toString());
			appentToDebugfile("-");
		}
	}
	
	public void appentToDebugfile(String msg) {
		msg = msg + "\n";
		try {
			File f = ReadWriteFile.getFileInParentPath("debug.txt");
			RandomAccessFile raf = new RandomAccessFile(f, "rw");
			raf.seek(raf.length());
			raf.write(msg.getBytes());
			raf.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
