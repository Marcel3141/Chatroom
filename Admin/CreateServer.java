package Admin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.Component;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import Configuration.JAR_Configuration;
/**
 * @author Marcel Kramer
 * @serial 0.1
 */
public class CreateServer extends JDialog implements ActionListener, WindowListener {
	
	public JTextField JTF_name;
	public JTextField JTF_port;
	public JTextField JTF_pw = new JTextField("");
	public JButton JB_ok = new JButton("Erstellen");
	public JLabel JL_msg = new JLabel("");
	
	public JAR_Configuration con;
	public Menue menue;
	
	public boolean ok = false;
	
	public CreateServer(Menue menue, JAR_Configuration con) {
		super(menue, "Server erstellen", true);	
		
		this.con = con;
		this.menue = menue;
		
		setSize(370, 220);
		setLocationRelativeTo(menue);
		setResizable(false);
		
		JTF_name = new JTextField(con.get("server.name"));
		JTF_port = new JTextField(con.getInt("server.port", 1024, 65535, 2000)+"");
		
		addWindowListener(this);
		
		JPanel JP_name = createPanel("Name", JTF_name);
		JP_name.setBounds(10, 10, 350, 25);
		
		JPanel JP_port = createPanel("Port", JTF_port);
		JP_port.setBounds(10, 40, 350, 25);
		
		JPanel JP_pw = createPanel("Passwort", JTF_pw);
		JP_pw.setBounds(10, 70, 350, 25);
		
		JPanel JP_ok = new JPanel(null);
		JP_ok.add(JB_ok);
		JB_ok.setSize(150, 30);
		JB_ok.addActionListener(this);
		JP_ok.setBounds(100, 105, 150, 50);
		
		JPanel JP_Meldung = new JPanel();
		JP_Meldung.add(JL_msg);
		JP_Meldung.setBounds(20, 160, 300, 50);
		
		JPanel JP_main = new JPanel(null);
		JP_main.add(JP_name);
		JP_main.add(JP_port);
		JP_main.add(JP_pw);
		JP_main.add(JP_ok);
		JP_main.add(JP_Meldung);
		
		add(JP_main);
		
		setVisible(true);
	}
	
	protected JPanel createPanel(String name, Component cmp) {
		JPanel JP = new JPanel(null);
		JLabel JL = new JLabel(name +": ");
		JL.setBounds(0, 0, 80, 25);
		cmp.setBounds(80, 0, 250, 25);
		JP.add(JL);
		JP.add(cmp);
		JP.setBounds(10, 10, 350, 25);
		return JP;
	}
	
	public void actionPerformed(ActionEvent e) {
		int p = 0;
		try {
			p = Integer.parseInt(JTF_port.getText());
			if ( p < 1023 || p > 65535) {
				JL_msg.setText("<html>Port has to be an integer number<br /> between 1023 and 65535");
				return;
			}
		}
		catch(Exception e1) {
			JL_msg.setText("<html>Port has to be an integer number<br /> between 1023 and 65535");
			return;
		}
		if (JTF_name.getText().equals("")) {
			JL_msg.setText("<html>Name must not be empty");
			return;
		}
		if (JTF_pw.getText().equals("")) {
			JL_msg.setText("<html>Password must not be empty");
			return;
		}
		con.change("server.name", JTF_name.getText());
		con.change("server.port", JTF_port.getText());
		ok = true;
		menue.cP.send(AdminConstants.START_SERVER + JTF_name.getText() + AdminConstants.TRENNZEICHEN + JTF_port.getText() + AdminConstants.TRENNZEICHEN + JTF_pw.getText() + ")");
		this.dispose();
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
	}
	
	public void finalize() throws Throwable {
		con.save();
		if(!ok)
			menue.buttonPanel.setServerOn(false);
		super.finalize();
	}

}
