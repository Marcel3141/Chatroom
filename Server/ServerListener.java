package Server;

/**
 * @author Marcel Kramer
 */
public interface ServerListener {
	
	public void updateUser();
	public void handleError(Exception e);
	
}