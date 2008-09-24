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

public class FlattenListTest extends LocalWorkerTestCase {

	protected String[] expectedInputNames() {
		return new String[] { "inputlist" };
	}

	protected String[] expectedOutputNames() {
		return new String[] { "outputlist" };
	}

	protected String[] expectedInputTypes() {
		return new String[] { "l(l(''))" };
	}

	protected String[] expectedOutputTypes() {
		return new String[] { "l('')" };
	}

	protected FlattenList getLocalWorker() {
		return new FlattenList();
	}

	public void testDefaultDepth() {
		assertEquals("Default value should be 2", 2, getLocalWorker().getDepth());
	}
	
	public void testSettingDepth() {
		FlattenList l = getLocalWorker();		
		l.setDepth(3);
		assertEquals(3, l.getDepth());
		l.setDepth(1337);
		assertEquals(1337, l.getDepth());
		// Below 2 should not work, should limit to 2
		l.setDepth(1);
		assertEquals(2, l.getDepth());
		l.setDepth(0);
		assertEquals(2, l.getDepth());
		l.setDepth(-1);
		assertEquals(2, l.getDepth());
		l.setDepth(-20);
		assertEquals(2, l.getDepth());
		// But setting to 2 should work
		l.setDepth(3);
		l.setDepth(2);
		assertEquals(2, l.getDepth());
	}
	
	public void testFlattening() throws Exception {
		List<List<String>> list = makeDeepList(0);
	
		List result = runFlattener(getLocalWorker(), list);
		
		assertEquals("incorrect number of entries in the list", 10,
			result.size());
		for (int i = 1; i <= 10; i++) {
			String testVal = String.valueOf(i);
			assertEquals("incorrect value in list", testVal, result.get(i - 1));
		}
	}

	public void testFlatteningDeeper() throws TaskExecutionException {
		List<List<List<String>>> deeperList = makeDeeperList();
		
		FlattenList l = getLocalWorker();
		l.setDepth(3);
		List result = runFlattener(l, deeperList);

		assertEquals("incorrect number of entries in the list", 4*10,
			result.size());
		for (int i = 1; i <= 4*10; i++) {
			String testVal = String.valueOf(i);
			assertEquals("incorrect value in list", testVal, result.get(i - 1));
		}
	}
	
	public void testTooNarrow() throws TaskExecutionException {
		List<List<String>> deepList = makeDeepList(0);
		FlattenList l = getLocalWorker();
		l.setDepth(3);
		// Should still manage, even if list is not that deep
		List result = runFlattener(l, deepList);
		
		assertEquals("incorrect number of entries in the list", 10,
			result.size());
		for (int i = 1; i <= 10; i++) {
			String testVal = String.valueOf(i);
			assertEquals("incorrect value in list", testVal, result.get(i - 1));
		}
	}
	
	public void testNotTooDeep() throws TaskExecutionException {
		List<List<List<String>>> deeperList = makeDeeperList();
		
		// Should only flatten 2 levels by default!
		FlattenList l = getLocalWorker();
		List result = runFlattener(l, deeperList);
		
		assertEquals("incorrect number of entries in the list", 3*4,
			result.size());
	}
	
	public void testXMLExport() throws DataConversionException {
		Element e = getLocalWorker().provideXML();
		assertEquals(XScufl.XScuflNS, e.getNamespace());
		assertEquals("extensions", e.getName());
		Element flattenlist = e.getChild("flattenlist", XScufl.XScuflNS);
		assertNotNull("Element <flattenlist> not found", flattenlist);
		Attribute depth = flattenlist.getAttribute("depth", XScufl.XScuflNS);
		assertNotNull("Attribute depth='' not found", depth);
		assertEquals(2, depth.getIntValue());
	}
	
	public void testXMLImport() {
		FlattenList flattener = getLocalWorker();
		flattener.setDepth(1337);
		Element e = flattener.provideXML();
		
		FlattenList flattener2 = getLocalWorker();
		flattener2.consumeXML(e);
		assertEquals("Did not consume XML correctly", 1337, flattener2.getDepth());
	}
	
	private List runFlattener(FlattenList flattener, List list) throws TaskExecutionException {
		Map<String, DataThing> inputs = new HashMap<String, DataThing>();
		inputs.put("inputlist", new DataThing(list));
		Map out = flattener.execute(inputs);
		DataThing thing = (DataThing) out.get("outputlist");
		List result = (List) thing.getDataObject();
		return result;
	}

	private List<List<List<String>>> makeDeeperList() {
		List<List<List<String>>> list = new ArrayList<List<List<String>>>();
		list.add(makeDeepList(0));
		list.add(makeDeepList(10));
		list.add(makeDeepList(20));
		list.add(makeDeepList(30));
		return list;
	}

	private List<List<String>> makeDeepList(int offset) {
		List<List<String>> list = new ArrayList<List<String>>();
		list.add(makeList(offset+1, offset+3));
		list.add(makeList(offset+4, offset+7));
		list.add(makeList(offset+8, offset+10));
		return list;
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
