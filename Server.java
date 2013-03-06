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
	
	public Server(String pw, int port, boolean verbose) {
		AT = new AdminThread(pw, port, verbose);
	}
	
	public Server(Configuration con, boolean verbose) {
		AT = new AdminThread(con, verbose);
	}
	
	public StartClass start(String pw, int port, boolean verbose) { 
		return new Server(pw, port, verbose);
	}
	
	public StartClass start(Configuration con, boolean verbose) {
		return new Server(con, verbose);
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