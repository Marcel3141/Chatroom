package ChatProtokoll;

/**
 * @author Marcel Kramer
 * @serial 0.1
 */
public interface ChatListener {
	
	public void connected(ChatProtokoll cP);
	public void logedIn(ChatProtokoll cP);
	public void recive(String s, ChatProtokoll cP);
	public void disconnected(ChatProtokoll cP);
	public void failedToConnect(Exception e, ChatProtokoll cP);
	public void wrongPassword(ChatProtokoll cP);
	
}
