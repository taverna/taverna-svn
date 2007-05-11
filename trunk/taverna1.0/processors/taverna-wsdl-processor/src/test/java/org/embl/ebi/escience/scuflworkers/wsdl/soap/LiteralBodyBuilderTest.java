package org.embl.ebi.escience.scuflworkers.wsdl.soap;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis.message.SOAPBodyElement;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scuflworkers.testhelpers.WSDLBasedTestCase;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;

public class LiteralBodyBuilderTest extends WSDLBasedTestCase {

	public void testUnqualifiedNamespaces() throws Exception {
		BodyBuilder builder = createBuilder(TESTWSDL_BASE+"whatizit.wsdl", "queryPmid");
		
		assertTrue("Is is the wrong type, it should be LiteralBodyBuilder",builder instanceof LiteralBodyBuilder);
		
		String parameters = "<parameters xmlns=\"http://www.ebi.ac.uk/webservices/whatizit/ws\"><pipelineName xmlns=\"\">swissProt</pipelineName><pmid xmlns=\"\">1234</pmid></parameters>";
		Map<String,DataThing> inputMap = new HashMap<String, DataThing>();
		inputMap.put("parameters", DataThingFactory.bake(parameters));
		
		SOAPBodyElement body = builder.build(inputMap);
		
		String xml = body.getAsString();
		
		assertTrue("Content of body is incorrect in the definition of the pipelineName and pmid:"+xml,xml.contains("<pipelineName xmlns=\"\">swissProt</pipelineName><pmid xmlns=\"\">1234</pmid>"));
		assertTrue("Wrapping element should have its namespace declared",xml.contains("<ns1:queryPmid"));
	}
	
	protected BodyBuilder createBuilder(String wsdl, String operation) throws Exception {
		WSDLBasedProcessor processor = new WSDLBasedProcessor(null, "test",
				wsdl,
				operation);
		return BodyBuilderFactory.instance().create(processor);
	}
}
