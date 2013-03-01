package Admin;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import Configuration.JAR_Configuration;
/**
 * @author Marcel Kramer
 */
public class SendPrivateMessage extends JDialog implements ActionListener {
	
	public JTextArea JTA_msg;
	public JButton JB_ok;
	public JButton JB_abort;
	public JScrollPane JSP_msg;
	
	public Menue menue;
	protected String msg;
	
	public boolean ok = false;
	
	public SendPrivateMessage(Menue menue, String ip, String name) {
		super(menue, "Nachricht an "+ name + " an " + ip + " senden", true);	
		
		this.menue = menue;
		
		msg = AdminConstants.SEND_MSG_TO_USER;
		msg += ip;
		msg += AdminConstants.TRENNZEICHEN;
		msg += name;
		msg += AdminConstants.TRENNZEICHEN;
		
		setSize(500, 500);
		setLocationRelativeTo(menue);
		setResizable(false);
		
		setLayout(new BorderLayout(3, 3));
		JTA_msg = new JTextArea();
		
		JSP_msg = new JScrollPane(JTA_msg);
		
		
		
		JPanel JP_Buttons = new JPanel();
		JP_Buttons.setLayout(new BoxLayout(JP_Buttons, BoxLayout.X_AXIS));
		
		JB_abort = new JButton("Abbrechen");
		JB_abort.setActionCommand("abort");
		JP_Buttons.add(JB_abort);
		JB_abort.addActionListener(this);
		
		JB_ok = new JButton("Senden");
		JB_ok.setActionCommand("ok");
		JP_Buttons.add(JB_ok);
		JB_ok.addActionListener(this);
		
		add(JSP_msg, BorderLayout.CENTER);
		add(JP_Buttons, BorderLayout.SOUTH);
		
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		
		//menue.cP.send(AdminConstants.START_SERVER + JTF_name.getText() + AdminConstants.TRENNZEICHEN + JTF_port.getText() + AdminConstants.TRENNZEICHEN + JTF_pw.getText() + ")");
		this.dispose();
	}

}
