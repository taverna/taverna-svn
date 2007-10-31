package net.sf.taverna.t2.cloudone.entity.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;

public class EntityListPanel extends JPanel {

	public EntityListPanel(DataManager dataManager, EntityListIdentifier id)
			throws RetrievalException, NotFoundException {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		EntityList entitylist = (EntityList) dataManager.getEntity(id);

		JLabel header = new JLabel("<html><b>List</b>, " + entitylist.size()
				+ " elements <small>" + id + "</small></html>");

		c.gridx = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		add(header, c);
		
		c.gridwidth = 1;
		add(new JPanel(), c); // indention filler

		c.gridx = 1;
		c.weightx = 0.1;
		c.fill = GridBagConstraints.HORIZONTAL;
		for (EntityIdentifier entityID : entitylist) {
			JPanel panel = EntityViewer.getPanelForEntity(dataManager,
					entityID);
			add(panel, c);
		}

		c.weighty = 0.1;
		add(new JPanel(), c); // vertical filler

	}

}
