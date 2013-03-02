import Configuration.Configuration;

/**
 * @author Marcel Kramer
 */
public interface StartClass {
	
	public StartClass start(String pw, int port);
	public StartClass start(Configuration con);
	public Configuration getConfig();
	public void pause();
	public void stop();
	
}
