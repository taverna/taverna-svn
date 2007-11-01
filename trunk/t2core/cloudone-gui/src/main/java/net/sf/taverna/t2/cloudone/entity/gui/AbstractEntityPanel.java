package net.sf.taverna.t2.cloudone.entity.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

public abstract class AbstractEntityPanel extends JPanel {

	public class ToggleDetailsMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (details != null) {
				// flip visible setting
				setDetailsVisible(! isDetailsVisible());
			}
		}
	}

	private JComponent details;
	private JComponent header;

	
	public AbstractEntityPanel() {
	}

	public abstract JComponent createHeader();

	public JComponent createDetails() {
		return null;
	}
	
	public void setDetailsVisible(boolean visible) {
		if (details == null) {
			return;
		}
		details.setVisible(visible);
	}
	
	public boolean isDetailsVisible() {
		return details.isVisible();
	}
	
	public void buildPanel() {
		setLayout(new GridBagLayout());
		
		// For debugging, add ugly border:
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		GridBagConstraints c = new GridBagConstraints();

		header = createHeader();
		header.addMouseListener(new ToggleDetailsMouseListener());
		header.setBorder(BorderFactory.createLineBorder(Color.CYAN));
		
		c.gridx = 0;
		c.weightx = 0.1;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(header, c);

		buildDetails();
	}

	protected void buildDetails() {
		details = createDetails();
		if (details == null) {
			return;
		}
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		details.setVisible(false);
		details.setBorder(BorderFactory.createLineBorder(Color.GREEN));
		add(details, c);

	}

	public JLabel createLabel(String type, EntityIdentifier id) {
		return new JLabel(
				"<html><b>" + type + "</b> <small>" + id
						+ " <a href='#'>(details)</a></small></html>");
	}

}
