import AdminThread.AdminThread;
import Configuration.Configuration;

public class Server extends StartClass{
	
	protected AdminThread AT;
	
	public Server(String pw, int port) {
		AT = new AdminThread(pw, port);
	}
	
	public Server(Configuration con) {
		AT = new AdminThread(con);
	}
	
	public static StartClass start(String pw, int port) { 
		return new Server(pw, port);
	}
	
	public static StartClass start(Configuration con) {
		return new Server(con);
	}
	
	public Configuration getConfig() {
		Configuration con = AT.getConfiguration();
		return con;
	}
	
	public void pause() {
		AT.goOffline();
	}
	
	public void stop() {
		AT.shutdown();
	}
	
}