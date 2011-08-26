package org.embl.ebi.escience.scuflworkers.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.XScufl;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

public class SliceListTest extends LocalWorkerTestCase {

	protected String[] expectedInputNames() {
		return new String[] { "inputlist", "fromindex", "toindex"};
	}

	protected String[] expectedOutputNames() {
		return new String[] { "outputlist" };
	}

	protected String[] expectedInputTypes() {
		return new String[] { "l('')", LocalWorker.STRING, LocalWorker.STRING };
	}

	protected String[] expectedOutputTypes() {
		return new String[] { "l('')" };
	}

	protected SliceList getLocalWorker() {
		return new SliceList();
	}

	public void testDefaultDepth() {
		assertEquals("Default value should be 1", 1, getLocalWorker().getDepth());
	}
	
	public void testSettingDepth() {
		SliceList l = getLocalWorker();		
		l.setDepth(3);
		assertEquals(3, l.getDepth());
		l.setDepth(1337);
		assertEquals(1337, l.getDepth());
		// Below 1 should not work, should limit to 1
		l.setDepth(0);
		assertEquals(1, l.getDepth());
		l.setDepth(-1);
		assertEquals(1, l.getDepth());
		l.setDepth(-20);
		assertEquals(1, l.getDepth());
		l.setDepth(3);
		l.setDepth(2);
		assertEquals(2, l.getDepth());
	}
	
	public void testSlicing() throws Exception {
		List<String> list = makeList(1, 3);
	
		List result = runSlicer(getLocalWorker(), list, "0", "2");
		
		assertEquals("incorrect number of entries in the list", 2,
			result.size());
		for (int i = 1; i <= 2; i++) {
			String testVal = String.valueOf(i);
			assertEquals("incorrect value in list", testVal, result.get(i - 1));
		}
		
		try {
			result = runSlicer(getLocalWorker(), list, "-2", "2");
			fail("TaskExecutionException expected");
		}
		catch (TaskExecutionException e) {
			// This is expected
		}
		try {
			result = runSlicer(getLocalWorker(), list, "1", "100");
			fail("TaskExecutionException expected");
		}
		catch (TaskExecutionException e) {
			// This is expected
		}
		try {
			result = runSlicer(getLocalWorker(), list, "1", "0");
			fail("TaskExecutionException expected");
		}
		catch (TaskExecutionException e) {
			// This is expected
		}
		try {
			result = runSlicer(getLocalWorker(), list, "hello", "2");
			fail("TaskExecutionException expected");
		}
		catch (TaskExecutionException e) {
			// This is expected
		}
		try {
			result = runSlicer(getLocalWorker(), list, "0", "world");
			fail("TaskExecutionException expected");
		}
		catch (TaskExecutionException e) {
			// This is expected
		}
	}
	
	public void testXMLExport() throws DataConversionException {
		Element e = getLocalWorker().provideXML();
		assertEquals(XScufl.XScuflNS, e.getNamespace());
		assertEquals("extensions", e.getName());
		Element slicelist = e.getChild("slicelist", XScufl.XScuflNS);
		assertNotNull("Element <slicelist> not found", slicelist);
		Attribute depth = slicelist.getAttribute("depth", XScufl.XScuflNS);
		assertNotNull("Attribute depth='' not found", depth);
		assertEquals(1, depth.getIntValue());
	}
	
	public void testXMLImport() {
		SliceList slicer = getLocalWorker();
		slicer.setDepth(1337);
		Element e = slicer.provideXML();
		
		SliceList slicer2 = getLocalWorker();
		slicer2.consumeXML(e);
		assertEquals("Did not consume XML correctly", 1337, slicer2.getDepth());
	}
	
	private List runSlicer(SliceList slicer, List list, String fromString, String toString) throws TaskExecutionException {
		Map<String, DataThing> inputs = new HashMap<String, DataThing>();
		inputs.put("inputlist", new DataThing(list));
		inputs.put("fromindex", new DataThing(fromString));
		inputs.put("toindex", new DataThing(toString));
		Map out = slicer.execute(inputs);
		DataThing thing = (DataThing) out.get("outputlist");
		List result = (List) thing.getDataObject();
		return result;
	}

	/**
	 * Generate a list of strings within the range, inclusive.
	 * <p>
	 * For instance, makeList(1,3) would return:
	 * <pre>
	 * ["1", "2", "3"]
	 * </pre>
	 * 
	 * @param start minimum entry
	 * @param stop maximum entry
	 * @return A List<String> of (stop-start)+1 items
	 */
	private List<String> makeList(int start, int stop) {
		List<String> list = new ArrayList<String>();
		for (int i=start; i<=stop; i++) {
			list.add(Integer.toString(i));
		}
		return list;
	}
	
	

}
