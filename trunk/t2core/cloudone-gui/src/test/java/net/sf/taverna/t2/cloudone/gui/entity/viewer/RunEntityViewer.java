package net.sf.taverna.t2.cloudone.gui.entity.viewer;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.WindowConstants;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.EmptyListException;
import net.sf.taverna.t2.cloudone.datamanager.MalformedListException;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.UnsupportedObjectTypeException;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.gui.entity.viewer.EntityViewer;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;

public class RunEntityViewer {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		DataManager dataManager = new InMemoryDataManager("mem1", Collections
				.<LocationalContext> emptySet());
		DataFacade facade = new DataFacade(dataManager);

		HashSet<ReferenceScheme> refSchemes = new HashSet<ReferenceScheme>();
		URL url1 = new URL("http://taverna.sourceforge.net/");
		refSchemes.add(new HttpReferenceScheme(url1));
		URL url2 = new URL("http://www.mygrid.org.uk/");
		refSchemes.add(new HttpReferenceScheme(url2));
		URL url3 = new URL("http://mygrid.org.uk/");

		refSchemes.add(new HttpReferenceScheme(url3));

		DataDocumentIdentifier identifier = (DataDocumentIdentifier) dataManager
				.registerDocument(refSchemes);

		BlobReferenceScheme<?> blobReferenceScheme = dataManager.getBlobStore()
				.storeFromString("qwertyuiopasdfdghghjkdfgdbkdfbkbfdfgorg");

		HashSet<ReferenceScheme> blobRefs = new HashSet<ReferenceScheme>();
		blobRefs.add(blobReferenceScheme);
		DataDocumentIdentifier blobIdentifier = (DataDocumentIdentifier) dataManager
				.registerDocument(blobRefs);

		List<Object> listOfList = new ArrayList<Object>();
		ArrayList<Object> firstList = new ArrayList<Object>();
		firstList.add("Anything");
		firstList.add("it doesn't matter");
		firstList.add("anymore");
		firstList.add(identifier);

		listOfList.add(firstList);

		Throwable throwable = new Throwable("failure", new Exception(
				"total failure", new Exception("asfzaf")));
		EntityIdentifier errorId = dataManager.registerError(1, 0,
				"did not work", throwable);
		listOfList.add(errorId);

		ArrayList<Object> thirdList = new ArrayList<Object>();
		thirdList.add("Something else");
		thirdList.add("that matters");
		thirdList.add(1337);
		thirdList.add(true);
		thirdList.add(blobIdentifier);
		listOfList.add(thirdList);

		ArrayList<Object> superList = new ArrayList<Object>();
		superList.add(listOfList);
		superList.add(listOfList);

		EntityIdentifier strings = facade.register(superList);
		EntityViewer frame = new EntityViewer(dataManager, strings);
		frame.setSize(new Dimension(300, 300));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}
}
