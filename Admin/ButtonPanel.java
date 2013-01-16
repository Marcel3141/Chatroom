package Admin;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.math.BigInteger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JButton;

import ChatProtokoll.ChatProtokoll;
import ChatProtokoll.ChatListener;
import Client.ChatPanel;
import Configuration.JAR_Configuration;
import Verschluesselung.RSA;
/**
 * @author Marcel Kramer
 * @serial 0.1
 */
public class ButtonPanel extends JPanel implements ActionListener, ChatListener {

	protected static final String SERVER_ON = "<html>Server starten";
	protected static final String SERVER_OFF = "<html>Server stoppen";
	protected static final String USER_ON = "<html>Userverwaltung<br />aktivieren";
	protected static final String USER_OFF = "<html>Userverwaltung<br />deaktivieren";
	protected static final String CHAT_ON = "<html>Chat starten";
	protected static final String CHAT_OFF = "<html>Chat beenden";
	
	protected JButton JB_Server = new JButton(SERVER_ON);
	protected JButton JB_User = new JButton(USER_ON);
	protected JButton JB_Chat = new JButton(CHAT_ON);
	
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
		
		JPanel JP_buttons = new JPanel();
		JP_buttons.setLayout(new BoxLayout(JP_buttons, BoxLayout.Y_AXIS));
		JP_buttons.add(createButtonPanel(JB_Server, hight));
		JP_buttons.add(createButtonPanel(JB_User, hight2));
		JP_buttons.add(createButtonPanel(JB_Chat, hight));
		
		add(JP_buttons);
		
		new Thread(schluessel_generieren).start();
		cP = new ChatProtokoll(this, menue.logIn.RAND_COUNT);
		menue.chatPanel.setChatProtokoll(cP);
	}
	
	protected JPanel createButtonPanel(Component comp, int hight) {
		JPanel tmp = new JPanel();
		tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
		tmp.add(Box.createRigidArea(new Dimension(3, hight)));
		tmp.add(comp);
		tmp.add(Box.createRigidArea(new Dimension(3, hight)));
		return tmp;
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
		System.out.println("logged in");
		menue.chatPanel.setConnected(true);
		menue.chatPanel.setEnabled(true);
	}
	
	public void recive(String msg, ChatProtokoll cP) {
		menue.chatPanel.recive(msg);
	}
	
	public void disconnected(ChatProtokoll cP) {
		checkKeyGeneration();
		menue.chatPanel.setConnected(false);
	}
	
	public void failedToConnect(Exception e, ChatProtokoll cP) {
		e.printStackTrace();
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