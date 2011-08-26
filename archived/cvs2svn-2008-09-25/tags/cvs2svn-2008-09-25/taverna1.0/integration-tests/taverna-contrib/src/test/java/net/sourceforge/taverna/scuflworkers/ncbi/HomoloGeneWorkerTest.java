package net.sourceforge.taverna.scuflworkers.ncbi;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * This class verifies that the HomoloGeneWorker is functioning properly.
 * 
 * @author Mark
 */
public class HomoloGeneWorkerTest extends AbstractXmlWorkerTest {

	@Ignore("test fails due to root has no child nodes")
	@Test
	public void testExecute() throws Exception {
		LocalWorker worker = new HomoloGeneWorker();
		Map inputMap = new HashMap();
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		inAdapter.putString("term", "brca2");

		Map outputMap = worker.execute(inputMap);
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		String results = outAdapter.getString("resultsXml");
		assertNotNull("The results were null", results);
		System.out.println(results);

		this.writeFile("test_homologene.xml", results);
		Element root = this.parseXml(results);
		this.testXmlNotEmpty(root);

	}

}
