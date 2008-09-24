package net.sourceforge.taverna.scuflworkers.ncbi;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.TransmitterException;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.junit.Test;
import org.w3c.dom.Element;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * @author Mark
 */
public class OMIMWorkerTest extends AbstractXmlWorkerTest {

	private static Logger logger = Logger.getLogger(OMIMWorkerTest.class);

	@Test
	public void testExecute() throws Exception {
		LocalWorker worker = new OMIMWorker();
		Map inputMap = new HashMap();
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		inAdapter.putString("term", "brca2");
		Map outputMap = null;

		try {
			outputMap = worker.execute(inputMap);
		} catch (TaskExecutionException e) {
			if (e.getCause() instanceof TransmitterException) { // don't fail
																// the test,
																// there is
																// something
																// wrong with
																// the external
																// service not
																// the worker.
				logger
						.error("There is a problem with the http://www.ncbi.nlm.nih.gov/entrez/query.fcgi endpoint for OMIWorker");
				return;
			} else {
				throw e;
			}
		}

		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		String results = outAdapter.getString("resultsXml");
		assertNotNull("The results were null", results);

		this.writeFile("test_omim.xml", results);
		Element root = this.parseXml(results);
		this.testXmlNotEmpty(root);

	}

}
