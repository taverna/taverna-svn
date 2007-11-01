package net.sf.taverna.t2.cloudone.entity.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;

public class EntityListPanel extends AbstractEntityPanel {

	private static Logger logger = Logger.getLogger(EntityListPanel.class);
	private EntityList entitylist;
	private final DataManager dataManager;
	private final EntityListIdentifier id;
	private ArrayList<AbstractEntityPanel> childPanels = new ArrayList<AbstractEntityPanel>();

	public EntityListPanel(DataManager dataManager, EntityListIdentifier id)
			throws RetrievalException, NotFoundException {
		this.dataManager = dataManager;
		this.id = id;
		entitylist = (EntityList) dataManager.getEntity(id);
		buildPanel();
	}

	@Override
	public void setDetailsVisible(boolean visible) {
		super.setDetailsVisible(visible);
		if (!visible) {
			// Also collapse children
			for (AbstractEntityPanel childPanel : childPanels) {
				childPanel.setDetailsVisible(visible);
			}
		}
	}

	@Override
	public JComponent createHeader() {
		JLabel header = new JLabel("<html><b>List</b>, " + entitylist.size()
				+ " elements <small>" + id
				+ " <a href='#'>(details)</a></small></html>");
		return header;
	}

	@Override
	public JComponent createDetails() {
		JPanel details = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		JPanel indentationFiller = new JPanel();
		details.add(indentationFiller, c); // indentation filler
		indentationFiller.setBorder(BorderFactory.createLineBorder(Color.YELLOW));
		
		c.gridx = 1;
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 0.1;
		
		childPanels.clear();
		for (EntityIdentifier entityID : entitylist) {
			try {
				AbstractEntityPanel panel = EntityViewer.getPanelForEntity(dataManager, entityID);
				details.add(panel, c);
				childPanels.add(panel);
			} catch (RetrievalException e) {
				logger.warn("Cannot retrieve " + entityID, e);
				details.add(new JLabel("Cannot retrieve " + entityID));
			} catch (NotFoundException e) {
				logger.warn("Cannot find " + entityID, e);
				details.add(new JLabel("Cannot find " + entityID));
			}

		}
		return details;
	}

}
