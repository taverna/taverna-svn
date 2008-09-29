package net.sf.taverna.t2.activities.wsdl.query;

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.partition.AddQueryActionHandler;

@SuppressWarnings("serial")
public class WSDLAddQueryActionHandler extends AddQueryActionHandler {
	

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String wsdl = JOptionPane.showInputDialog(null,"Address of the WSDL document","WSDL location",JOptionPane.INFORMATION_MESSAGE);
		if (wsdl!=null) {
			WSDLQuery query = new WSDLQuery(wsdl);
			addQuery(query);
		}
	}

	@Override
	protected Icon getIcon() {
		return new ImageIcon(WSDLAddQueryActionHandler.class.getResource("/wsdl.png"));
	}

	@Override
	protected String getText() {
		return "WSDL...";
	}

}
