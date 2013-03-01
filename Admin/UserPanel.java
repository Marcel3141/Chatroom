package Admin;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Component;
import java.awt.Color;
import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
/**
 * @author Marcel Kramer
 * @serial 0.1
 */
public class UserPanel extends JPanel implements ActionListener {

	protected Menue menue;
	
	int w1 = 5;
	int w2 = 5;
	
	public final int hight = 25;
	
	public UserPanel(Menue menue) {
		this.menue = menue;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		int w = new JScrollPane().getVerticalScrollBar().getMaximumSize().width;
		
		{
			JLabel t = new JLabel("T");
			w1 += t.getFontMetrics(t.getFont()).stringWidth("mmmmmmmm");
			w2 += t.getFontMetrics(t.getFont()).stringWidth("777.777.777.777");
		}
		
		add(Box.createRigidArea(new Dimension(w1+w2+w+7,1)));
		add(new JLabel("No Users Online"));
	}
	
	protected JPanel createLine(String text1, String text2, int maxWidth1, int maxWidth2, Color color) {
		JPanel tmp = new JPanel();
		tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
		
		JLabel JL_tmp1 = new JLabel(text1);
		JLabel JL_tmp2 = new JLabel(text2);
		
		int w1 = maxWidth1 - JL_tmp1.getFontMetrics(JL_tmp1.getFont()).stringWidth(text1);
		int w2 = maxWidth2 - JL_tmp2.getFontMetrics(JL_tmp2.getFont()).stringWidth(text2);
		
		tmp.add(Box.createRigidArea(new Dimension(1,hight)));
		tmp.add(JL_tmp1);
		if (w1 >= 1)
			tmp.add(Box.createRigidArea(new Dimension(w1,hight)));
		tmp.add(Box.createRigidArea(new Dimension(3,hight)));
		tmp.add(JL_tmp2);
		if (w2 >= 1)
			tmp.add(Box.createRigidArea(new Dimension(w2,hight)));
		tmp.setBackground(color);
		
		return tmp;
	}
	
	protected Component createTable(String[] nArray, String[] ipArray) {
		JPanel all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
		
		all.add(createLine("Name", "IP-Adresse", w1, w2, Color.GRAY));
		
		for (int i = 0; i < nArray.length; i++) {
			JPanel tmp = createLine(nArray[i], ipArray[i], w1, w2, (i%2==0 ? Color.WHITE : Color.LIGHT_GRAY));
			UserPopup userPopup = new UserPopup(this, ipArray[i] + AdminConstants.TRENNZEICHEN + nArray[i]);
			tmp.setComponentPopupMenu(userPopup);
			
			all.add(tmp);
		}
		
		JScrollPane scroll = new JScrollPane(all);
		
		return scroll;
	}
	
	public void updateInformation(String[] names, String[] ips) {
		Component cmp;
		if(names == null || ips == null || names.length == 0 || ips.length == 0)
			cmp = new JLabel("No Users Online");
		else 
			cmp = createTable(names, ips);
		this.remove(1);
		this.add(cmp);
		menue.doUpdate();
	}
	
	public void actionPerformed(ActionEvent e) {
		String com = e.getActionCommand();
		if (com.startsWith(AdminConstants.SEND_MSG_TO_USER)) {
			com = com.substring(AdminConstants.SEND_MSG_TO_USER.length());
			String[] param = com.split(AdminConstants.TRENNZEICHEN);
			new SendPrivateMessage(menue, param[0], param[1]);
		}
		else if (com.startsWith(AdminConstants.KILL)) {
			menue.cP.send(com + ")");
		}
	}
	
}