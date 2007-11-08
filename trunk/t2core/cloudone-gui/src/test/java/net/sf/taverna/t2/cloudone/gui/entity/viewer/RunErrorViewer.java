package net.sf.taverna.t2.cloudone.gui.entity.viewer;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Collections;

import javax.swing.WindowConstants;

import net.sf.taverna.t2.cloudone.datamanager.EmptyListException;
import net.sf.taverna.t2.cloudone.datamanager.MalformedListException;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.UnsupportedObjectTypeException;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;

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
	public static void main(String[] args) throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		InMemoryDataManager dataManager = new InMemoryDataManager("mem1",
				Collections.<LocationalContext> emptySet());

		Throwable throwable = new Throwable("failure", new Exception(
				"total failure", new Exception("asfzaf")));
		EntityIdentifier identifier = dataManager.registerError(1, 0,
				"did not work", throwable);

		EntityViewer frame = new EntityViewer(dataManager, identifier);
		frame.setSize(new Dimension(300, 300));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

}
