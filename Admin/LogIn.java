package Admin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.math.BigInteger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import Configuration.JAR_Configuration;
import Verschluesselung.RSA;
import ChatProtokoll.ChatProtokoll;
import ChatProtokoll.ChatListener;

/**
 * @author Marcel Kramer
 * @serial 0.1
 */
public class LogIn extends JFrame implements ActionListener, WindowListener, ChatListener {
	
	public final int RAND_COUNT;
	public final int BIN_LENGTH;
	
	public JTextField JTF_name = new JTextField("User");
	public JTextField JTF_ip = new JTextField("localHost");
	public JTextField JTF_port = new JTextField("2000");
	public JPasswordField JPF_pw = new JPasswordField();
	public JButton JB_ok = new JButton("Verbinden");
	public JLabel JL_Meldung = new JLabel("");
	
	public KeyListener JTF_KeyListener = new KeyListener() {
		public void keyTyped(KeyEvent e) {}
		public void keyReleased(KeyEvent e) {}
		public void keyPressed(KeyEvent e) {
			JL_Meldung.setText("");
		}
	};
	
	public Thread schluessel_generieren = new Thread(new Runnable() {
		public void run() {
			schluessel = RSA.erstellen(BIN_LENGTH);
			JB_ok.setEnabled(true);
		}
	});
	
	protected BigInteger[] schluessel = new BigInteger[3];
	
	public JAR_Configuration con;
	public ChatProtokoll cP;
	public Menue menue;
	
	public LogIn() {
		super("Serveradministration v0.1");	
		setSize(370, 250);
		setLocationRelativeTo(null);
		setResizable(false);
		
		con = new JAR_Configuration("Admin.ini", "=", "ConfigurationFiles");
		
		RAND_COUNT = con.getInt("RAND_COUNT", 2, 100);
		BIN_LENGTH = con.getInt("BIN_LENGTH", 100, 50000, 1024);
		
		JTF_name.setText(con.get("name"));
		JTF_ip.setText(con.get("ip"));
		JTF_port.setText(con.get("port"));
		
		addWindowListener(this);
		
		JPanel JP_name = createPanel("Name", JTF_name);
		JP_name.setBounds(10, 10, 350, 25);
		
		JPanel JP_ip = createPanel("IP", JTF_ip);
		JP_ip.setBounds(10, 40, 350, 25);
		
		JPanel JP_port = createPanel("Port", JTF_port);
		JP_port.setBounds(10, 70, 350, 25);
		
		JPanel JP_pw = createPanel("Passwort", JPF_pw);
		JP_pw.setBounds(10, 100, 350, 25);
		
		JPanel JP_ok = new JPanel(null);
		JP_ok.add(JB_ok);
		JB_ok.setSize(150, 30);
		JB_ok.addActionListener(this);
		JP_ok.setBounds(100, 130, 150, 50);
		
		JPanel JP_Meldung = new JPanel();
		JP_Meldung.add(JL_Meldung);
		JP_Meldung.setBounds(20, 185, 300, 50);
		
		JPanel JP_main = new JPanel(null);
		JP_main.add(JP_name);
		JP_main.add(JP_ip);
		JP_main.add(JP_port);
		JP_main.add(JP_pw);
		JP_main.add(JP_ok);
		JP_main.add(JP_Meldung);
		
		add(JP_main);
		
		JB_ok.setActionCommand("connect");
		JB_ok.setEnabled(false);
		new Thread(schluessel_generieren).start();
		setVisible(true);
		cP = new ChatProtokoll(this, RAND_COUNT);
	}
	
	protected JPanel createPanel(String name, Component cmp) {
		JPanel JP = new JPanel(null);
		JLabel JL = new JLabel(name +": ");
		JL.setBounds(0, 0, 80, 25);
		cmp.setBounds(80, 0, 250, 25);
		JP.add(JL);
		JP.add(cmp);
		cmp.addKeyListener(JTF_KeyListener);
		JP.setBounds(10, 10, 350, 25);
		return JP;
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("connect")) {
			con.change("name", JTF_name.getText());
			con.change("ip", JTF_ip.getText());
			con.change("port", JTF_port.getText());
			if (cP.keysNeeded())
				cP.setKeys(schluessel[0],schluessel[1],schluessel[2]);
			cP.setName(con.get("name"));
			String pw = "";
			char[] pw_c = JPF_pw.getPassword();
			for (char c: pw_c) {
				pw += c + "";
			}
			cP.connectTo(con.get("ip"), con.getInt("port"), pw);
		}
		
	}
	
	public void checkKeyGeneration() {
		if (! cP.areKeysReady() ) {
			JB_ok.setEnabled(false);
			new Thread(schluessel_generieren).start();
		}
	}
	
	public void connected(ChatProtokoll cP) {}
	
	public void logedIn(ChatProtokoll cP) {
		this.setVisible(false);
		menue = new Menue(cP, this, con);
	}
	
	public void recive(String s, ChatProtokoll cP) {
		menue.command(s);
	}
	
	public void disconnected(ChatProtokoll cP) {
		checkKeyGeneration();
		JL_Meldung.setText("Verbindung wurde beendet");
		try {
			menue.finalize();
		}
		catch(Throwable e) {}
		menue = null;
		this.setVisible(true);
	}
	
	public void failedToConnect(Exception e, ChatProtokoll cP) {
		checkKeyGeneration();
		JL_Meldung.setText("Fehler beim Verbindungsaufbau");
		this.setVisible(true);
		e.printStackTrace();
	}
	
	public void wrongPassword(ChatProtokoll cP) {
		checkKeyGeneration();
		JL_Meldung.setText("Falsches password");
		this.setVisible(true);
	}
	
	public void windowOpened(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowActivated(WindowEvent arg0) {	}
	public void windowClosed(WindowEvent arg0) {}			
	public void windowClosing(WindowEvent arg0) {
		try {
			finalize();
		}
		catch (Throwable e) {}
		System.exit(0);
	}
	
	protected void finalize() throws Throwable {
		con.save();
		super.finalize();
	}

}
