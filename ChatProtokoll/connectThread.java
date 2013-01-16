package ChatProtokoll;

public class connectThread extends Thread{
	
	protected boolean client;
	protected int port;
	protected String pw;
	protected ChatProtokoll cp;
	
	protected String ip;
	protected int checkTime;
	
	public connectThread(String ip, int port, String pw, ChatProtokoll cp) {
		client = true;
		this.cp = cp;
		this.port = port;
		this.pw = pw;
		this.ip = ip;
		this.start();
	}

	public connectThread(int port, String pw, int checkTime, ChatProtokoll cp) {
		client = false;
		this.cp = cp;
		this.port = port;
		this.pw = pw;
		this.checkTime = checkTime;
		this.start();
	}
	
	public void run() {
		if (client) 
			cp.connectTo(ip, port, pw, false);
		else
			cp.waitAsHost(port, pw, checkTime, false);
	}
}