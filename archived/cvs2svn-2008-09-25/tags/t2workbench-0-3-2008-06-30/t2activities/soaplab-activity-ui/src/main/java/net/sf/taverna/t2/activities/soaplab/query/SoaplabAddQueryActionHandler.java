package net.sf.taverna.t2.activities.soaplab.query;

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.partition.AddQueryActionHandler;

@SuppressWarnings("serial")
public class SoaplabAddQueryActionHandler extends AddQueryActionHandler {

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String url = JOptionPane.showInputDialog(null,"Soaplab base location","Soaplab Location",JOptionPane.INFORMATION_MESSAGE);
		if (url!=null) {
			SoaplabQuery query = new SoaplabQuery(url);
			addQuery(query);
		}
	}

	@Override
	protected Icon getIcon() {
		return new ImageIcon(SoaplabAddQueryActionHandler.class.getResource("/soaplab.png"));
	}

	@Override
	protected String getText() {
		return "Add Soaploab query...";
	}

}
