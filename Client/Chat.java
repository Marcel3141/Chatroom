package Client;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import ChatProtokoll.ChatProtokoll;
import Configuration.JAR_Configuration;

/**
 * @author Marcel Kramer
 * @serial 0.1
 */
public class Chat extends JFrame implements WindowListener {
	
	protected ChatProtokoll cP;
	protected JAR_Configuration con;
	protected ChatPanel chatPanel;
	
	public Chat(String name, ChatProtokoll cP, LogIn lI, JAR_Configuration con) {
		super("Verbindung zu "+name+" steht");
		
		this.cP = cP;
		this.con = con;
		
		addWindowListener(this);
		
		chatPanel = new ChatPanel(cP);
		
		setSize(500, 500);
		setLocationRelativeTo(lI);		
		
		add(chatPanel);
		
		setVisible(true);
	}
	
	public void empfangen(String in) {
		chatPanel.recive(in);
	}
	
	public void windowOpened(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowActivated(WindowEvent arg0) {	}
	public void windowClosed(WindowEvent arg0) {}			
	public void windowClosing(WindowEvent arg0) {
		cP.disconnect();
	}
	
	public void finalize() throws Throwable {
		super.finalize();
		this.dispose();
	}
}
