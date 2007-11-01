package net.sf.taverna.t2.cloudone.entity.gui;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;

public class RunErrorViewer {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnsupportedObjectTypeException 
	 * @throws MalformedListException 
	 * @throws EmptyListException 
	 * @throws NotFoundException 
	 * @throws RetrievalException 
	 */
	public static void main(String[] args) throws EmptyListException, MalformedListException, UnsupportedObjectTypeException, IOException, RetrievalException, NotFoundException {
		InMemoryDataManager dataManager = new InMemoryDataManager("mem1", Collections
				.<LocationalContext> emptySet());

		Throwable throwable = new Throwable("failure", new Exception(
				"total failure", new Exception("asfzaf")));
		EntityIdentifier identifier = dataManager.registerError(1, 0, "did not work", throwable);
		
		EntityViewer frame = new EntityViewer(dataManager, identifier);
		frame.setSize(new Dimension(300, 300));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

}
