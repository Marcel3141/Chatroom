package Admin;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.math.BigInteger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ChatProtokoll.ChatProtokoll;
import ChatProtokoll.ChatListener;
import Client.ChatPanel;
import Configuration.JAR_Configuration;
import Verschluesselung.RSA;
/**
 * @author Marcel Kramer
 * @serial 0.1
 */
public class ButtonPanel extends JMenuBar implements ActionListener, ChatListener {

	protected static final String SERVER_ON = "Server starten";
	protected static final String SERVER_OFF = "Server stoppen";
	protected static final String USER_ON = "Userverwaltung aktivieren";
	protected static final String USER_OFF = "Userverwaltung deaktivieren";
	protected static final String CHAT_ON = "Chat starten";
	protected static final String CHAT_OFF = "Chat beenden";
	
	protected JMenuItem JB_Server = new JMenuItem(SERVER_ON);
	protected JMenuItem JB_User = new JMenuItem(USER_ON);
	protected JMenuItem JB_Chat = new JMenuItem(CHAT_ON);
	
	protected JMenu JM_Server = new JMenu("Server");
	protected JMenu JM_View = new JMenu("Ansicht");
	
	protected boolean serverOn;
	
	protected ChatProtokoll cP;
	
	protected final int hight = 25;
	protected final int hight2 = 50;
	
	protected final Menue menue;
	
	public Thread schluessel_generieren = new Thread(new Runnable() {
		public void run() {
			BigInteger[] schluessel = RSA.erstellen(menue.logIn.BIN_LENGTH);
			cP.setKeys(schluessel[0], schluessel[1], schluessel[2]);
			JB_Chat.setEnabled(serverOn);
		}
	});
	
	public ButtonPanel(Menue menue) {
		this.menue = menue;
		
		serverOn = false;
		
		JB_Server.setActionCommand(SERVER_ON);
		JB_User.setActionCommand(USER_ON);
		JB_Chat.setActionCommand(CHAT_ON);
		
		JB_Server.setEnabled(false);
		JB_User.setEnabled(false);
		JB_Chat.setEnabled(false);
		
		JB_Server.addActionListener(this);
		JB_User.addActionListener(this);
		JB_Chat.addActionListener(this);
		
		JM_Server.add(JB_Server);
		JM_View.add(JB_Chat);
		JM_View.add(JB_User);
		
		add(JM_Server);
		add(JM_View);
		
		new Thread(schluessel_generieren).start();
		cP = new ChatProtokoll(this, menue.logIn.RAND_COUNT);
		menue.chatPanel.setChatProtokoll(cP);
	}
	
	public void setServerOn(boolean b) {
		serverOn = b;
		JB_Server.setActionCommand(b ? SERVER_OFF : SERVER_ON);
		JB_Server.setText(b ? SERVER_OFF : SERVER_ON);
		JB_Server.setEnabled(true);
		if (b) {
			JB_User.setEnabled(true);
			if (cP.areKeysReady())
				JB_Chat.setEnabled(true);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(SERVER_ON)) {
			JB_Server.setEnabled(false);
			new CreateServer(menue, menue.con);
		}
		else if (e.getActionCommand().equals(SERVER_OFF)) {
			JB_Server.setEnabled(false);
			
			JB_User.setActionCommand(USER_ON);
			JB_User.setText(USER_ON);
			JB_User.setEnabled(false);
			
			menue.cP.send(AdminConstants.STOP_SERVER);
		}
		else if (e.getActionCommand().equals(CHAT_ON)) {
			JB_Chat.setEnabled(false);
			cP.setName(menue.logIn.JTF_name.getText());
			cP.connectTo( menue.logIn.JTF_ip.getText(),
				menue.serverPort, menue.serverPW);
		}
		else if (e.getActionCommand().equals(CHAT_OFF)) {
			JB_Chat.setEnabled(false);
			cP.disconnect();
		}	
		else if (e.getActionCommand().equals(USER_ON)) {
			JB_User.setActionCommand(USER_OFF);
			JB_User.setText(USER_OFF);
			menue.setUserVisible(true);
		}
		else if (e.getActionCommand().equals(USER_OFF)) {
			JB_User.setActionCommand(USER_ON);
			JB_User.setText(USER_ON);
			menue.setUserVisible(false);
		}
	}
	
	public void connected(ChatProtokoll cP) {}
	
	public void logedIn(ChatProtokoll cP) {
		if (serverOn) {
			JB_Chat.setActionCommand(CHAT_OFF);
			JB_Chat.setText(CHAT_OFF);
			JB_Chat.setEnabled(true);
		}
		menue.chatPanel.setConnected(true);
		menue.chatPanel.setEnabled(true);
	}
	
	public void recive(String msg, ChatProtokoll cP) {
		menue.chatPanel.recive(msg);
	}
	
	public void disconnected(ChatProtokoll cP) {
		if (serverOn) {
			JB_Chat.setActionCommand(CHAT_OFF);
			JB_Chat.setText(CHAT_OFF);
			JB_Chat.setEnabled(true);
		}
		checkKeyGeneration();
		menue.chatPanel.setConnected(false);
	}
	
	public void failedToConnect(Exception e, ChatProtokoll cP) {
		checkKeyGeneration();
	}
	
	public void wrongPassword(ChatProtokoll cP) {
		checkKeyGeneration();
	}
	
	public void checkKeyGeneration() {
		if (! cP.areKeysReady() ) {
			JB_Chat.setEnabled(false);
			new Thread(schluessel_generieren).start();
		}
	}
	
}