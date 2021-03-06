package Server;

import java.util.Vector;
import java.math.BigInteger;

import Verschluesselung.PrimzahlThread;
import ChatProtokoll.*;
import Configuration.JAR_Configuration;

/**
 * @author Marcel Kramer
 */
public class Server implements ChatListener, Runnable{
	
	public final int PORT;
	public final String LOG_IN_PW;
	public final int CHECK_TIME;
	public final int RAND_COUNT;
	public final String NAME;
	
	public ServerListener sL;
	public JAR_Configuration con;
	public PrimzahlThread pT;
	
	public Vector<ChatProtokoll> clients;
	
	public Vector<String> lockedIP;
	
	protected Vector<BigInteger[]> key;
	
	protected boolean online;
	
	public Server(final String PASSWORT, final int PORT, ServerListener sL) {
		this.PORT = PORT;
		this.LOG_IN_PW = PASSWORT;
		this.sL = sL;
		NAME = "Server";
		con = new JAR_Configuration("Server.ini", "=");
		RAND_COUNT = con.getInt("RAND_COUNT", 3, 100);
		CHECK_TIME = con.getInt("CHECK_TIME", 100, 300000, 5000);
		startServer();
    }
	
	public Server(final String PASSWORT, final int PORT, ServerListener sL, String name) {
		this.PORT = PORT;
		this.LOG_IN_PW = PASSWORT;
		this.sL = sL;
		this.NAME = name;
		con = new JAR_Configuration("Server.ini", "=");
		RAND_COUNT = con.getInt("RAND_COUNT", 3, 100);
		CHECK_TIME = con.getInt("CHECK_TIME", 100, 300000, 5000);
		startServer();
	}
	
	protected void startServer() {
		online = true;
		
		int m = con.getInt("MAX_BUFFERED_KEYS", 5, 5000);
		
		key = new Vector<BigInteger[]>();
		
		pT = new PrimzahlThread(key, m, con.getInt("BIN_LENGTH", 100, 25000));
		
		lockedIP = new Vector<String>();
		clients = new Vector<ChatProtokoll>();
		
		new Thread(this).start();
		sL.pl("Server started", "Server");
	}
	
	protected void startChatProtokoll() {
		if (online) {
			BigInteger[] key = getKeys();
			ChatProtokoll cP = new ChatProtokoll(this, RAND_COUNT, NAME);
			cP.setKeys(key[0], key[1], key[2]);
			cP.waitAsHost(PORT, LOG_IN_PW, CHECK_TIME);
			clients.add(cP);
		}
	}
    
    public BigInteger[] getKeys() {
    	while (key.isEmpty()) {
    		try {
				Thread.sleep(100);
			} 
    		catch (InterruptedException e) {}
    	}
    	BigInteger[] key2 = key.firstElement();
    	key.remove(0);
    	return key2;
    }
	
	public void connected(ChatProtokoll cP) {
		if (online)
			new Thread(this).start();
		String ip = cP.getIP();
		if (lockedIP.contains(ip))
			cP.disconnect();
	}
	
	public void logedIn(ChatProtokoll cP) {
		if (! nameOk(cP.getName())) {
			cP.disconnect();
		}
		else {
			sendOther(cP.getName() + " hat den Chatroom betreten", NAME, cP);
			sL.updateUser();
		}
	}
	
	public void recive(String msg, ChatProtokoll cP) {
		sendOther(msg, cP);
	}
	
	public void disconnected(ChatProtokoll cP) {
		delete(cP);
		if(online) {
			sendOther(cP.getName() + " hat den Chatroom verlassen", NAME, cP);
			sL.updateUser();
		}
	}
	
	public void failedToConnect(Exception e, ChatProtokoll cP) {
		if (online && cP.isConnected())
			new Thread(this).start();
		sL.handleError(e);
	}
	
	public void wrongPassword(ChatProtokoll cP) {
		delete(cP);
	}
	
	public boolean nameOk(String name) {
		if (name.length() > 8)
			return false;
		name = name.trim();
		if (name.length() < 1)
			return false;
		
		return true;
	}
	
	public void sendAll(String msg) {
		for(ChatProtokoll cP: clients)
			cP.send(msg);
	}
	
	public void sendOther(String msg, ChatProtokoll notThis) {
		sendOther(msg, notThis.getName(), notThis);
	}
	
	public void sendOther(String msg, String name, ChatProtokoll notThis) {
		for(ChatProtokoll cP: clients) {
			if (!cP.equals(notThis)) {
				cP.send(msg + ChatConstants.TRENNZEICHEN + name);
			}
		}
	}
	
	protected void delete(ChatProtokoll cP) {
		try {
			clients.remove(clients.indexOf(cP));
		}
		catch(Exception e) {}
	}
	
	public void lockIP(String ip) {
		lockedIP.add(ip);
		if (clients.isEmpty())
			return;
		int i = 0;
		while (true) {
			if (i >= clients.size())
				break;
			ChatProtokoll cP = clients.get(i);
			if (cP.getIP().equals(ip)) {
				cP.disconnect();
				i--;
			}
			i++;
		}
	}
	
	public void run() {
		startChatProtokoll();
	}
	
	public void goOffline() {
		online = false;
		pT.stopThread();
		ChatProtokoll cP = clients.lastElement();
		cP.abortConnecting();
		cP.disconnect();
		while (! cP.isSocketFree()) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {}
		}
	}
	
	public void shutdown() {
		shutdown("");
	}
	
	public void shutdown(String msg) {
		if (online) {
			goOffline();
		}
		if (! msg.equals(""))
			sendAll(NAME + ": " + msg);
		sendAll(NAME + ": Server is shutting down");
		if (clients.isEmpty())
			return;
		while (! clients.isEmpty()) {
			ChatProtokoll cP = clients.firstElement();
			cP.disconnect();
			delete(cP);
		}
		clients = null;
		sL.pl("Server has been shutdown", "Server");
		sL = null;
	}
	
}
