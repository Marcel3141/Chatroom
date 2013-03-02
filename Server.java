import AdminThread.AdminThread;
import Configuration.Configuration;

/**
 * @author Marcel Kramer
 */
public class Server implements StartClass {
	
	protected AdminThread AT;
	
	public Server() {
		AT = null;
	}
	
	public Server(String pw, int port) {
		AT = new AdminThread(pw, port);
	}
	
	public Server(Configuration con) {
		AT = new AdminThread(con);
	}
	
	public StartClass start(String pw, int port) { 
		return new Server(pw, port);
	}
	
	public StartClass start(Configuration con) {
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