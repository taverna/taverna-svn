package org.embl.ebi.escience.scufl.view;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.SetOnlineException;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;

public class XScuflViewTest extends TestCase {
	
	/**
	 * Attempts to load data into a ScuflModel from the same source that the
	 * XScuflParserTest uses, then print out the XScufl text from the XScuflView. In
	 * an ideal world these would be the same, subject to XML parsing (esp.
	 * whitespace).
	 * 
	 * @author Tom Oinn
	 * @author Stian Soiland
	 */
	public void testLoading() throws UnknownProcessorException, UnknownPortException, ProcessorCreationException, DataConstraintCreationException, DuplicateProcessorNameException, MalformedNameException, ConcurrencyConstraintCreationException, DuplicateConcurrencyConstraintNameException, XScuflFormatException, IOException, SetOnlineException {
		ScuflModel model = new ScuflModel();
		// We don't care about the services in our example being outdated!
		model.setOffline(true);
		XScuflView view = new XScuflView(model);		
		ClassLoader loader = this.getClass().getClassLoader();
		URL location = loader
				.getResource("org/embl/ebi/escience/scufl/parser/XScufl_example.xml");					
		XScuflParser.populate(location.openStream(), model, null);		
								
		// NOTE: The following test will fail with anything but a very simple workflow. 
		// For instance, two processors can be stored in any order in the XML.
		String generatedXML = view.getXMLText();		
		// Should no longer be equal if we clear
		model.clear();
		assertFalse(generatedXML.equals(view.getXMLText()));
		// But if we reload what we just generated, it should be the same
		XScuflParser.populate(generatedXML, model, null);
		assertTrue(generatedXML.equals(view.getXMLText()));				
		

		/*  Disabled - the XML on file might be in an older format		
		InputStreamReader isr = new InputStreamReader(location.openStream());		
		String loadedXML = IOUtils.toString(isr);				
		assertEquals(loadedXML, generatedXML);
		*/
		
	}		
}
