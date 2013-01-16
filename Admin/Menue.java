package Admin;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;

import ChatProtokoll.*;
import Configuration.JAR_Configuration;
import Client.ChatPanel;

/**
 * @author Marcel Kramer
 * @serial 0.1
 */
public class Menue extends JFrame implements WindowListener {
	
	protected ChatProtokoll cP;
	protected JAR_Configuration con;
	protected LogIn logIn;
	
	protected ButtonPanel buttonPanel;
	protected ChatPanel chatPanel;
	protected UserPanel userPanel;
	
	protected JPanel JP_Additional;
	
	protected boolean showConfig;
	protected boolean showUser;
	
	protected String serverPW;
	protected int serverPort;
	
	
	
	public Menue(ChatProtokoll cP, LogIn logIn, JAR_Configuration con) {
		super("Serveradministration v0.1");
		this.cP = cP;
		this.con = con;
		this.logIn = logIn;
		
		addWindowListener(this);
		
		JP_Additional = new JPanel();
		chatPanel = new ChatPanel();
		buttonPanel = new ButtonPanel(this);
		userPanel = new UserPanel(this);
		
		JP_Additional.setLayout(new BoxLayout(JP_Additional, BoxLayout.X_AXIS));
		
		setSize(500,500);
		setLocationRelativeTo(logIn);
		setLayout(new BorderLayout(3, 3));
		
		add(buttonPanel, BorderLayout.LINE_START);
		add(chatPanel, BorderLayout.CENTER);
		add(JP_Additional, BorderLayout.LINE_END);
		
		setVisible(true);
	}
	
	public void setUserVisible(boolean b) {
		showUser = b;
		if (b)
			cP.send(AdminConstants.UP_USER);
		updateAdditionalPanel();
		
	}
	
	public void updateAdditionalPanel() {
		JPanel jp = new JPanel();	
		jp.setLayout(new BoxLayout(jp, BoxLayout.X_AXIS));
		if (showUser)
			jp.add(userPanel);
		try {
			JP_Additional.remove(0);
		}
		catch(Exception e) {}
		JP_Additional.add(jp);
		doUpdate();
	}
	
	public void doUpdate() {
		int w = this.getWidth();
		int h = this.getHeight();
		this.setSize(w - 1, h);
		this.setSize(w,h);
	}
	
	public void command(String com) {
		System.out.println(com);
		if (com.startsWith(AdminConstants.TEST)) {
			cP.send(AdminConstants.RESPONSE);
		}
		else if (com.startsWith(AdminConstants.SERVER_IS_ON)) {
			com = com.substring(AdminConstants.SERVER_IS_ON.length(), com.length()-1);
			String[] param = com.split(AdminConstants.TRENNZEICHEN);
			serverPort = Integer.parseInt(param[1]);
			serverPW = param[0];
			buttonPanel.setServerOn(true);
		}
		else if (com.startsWith(AdminConstants.SERVER_IS_OFF)) {
			buttonPanel.setServerOn(false);
		}
		else if (com.startsWith(AdminConstants.UP_USER)) {
			if (!showUser)
				return;
			String splt = ("|".equals(AdminConstants.TRENNZEICHEN) ? "-" : "|");
			com = com.substring(AdminConstants.UP_USER.length(), com.length()-1);
			String[] param = com.split(AdminConstants.TRENNZEICHEN);
			String[][] info = new String[2][param.length];
			int i = 0;
			for (String s: param) {
				String[] val = s.split(splt);
				info[0][i] = val[0];
				info[1][i] = val[1];
				i++;
			}
			userPanel.updateInformation(info[0], info[1]);
		}
	}
	
	public void windowOpened(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}			
	public void windowClosing(WindowEvent e) {
		cP.disconnect();
	}
	
	public void finalize() throws Throwable {
		con.save();
		super.finalize();
	}
	
}
