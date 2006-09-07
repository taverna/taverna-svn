package org.embl.ebi.escience.scufl.view;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.SetOnlineException;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.jdom.Document;

public class XScuflViewTest extends TestCase {
	private static Logger logger = Logger.getLogger(XScuflViewTest.class);
	/**
	 * Attempts to load data into a ScuflModel from the same source that the
	 * XScuflParserTest uses, then print out the XScufl text from the XScuflView. In
	 * an ideal world these would be the same, subject to XML parsing (esp.
	 * whitespace).
	 * 
	 * @author Tom Oinn
	 * @author Stian Soiland
	 */
	
	//FIXME Test needs moving to an integration test.	
	public void testLoading() throws UnknownProcessorException, UnknownPortException, ProcessorCreationException, DataConstraintCreationException, DuplicateProcessorNameException, MalformedNameException, ConcurrencyConstraintCreationException, DuplicateConcurrencyConstraintNameException, XScuflFormatException, IOException, SetOnlineException {
		logger.error("testLoading commented out as it required a full system build to run successfully. Needs moving to an Integration test phase.");
		/* COMMENTED OUT - REQUIRED FULL SYSTEM TO BE BUILT TO RUN SUCCESSFULLY - MORE SUITABLE AS AN INTEGRATION TEST RATHER THAN A UNIT TEST.
		// Time to wait for the events to propagate to XScuflView
		final int waitForEvent=50; // ms
		ScuflModel model = new ScuflModel();
		// We don't care about the services in our example being outdated!
		model.setOffline(true);
		XScuflView view = new XScuflView(model);		
		ClassLoader loader = this.getClass().getClassLoader();
		URL location = loader
				.getResource("org/embl/ebi/escience/scufl/parser/XScufl_example.xml");					
		XScuflParser.populate(location.openStream(), model, null);		
		try {
			
			Thread.sleep(waitForEvent);
		} catch (InterruptedException e) {
		}						
		// NOTE: The following test will fail with anything but a very simple workflow. 
		// For instance, two processors can be stored in any order in the XML.
		String generatedXML = view.getXMLText();		
		// Should no longer be equal if we clear
		model.clear();
		try {
			Thread.sleep(waitForEvent);
		} catch (InterruptedException e) {
		}
		assertFalse(generatedXML.equals(view.getXMLText()));
		// But if we reload what we just generated, it should be the same
		XScuflParser.populate(generatedXML, model, null);
		try {
			Thread.sleep(waitForEvent);
		} catch (InterruptedException e) {
		}
		assertTrue(generatedXML.equals(view.getXMLText()));
		// Finished with the view
		model.removeListener(view);

		//Disabled - the XML on file might be in an older format		
		//InputStreamReader isr = new InputStreamReader(location.openStream());		
		//String loadedXML = IOUtils.toString(isr);				
		//assertEquals(loadedXML, generatedXML);
		 */
				
	}
	
	public void testStatic() {
		ScuflModel model = new ScuflModel();
		Document doc = XScuflView.getDocument(model);
		assertEquals("scufl", doc.getRootElement().getName());
		for (ScuflModelEventListener listener : model.getListeners() ) {
			assertFalse(listener instanceof XScuflView);
		}		
		String xml = XScuflView.getXMLText(model);
		assertTrue(xml.startsWith("<?xml"));
		for (ScuflModelEventListener listener : model.getListeners() ) {
			assertFalse(listener instanceof XScuflView);
		}				
	}
	public void testListener() {
		ScuflModel model = new ScuflModel();
		for (ScuflModelEventListener listener : model.getListeners() ) {
			assertFalse(listener instanceof XScuflView);
		}
		// Should add it self automatically
		XScuflView view = new XScuflView(model);
		for (ScuflModelEventListener listener : model.getListeners() ) {
			assertTrue(listener instanceof XScuflView);
		}
		// But we have to remove it manually
		model.removeListener(view);
		for (ScuflModelEventListener listener : model.getListeners() ) {
			assertFalse(listener instanceof XScuflView);
		}
	}
}
