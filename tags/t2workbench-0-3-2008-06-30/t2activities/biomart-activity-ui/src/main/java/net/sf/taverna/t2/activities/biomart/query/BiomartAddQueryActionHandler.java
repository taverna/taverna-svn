package net.sf.taverna.t2.activities.biomart.query;

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.partition.AddQueryActionHandler;

@SuppressWarnings("serial")
public class BiomartAddQueryActionHandler extends AddQueryActionHandler {

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String location = JOptionPane.showInputDialog(null,"Enter the biomart location","Biomart Location",JOptionPane.INFORMATION_MESSAGE);
		if (location!=null) {
			BiomartQuery query = new BiomartQuery(location);
			addQuery(query);
		}
	}

	@Override
	protected Icon getIcon() {
		return new ImageIcon(BiomartAddQueryActionHandler.class.getResource("/biomart.png"));
	}

	@Override
	protected String getText() {
		return "Add new Biomart location ...";
	}

}
