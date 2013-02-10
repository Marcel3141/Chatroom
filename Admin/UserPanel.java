package Admin;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;

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

	protected Menue menue = null;
	
	public final int hight = 25;
	
	public UserPanel(Menue menue) {
		this.menue = menue;
		
		add(new JPanel());
	}
	
	protected Component createTable(String[] nArray, String[] ipArray) {
		JPanel names = new JPanel();
		JPanel ips = new JPanel();
		JPanel kill = new JPanel();
		
		names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
		ips.setLayout(new BoxLayout(ips, BoxLayout.Y_AXIS));
		kill.setLayout(new BoxLayout(kill, BoxLayout.Y_AXIS));
		{
			JPanel tmp1 = new JPanel();
			JPanel tmp2 = new JPanel();
			JPanel tmp3 = new JPanel();
			tmp1.setLayout(new BoxLayout(tmp1, BoxLayout.X_AXIS));
			tmp1.add(Box.createRigidArea(new Dimension(35,hight)));
			tmp1.add(new JLabel("Name"));
			tmp1.add(Box.createRigidArea(new Dimension(35,hight)));
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.add(Box.createRigidArea(new Dimension(35,hight)));
			tmp2.add(new JLabel("IP-Adresse"));
			tmp2.add(Box.createRigidArea(new Dimension(35,hight)));
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.add(Box.createRigidArea(new Dimension(10,hight)));
			tmp3.add(new JLabel(""));
			tmp3.add(Box.createRigidArea(new Dimension(10,hight)));
			names.add(tmp1);
			ips.add(tmp2);
			kill.add(tmp3);
		}
		
		for (int i = 0; i < nArray.length; i++) {
			JPanel tmp1 = new JPanel();
			JPanel tmp2 = new JPanel();
			JPanel tmp3 = new JPanel();
			tmp1.setLayout(new BoxLayout(tmp1, BoxLayout.X_AXIS));
			tmp1.add(Box.createRigidArea(new Dimension(1,hight)));
			tmp1.add(new JLabel(nArray[i]));
			tmp1.add(Box.createRigidArea(new Dimension(1,hight)));
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.add(Box.createRigidArea(new Dimension(1,hight)));
			tmp2.add(new JLabel(ipArray[i]));
			tmp2.add(Box.createRigidArea(new Dimension(1,hight)));
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.add(Box.createRigidArea(new Dimension(1,hight)));
			JButton JB = new JButton("Kill User");
			JB.setActionCommand(ipArray[i] + AdminConstants.TRENNZEICHEN + nArray[i]);
			JB.addActionListener(this);
			tmp3.add(JB);
			tmp3.add(Box.createRigidArea(new Dimension(1,hight)));
			
			names.add(tmp1);
			ips.add(tmp2);
			kill.add(tmp3);
		}
		
		JPanel all = new JPanel();
		all.setLayout(new BoxLayout(all, BoxLayout.X_AXIS));
		all.add(names);
		all.add(ips);
		all.add(kill);
		
		JScrollPane scroll = new JScrollPane(all);
		
		return scroll;
	}
	
	public void updateInformation(String[] names, String[] ips) {
		Component cmp;
		if(names == null || ips == null || names.length == 0 || ips.length == 0)
			cmp = new JLabel("No Users Online");
		else 
			cmp = createTable(names, ips);
		this.remove(0);
		this.add(cmp);
		menue.doUpdate();
	}
	
	public void actionPerformed(ActionEvent e) {
		menue.cP.send(AdminConstants.KILL + e.getActionCommand() + ")");
	}
	
}