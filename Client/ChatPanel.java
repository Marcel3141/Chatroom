package Client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ChatProtokoll.ChatProtokoll;
import ChatProtokoll.ChatConstants;
import Configuration.ReadWriteFile;

/**
 * @author Marcel Kramer
 * @serial 0.1
 */
public class ChatPanel extends JPanel implements ActionListener {
	
	protected ChatProtokoll cP;
	
	protected JLabel JTA_msg;
	protected JScrollPane JSP_msg;
	protected JTextField JTF_text;
	
	public Vector<String> HTML_LIST;
	
	protected boolean chatEnabled;
	protected boolean connected;
	
	public ChatPanel(ChatProtokoll cP) {
		
		this.cP = cP;
		chatEnabled = true;
		connected = true;
		
		createLayout();
		
		HTML_LIST = new Vector<String>();
		
		Vector<String> htmlList =
			ReadWriteFile.readFileInJAR("HTML.ini", "ConfigurationFiles");
		for (String s: htmlList) {
			if (!s.equals(""))
				HTML_LIST.add(s);
		}
	}
	
	public ChatPanel() {
		
		chatEnabled = false;
		connected = false;
		
		createLayout();
		
		HTML_LIST = new Vector<String>();
		
		Vector<String> htmlList =
			ReadWriteFile.readFileInJAR("HTML.ini", "ConfigurationFiles");
		for (String s: htmlList) {
			if (!s.equals(""))
				HTML_LIST.add(s);
		}
	}
	
	protected void createLayout() {
		this.setLayout(new BorderLayout(3, 3));
		JTA_msg = new JLabel("<html>" +
			(chatEnabled ? "" : "Chat is not connected"));
		JTA_msg.setVerticalAlignment(SwingConstants.TOP);
		JSP_msg = new JScrollPane(JTA_msg);
		this.add(JSP_msg, BorderLayout.CENTER);
		
		JTF_text = new JTextField();
		JTF_text.setEnabled(chatEnabled);
		JTF_text.addActionListener(this);
		this.add(JTF_text, BorderLayout.SOUTH);
		
		this.setVisible(true);
	}
	
	public void setChatProtokoll(ChatProtokoll cP) {
		if (!chatEnabled) {
			this.cP = cP;
			JTA_msg.setText("<html><b>"
				+ (cP.isReadyToChat() ? ("Connected to" + cP.getName() + "</b>")
				: "<b>Chat is not connected</b>"));
		}
	}
	
	public void setChatEnabled(boolean b) {
		if (chatEnabled != b) {
			chatEnabled = b;
			if (b && !connected)
				setConnected(true);
			else {
				JTA_msg.setText("<html><b>Chat has been "
					+ (b ? "en" : "dis") + "abled</b><br />"
					+ JTA_msg.getText().substring(6));
				JTF_text.setEnabled(b);
			}
		}
	}
	
	public void setConnected(boolean b) {
		if (connected != b) {
			connected = b;
			chatEnabled = b;
			JTA_msg.setText("<html><b>Chat has been " + (b ? "" : "dis") 
				+ "connected to "+ (b ? "" : cP.getName()) + "</b><br />"
				+ JTA_msg.getText().substring(6));
			JTF_text.setEnabled(b);
		}
	}
	
	public void reset() {
		JTA_msg.setText("<html>");
	}
	
	public void recive(String in) {
		if (! connected)
			return;
		if (in.contains(ChatConstants.TRENNZEICHEN)) {
			String[] param = in.split(ChatConstants.TRENNZEICHEN);
			in = param[1] + ": " + param[0];
		}
		else {
			in = cP.getName() + ": " + in;
		}
		in = in.replaceAll(ChatConstants.SLASH, "/");
		in = checkHTML(in);
		JTA_msg.setText("<html>"+in+"<br />"+JTA_msg.getText().substring(6));
	}
	
	public String checkHTML(String str) {
		Vector<String> ende = new Vector<String>();
		for (int i=0; i<str.length(); i++) {
			if (str.charAt(i)=='<') {
				boolean ok = false;
				for (String s: HTML_LIST) {
					if (str.length()>i+1 
						&& (str.substring(i+1).startsWith(s+">")
						|| str.substring(i+1).startsWith(s+" "))) 
					{
						ok=true;
						if(!s.endsWith("/"))
							ende.add(0, s);
						break;
					}
					
					if (str.length()>i+1
						&& str.substring(i+1).startsWith("/"+s+">")) {
						if ( !ende.isEmpty() && s.equals(ende.firstElement())){
							ende.remove(0);
							ok=true;
						}
						break;
					}
				}
				if (!ok) {
					str = ( str.substring(0, i) + "&lt;" + str.substring(i+1));
					i+=3;
				}
			}
    	}
    	for (String s: ende) str+= "</"+s+">";
		return str;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (!JTF_text.getText().equals("")) {
			if (cP.isReadyToChat()) {
				String s = JTF_text.getText();
				JTA_msg.setText("<html>You: " + s + "<br />"
					+ JTA_msg.getText().substring(6) );
				s = s.replaceAll("/", ChatConstants.SLASH);
				cP.send(s);
			}
			else {
				setConnected(false);
			}
		}
		JTF_text.setText("");
	}

}
