import Configuration.Configuration;

/**
 * @author Marcel Kramer
 */
public interface StartClass {
	
	public StartClass start(String pw, int port, boolean verbose);
	public StartClass start(Configuration con, boolean verbose);
	public Configuration getConfig();
	public void pause();
	public void stop();
	
}
