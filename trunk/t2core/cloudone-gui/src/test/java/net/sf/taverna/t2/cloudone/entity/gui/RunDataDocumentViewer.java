package net.sf.taverna.t2.cloudone.entity.gui;

import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.WindowConstants;

import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.impl.DataDocumentImpl;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.impl.http.HttpReferenceScheme;

public class RunDataDocumentViewer {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 * @throws NotFoundException 
	 * @throws RetrievalException 
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws MalformedURLException, RetrievalException, NotFoundException {
		InMemoryDataManager dataManager = new InMemoryDataManager("mem1", Collections
				.<LocationalContext> emptySet());

		HashSet<ReferenceScheme> refSchemes = new HashSet<ReferenceScheme>();
		URL url1 = new URL("http://taverna.sourceforge.net/");
		refSchemes.add(new HttpReferenceScheme(url1));
		URL url2 = new URL("http://www.mygrid.org.uk/");
		refSchemes.add(new HttpReferenceScheme(url2));
		URL url3 = new URL("http://mygrid.org.uk/");

		refSchemes.add(new HttpReferenceScheme(url3));
		
		DataDocumentIdentifier identifier = (DataDocumentIdentifier) dataManager.registerDocument(refSchemes);
		
		EntityViewer frame = new EntityViewer(dataManager, identifier);
		frame.setSize(new Dimension(300, 300));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

}
