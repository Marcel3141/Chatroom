package ChatProtokoll;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.math.BigInteger;

import Verschluesselung.RSA;

/**
 * @author Marcel Kramer
 * @serial 0.1
 */
public class ChatProtokoll implements Runnable {
	
	protected boolean readyToChat;
	protected boolean connected;
	protected boolean tryingToConnect;
	protected boolean keysReady;
	protected boolean disabled;
	protected boolean client;
	protected boolean keysExchanged;
	
	protected Socket sock;
	protected BufferedReader in;
	protected PrintWriter out;
	
	protected ChatListener cL;
	
	protected BigInteger prodIn;
	protected BigInteger expIn;
	
	protected BigInteger prodOut;
	protected BigInteger expOut;
	protected BigInteger invOut;
	
	protected int randCount;
	protected String name;
	protected String otherName;
	protected String otherIP;
	
	public ChatProtokoll(ChatListener cL) {
		this.cL = cL;
		randCount = 1;
		name = "";
		setStartValues();
	}
	
	public ChatProtokoll(ChatListener cL, int randCount) {
		this.cL = cL;
		name = "";
		this.randCount = randCount;
		setStartValues();
	}
	
	public ChatProtokoll(ChatListener cL, int randCount, String name) {
		this.cL = cL;
		this.randCount = randCount;
		this.name = name;
		setStartValues();

	}
	
	protected void setStartValues() {
		readyToChat = false;
		connected = false;
		tryingToConnect = false;
		keysReady = false;
		disabled = false;
		keysExchanged = false;
		otherName = "";
		otherIP = "";
	}
	
	protected void setDefaultValues(boolean disabled) {
		readyToChat = false;
		connected = false;
		tryingToConnect = false;
		this.disabled = disabled;
		keysExchanged = false;
		otherName = "";
		otherIP = "";
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setRandomNumbers(int count) {
		if (count > 0)
			randCount = count;
	}
	
	public void setKeys(BigInteger prod, BigInteger exp, BigInteger inv) {
		if (!keysReady && !connected) {
			keysReady = true;
			prodOut = prod;
			expOut = exp;
			invOut = inv;
		}
	}
	
	public void connectTo(String ip, int port, String pw) {
		new connectThread(ip, port, pw, this);
	}
	
	public void connectTo(String ip, int port, String pw, boolean asThread) {
		if (asThread) {
			new connectThread(ip, port, pw, this);
			return;
		}
		if(!tryingToConnect && keysReady && !disabled && !connected) {
			tryingToConnect = true;
			client = true;
			try {
				sock = new Socket(ip, port);
				verify(pw);
			}
			catch(Exception e) {
				cL.failedToConnect(e, this);
			}
			
		}
		if (!readyToChat) {
			setDefaultValues(false);
		}
	}
	
	public void waitAsHost(int port, String pw, int checkTime) {
		new connectThread(port, pw, checkTime, this);
	}
	
	public void waitAsHost(int port, String pw,
			int checkTime, boolean asThread) {
		if (asThread) {
			new connectThread(port, pw, checkTime, this);
			return;
		}
		if(!tryingToConnect && keysReady && !disabled && !connected) {
			tryingToConnect = true;
			client = false;
			ServerSocket serSock = null;
			try {
				serSock = new ServerSocket(port);
				serSock.setSoTimeout(checkTime);
				while(!disabled) {
					try {
						sock = serSock.accept();
						break;
					}
					catch(SocketTimeoutException e) {}
				}
				serSock.close();
				verify(pw);
			}
			catch(Exception e) {
				try {
					serSock.close();
				}
				catch(Exception e1) {}
				cL.failedToConnect(e, this);
			}
		}
		if (!readyToChat) {
			setDefaultValues(false);
		}
	}
	
	protected void verify(String pw) {
		if (!disabled) {
			tryingToConnect = false;
			otherIP = (sock.getInetAddress()+"").substring(1);
			connected = true;
			cL.connected(this);
			if (disabled)
				return;
			try {
				keyExchange();
				keysExchanged = true;
			}
			catch(Exception e) {
				cL.failedToConnect(e, this);
				connected = false;
				disabled = true;
				return;
			}
			if (disabled)
				return;
			boolean loggedIn = logIn(pw);
			if (disabled)
				return;
			if (!loggedIn) {
				cL.wrongPassword(this);
				return;
			}
			exchangeNames();
			if (disabled)
				return;
			cL.logedIn(this);
			if (!disabled) {
				readyToChat = true;
				new Thread(this).start();
			}
		}
	}
	
	protected void keyExchange() throws IOException {
		out = new PrintWriter(sock.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		keysReady = false;
		out.println(prodOut);
		out.println(expOut);
		String s = getNextLine();
		if (s.equals(ChatConstants.DISCONNECT))
			disabled = true;
		if (disabled)
			return;
		prodIn = new BigInteger(s);
		s = getNextLine();
		if (s.equals(ChatConstants.DISCONNECT))
			disabled = true;
		if (disabled)
			return;
		expIn = new BigInteger(s);
	}
	
	protected void exchangeNames() {
		send(name);
		String s = dechiffrieren(getNextLine());
		if (s.equals(ChatConstants.DISCONNECT))
			disabled = true;
		if (disabled)
			return;
		otherName = s;
	}
	
	protected boolean logIn(String pw) {
		if (client) {
			send(pw);
			String s = getNextLine();
			if (disabled)
				return false;
			s = dechiffrieren(s);
			if ( s.equals(ChatConstants.CONNECTED) )
				return true;
			return false;
		}
		else {
			String s = getNextLine();
			if (disabled)
				return false;
			s = dechiffrieren(s);
			if (! pw.equals(s))
				return false;
			send(ChatConstants.CONNECTED);
			return true;
		}
	}
	
	protected String getNextLine() {
		try {
			while(!in.ready() && !disabled) {
				try {
					Thread.sleep(1);
				}
				catch (Exception e) {}
			}
		}
		catch(Exception e) {}
		if (!disabled) {
			try {
				return in.readLine();
			}
			catch(Exception e) {
				disabled = true;
				return "";
			}
		}
		else
			return "";
	}
	
	public boolean isSocketFree() {
		return !tryingToConnect && !connected && !readyToChat && !disabled;
	}
	
	public boolean isReadyToConnect() {
		return !tryingToConnect && !connected && !readyToChat
			&& !disabled && keysReady;
	}
	
	public boolean isReadyToChat() {
		return readyToChat && !disabled;
	}
	
	public boolean isConnected() {
		return connected && !disabled;
	}
	
	public boolean isTryingToConnect() {
		return tryingToConnect && !disabled;
	}
	
	public boolean keysNeeded() {
		return !keysReady && !connected;
	}
	
	public boolean areKeysReady() {
		return keysReady;
	}
	
	public String getName() {
		return otherName;
	}
	
	public String getIP() {
		return otherIP;
	}
	
	public void abortConnecting() {
		if (tryingToConnect) {
			tryingToConnect = false;
			disabled = true;
		}
	}
	
	public void disconnect() {
		if ((this.readyToChat || connected) && ! disabled) {
			boolean readyToChat = this.readyToChat;
			try {
				send(ChatConstants.DISCONNECT);
			}
			catch(Exception e) {}
			this.readyToChat = false;
			connected = false;
			disabled = true;
			try {
				in.close();
				out.close();
				sock.close();
			}
			catch(Exception e) {
				try {
					sock.close();
				}
				catch(Exception e1) {}
			}
			cL.disconnected(this);
			setDefaultValues(! readyToChat);
		}
	}
	
	protected String dechiffrieren(String chiff) throws NumberFormatException {
		return RSA.dechiffrieren(chiff, prodOut, invOut);
	}
	
	public void send(String msg) {
		if (keysExchanged)
			out.println(RSA.chiffrieren(msg, prodIn, expIn, randCount));
		else if (msg.equals(ChatConstants.DISCONNECT))
			out.println(msg);
	}
	
	public void run() {
		while (readyToChat) {
			try {
				if (in.ready()) {
					String msg = dechiffrieren(in.readLine());
					if (msg.startsWith(ChatConstants.DISCONNECT)&&readyToChat){
						disconnect();
					}
					else {
						if (readyToChat)
							cL.recive(msg, this);
					}
				}
			}
			catch (NumberFormatException e) {
				
			}
			catch (IOException e) {
				disconnect();
			}
			try {
				Thread.sleep(100);
			}
			catch(Exception e) {}
		}
	}
	
}
