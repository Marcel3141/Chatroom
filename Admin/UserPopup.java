package Admin;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.Component;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;


/**
 * @author Marcel Kramer
 */
public class UserPopup extends JPopupMenu {
	
	protected JMenuItem JMI_send;
	protected JMenuItem JMI_lock;
	
	public UserPopup(ActionListener aL, String actionCommand) {
		
		JMI_send = new JMenuItem("send private message");
		JMI_lock = new JMenuItem("lock this IP");
		
		JMI_send.addActionListener(aL);
		JMI_lock.addActionListener(aL);
		
		JMI_send.setActionCommand(AdminConstants.SEND_MSG_TO_USER + actionCommand);
		JMI_lock.setActionCommand(AdminConstants.KILL + actionCommand);
		
		add(JMI_send);
		add(JMI_lock);
		
	}
	
}