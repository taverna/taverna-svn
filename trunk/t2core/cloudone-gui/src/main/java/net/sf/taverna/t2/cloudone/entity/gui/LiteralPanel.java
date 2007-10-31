package net.sf.taverna.t2.cloudone.entity.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.entity.Literal;

public class LiteralPanel extends JPanel {

	public LiteralPanel(Literal id) {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();		
		JLabel valueLabel = new JLabel(id.getValue().toString());
		add(valueLabel, c);

		// Filler
		c.weightx = 0.1;
		add(new JPanel(), c);		
		c.weightx = 0.0;
		
		String type = id.getValueType().getSimpleName();
		JLabel typeLabel = new JLabel("<html>  <small>" + type + "</small></html>");
		add(typeLabel, c);
		
	}

}
