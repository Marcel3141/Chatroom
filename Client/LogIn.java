package Client;

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
	public ButtonGroup BG_typ = new ButtonGroup();
	public JRadioButton JRB_host = new JRadioButton("Host");
	public JRadioButton JRB_client = new JRadioButton("Client");
	public JButton JB_ok = new JButton("Verbinden");
	public JLabel JL_Meldung = new JLabel("");
	
	public ActionListener JRB_ActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JL_Meldung.setText("");
			if (JRB_client.isSelected()) {
				if (JB_ok.getActionCommand().equals("ok"))
					JB_ok.setText("Verbinden");
				JTF_ip.setEnabled(true);
				JPF_pw.setEditable(true);
			}
			else {
				if (JB_ok.getActionCommand().equals("ok"))
					JB_ok.setText("Erstellen");
				JTF_ip.setEnabled(false);
				JPF_pw.setEditable(false);
			}
			
		}
	};
	
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
	public Chat chat;
	
	public LogIn() {
		super("Chatroom v0.1");	
		setSize(370, 270);
		setLocationRelativeTo(null);
		setResizable(false);
		
		con = new JAR_Configuration("Chatter.ini", "=", "ConfigurationFiles");
		
		RAND_COUNT = con.getInt("RAND_COUNT", 2, 5000, 10);
		BIN_LENGTH = con.getInt("BIN_LENGTH", 128, 1073741824, 1024);
		if (0 == con.getInt("selectedButton"))
			JRB_client.setSelected(true);
		else
			JRB_host.setSelected(true);
		
		JRB_ActionListener.actionPerformed(null);
		
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
		JP_ok.setBounds(100, 160, 150, 50);
		
		JPanel JP_typ = new JPanel();
		BG_typ.add(JRB_client);
		BG_typ.add(JRB_host);
		JP_typ.add(JRB_client);
		JP_typ.add(JRB_host);
		JRB_host.addActionListener(JRB_ActionListener);
		JRB_client.addActionListener(JRB_ActionListener);
		JP_typ.setBounds(30, 130, 300, 25);
		
		JPanel JP_Meldung = new JPanel();
		JP_Meldung.add(JL_Meldung);
		JP_Meldung.setBounds(20, 205, 300, 50);
		
		JPanel JP_main = new JPanel(null);
		JP_main.add(JP_name);
		JP_main.add(JP_ip);
		JP_main.add(JP_port);
		JP_main.add(JP_typ);
		JP_main.add(JP_pw);
		JP_main.add(JP_ok);
		JP_main.add(JP_Meldung);
		
		add(JP_main);
		
		JB_ok.setActionCommand("ok");
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
		if(e.getActionCommand().equals("ok")) {
			JB_ok.setActionCommand("abort");
			JB_ok.setText("Abbrechen");
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
			if(JRB_client.isSelected()) {
				con.change("selectedButton", "0");
				cP.connectTo(con.get("ip"), con.getInt("port"), pw);
			}
			else {
				con.change("selectedButton", "1");
				cP.waitAsHost(con.getInt("port"), pw, 1500); //alle 1,5 sec wird getestet ob abgebrochen wurde
			}
		}
		else if (e.getActionCommand().equals("abort")) {
			JB_ok.setActionCommand("ok");
			cP.abortConnecting();
			
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
		chat = new Chat(cP.getName(), cP, this, con);
	}
	
	public void recive(String s, ChatProtokoll cP) {
		chat.empfangen(s);
	}
	
	public void disconnected(ChatProtokoll cP) {
		checkKeyGeneration();
		JB_ok.setActionCommand("ok");
		JRB_ActionListener.actionPerformed(null);
		JL_Meldung.setText("Verbindung wurde beendet");
		chat = null;
		this.setVisible(true);
	}
	
	public void failedToConnect(Exception e, ChatProtokoll cP) {
		checkKeyGeneration();
		JB_ok.setActionCommand("ok");
		JRB_ActionListener.actionPerformed(null);
		JL_Meldung.setText("Fehler beim Verbindungsaufbau");
	}
	
	public void wrongPassword(ChatProtokoll cP) {
		checkKeyGeneration();
		JB_ok.setActionCommand("ok");
		JRB_ActionListener.actionPerformed(null);
		JL_Meldung.setText("Falsches password");
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
