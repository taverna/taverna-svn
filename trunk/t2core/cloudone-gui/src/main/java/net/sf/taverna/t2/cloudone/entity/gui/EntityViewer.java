package net.sf.taverna.t2.cloudone.entity.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.EmptyListException;
import net.sf.taverna.t2.cloudone.datamanager.MalformedListException;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.UnsupportedObjectTypeException;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;

public class EntityViewer extends JFrame {

	private EntityIdentifier parentID;
	private JScrollPane jScrollPane1;
	private DataManager dataManager;

	public EntityViewer(DataManager dataManager, EntityIdentifier id)
			throws RetrievalException, NotFoundException {
		this.dataManager = dataManager;
		parentID = id;
		initialise(id);
	}

	private void initialise(EntityIdentifier id) throws RetrievalException,
			NotFoundException {
		AbstractEntityPanel entityPanels = getPanelForEntity(dataManager, id);
		JPanel verticalFiller = new JPanel();
		verticalFiller.setMinimumSize(new Dimension(0,0));
		//verticalFiller.setBorder(BorderFactory.createLineBorder(Color.RED));
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.1;
		c.weightx = 0.1;
		entityPanels.add(verticalFiller, c); // vertical filler
		
		entityPanels.setDetailsVisible(true);
		jScrollPane1 = new JScrollPane(entityPanels);
		// add the panel to the parent panel
		add(jScrollPane1);
	}

	public static AbstractEntityPanel getPanelForEntity(DataManager dataManager,
			EntityIdentifier id) throws RetrievalException, NotFoundException {
		IDType type = id.getType();
		AbstractEntityPanel panel;
		if (type.equals(IDType.Data)) {
			panel = new DataDocumentPanel(dataManager,
					(DataDocumentIdentifier) id);
		} else if (type.equals(IDType.Error)) {
			panel = new ErrorDocumentPanel(dataManager,
					(ErrorDocumentIdentifier) id);
		} else if (type.equals(IDType.List)) {
			panel = new EntityListPanel(dataManager, (EntityListIdentifier) id);
		} else if (type.equals(IDType.Literal)) {
			panel = new LiteralPanel((Literal) id);
		} else {
			throw new IllegalArgumentException("Unknown entity type " + type
					+ " for " + id);
		}
	
		return panel;
	}

	
}
