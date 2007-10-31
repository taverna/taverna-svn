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
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

public class RunEntityViewer {
	
	public static void main(String[] args) throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		DataManager dataManager = new InMemoryDataManager("mem1", Collections
				.<LocationalContext> emptySet());
		DataFacade facade = new DataFacade(dataManager);
		List<List<Object>> listOfList = new ArrayList<List<Object>>();
		ArrayList<Object> firstList = new ArrayList<Object>();
		firstList.add("Anything");
		firstList.add("it doesn't matter");
		firstList.add("anymore");

		listOfList.add(firstList);

		ArrayList<Object> secondList = new ArrayList<Object>();
		secondList.add("Something else");
		secondList.add("that matters");
		secondList.add(1337);
		secondList.add(true);

		
		listOfList.add(secondList);

		EntityIdentifier strings = facade.register(listOfList);
		EntityViewer frame = new EntityViewer(dataManager, strings);
		frame.setSize(new Dimension(300, 300));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
