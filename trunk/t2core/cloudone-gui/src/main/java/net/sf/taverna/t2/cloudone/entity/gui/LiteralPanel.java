package net.sf.taverna.t2.cloudone.entity.gui;


import javax.swing.JComponent;
import javax.swing.JLabel;

import net.sf.taverna.t2.cloudone.entity.Literal;

public class LiteralPanel extends AbstractEntityPanel {
	private static final long serialVersionUID = 1L;
	private Literal id;

	public LiteralPanel(Literal id) {
		this.id = id;
		buildPanel();
	}

	@Override
	public JComponent createHeader() {
		String type = id.getValueType().getSimpleName();
		JLabel valueLabel = new JLabel("<html><small>" + type + "</small> "
				+ id.getValue().toString());
		return valueLabel;
	}

}
