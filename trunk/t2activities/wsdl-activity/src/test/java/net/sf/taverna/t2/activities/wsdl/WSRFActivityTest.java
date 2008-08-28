package net.sf.taverna.t2.activities.wsdl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.taverna.wsdl.parser.WSDLParser;

import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPEnvelope;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

public class WSRFActivityTest {

	public class DummyInvoker extends T2WSDLSOAPInvoker {

		public DummyInvoker(String wsrfEndpoint) {
			super(wsdlParser, "add", Arrays.asList("attachmentList"),
					wsrfEndpoint);
		}

		@Override
		protected SOAPEnvelope invokeCall(Call call, SOAPEnvelope requestEnv) {
			requestXML = requestEnv;
			return null;
		}
	}

	private static final Namespace SoapEnvelopeNS = Namespace
			.getNamespace("http://schemas.xmlsoap.org/soap/envelope/");
	private static final Namespace CounterNS = Namespace
			.getNamespace("http://counter.com");
	private static final Namespace DifficultNS = Namespace
			.getNamespace("http://difficult.com/");

	private static final Namespace DefaultNS = Namespace
			.getNamespace("http://default/");

	private URL counterServiceWSDL;
	private WSDLParser wsdlParser;
	protected SOAPEnvelope requestXML;

	@Before
	public void makeWSDLParser() throws Exception {
		String path = "wsrf/counterService/CounterService_.wsdl";
		counterServiceWSDL = getClass().getResource(path);
		assertNotNull("Coult not find test WSDL " + path, counterServiceWSDL);
		wsdlParser = new WSDLParser(counterServiceWSDL.toExternalForm());
	}

	public void noHeaders() throws Exception {

	}

	@Test
	public void insertedEndpoint() throws Exception {
		// Example from http://www.mygrid.org.uk/dev/issues/browse/TAV-23
		String wsrfEndpoint = "" +
				"<c:createCounterResponse xmlns:c='http://counter.com'>" +
				"<wsa:EndpointReference "
				+ "xmlns:wsa='http://schemas.xmlsoap.org/ws/2004/03/addressing' "
				+ "xmlns:counter='http://counter.com'>"
				+ "  <wsa:Address>http://130.88.195.110:8080/wsrf/services/CounterService</wsa:Address>"
				+ "   <wsa:ReferenceProperties>"
				+ "     <counter:CounterKey>15063581</counter:CounterKey>"
				+ "      <difficult:one xmlns:difficult='http://difficult.com/' "
				+ "             difficult:attrib='something' attrib='else' >"
				+ "         <difficult:fish><counter:fish /></difficult:fish> "
				+ "      </difficult:one>" + "      <emptyNamespace>"
				+ "          <defaultNamespace xmlns='http://default/'>"
				+ "\n  default  \n " + "</defaultNamespace>"
				+ "          <stillEmpty />" + "      </emptyNamespace>"
				+ "  </wsa:ReferenceProperties>"
				+ "  <wsa:ReferenceParameters/>" + "</wsa:EndpointReference>" + 
				"</c:createCounterResponse>";

		// Note: We'll subclass to avoid calling service
		// and request attachmentList to trigger TAV-617-code and avoid
		// parsing of the (missing) response
		T2WSDLSOAPInvoker invoker = new DummyInvoker(wsrfEndpoint);
		Map<String, Object> results = invoker.invoke(Collections.singletonMap(
				"add", "10"));
		assertEquals(1, results.size());
		assertEquals("attachmentList", results.keySet().iterator().next());

		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new StringReader(requestXML.toString()));
		Element header = doc.getRootElement()
				.getChild("Header", SoapEnvelopeNS);
		assertNotNull("Could not find soapenv:Header", header);
		assertEquals("Unexpected number of children in header", 3, header
				.getChildren().size());

		// Check that everything was preserved as much as possible

		Element counterChild = header.getChild("CounterKey", CounterNS);
		assertEquals("15063581", counterChild.getText());
		assertEquals("Did not preserve namespace", "counter", counterChild
				.getNamespacePrefix());

		Element difficultChild = header.getChild("one", DifficultNS);
		assertNotNull("Could not find difficult:one", difficultChild);
		assertEquals("Did not preserve namespace", "difficult", difficultChild
				.getNamespacePrefix());
		assertEquals("something", difficultChild.getAttribute("attrib",
				DifficultNS).getValue());
		assertEquals("else", difficultChild.getAttribute("attrib",
				Namespace.NO_NAMESPACE).getValue());

		Element counterFish = difficultChild.getChild("fish", DifficultNS)
				.getChild("fish", CounterNS);
		assertEquals("counter", counterFish.getNamespacePrefix());

		Element emptyChild = header.getChild("emptyNamespace",
				Namespace.NO_NAMESPACE);
		Element defaultNamespace = emptyChild.getChild("defaultNamespace",
				DefaultNS);
		assertEquals("\n  default  \n ", defaultNamespace.getText());

		Element stillEmpty = emptyChild.getChild("stillEmpty");
		assertEquals(Namespace.NO_NAMESPACE, stillEmpty.getNamespace());

	}
}
