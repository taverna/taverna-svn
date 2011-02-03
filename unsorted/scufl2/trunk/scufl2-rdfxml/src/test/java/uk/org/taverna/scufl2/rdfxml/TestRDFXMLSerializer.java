package uk.org.taverna.scufl2.rdfxml;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.junit.Before;
import org.junit.Test;

import uk.org.taverna.scufl2.api.ExampleWorkflow;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;

public class TestRDFXMLSerializer {
	protected static final String TAVERNAWORKBENCH_RDF = "profile/tavernaWorkbench.rdf";
	protected static final String HELLOWORLD_RDF = "workflow/HelloWorld.rdf";
	RDFXMLSerializer serializer = new RDFXMLSerializer();
	ExampleWorkflow exampleWf = new ExampleWorkflow();	
	WorkflowBundle workflowBundle;
	
	Namespace XSI_NS = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
	Namespace RDF_NS = Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	Namespace RDSF_NS = Namespace.getNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
	Namespace SCUFL2_NS = Namespace.getNamespace("s", "http://ns.taverna.org.uk/2010/scufl2#");


	@Before
	public void makeExampleWorkflow() {
		workflowBundle = new ExampleWorkflow().makeWorkflowBundle();
		serializer.setWfBundle(workflowBundle);
	}

	
	@Test
	public void workflowBundleXml() throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		// To test that seeAlso URIs are stored
		serializer.workflowDoc(new NullOutputStream(), workflowBundle.getMainWorkflow(), URI.create(HELLOWORLD_RDF));		
		serializer.profileDoc(new NullOutputStream(), workflowBundle.getMainProfile(), URI.create(TAVERNAWORKBENCH_RDF));
		
		serializer.workflowBundleDoc(outStream, URI.create("workflowBundle.rdf"));
		//System.out.write(outStream.toByteArray());
		Document doc = parseXml(outStream);
		Element root = doc.getRootElement();
		
		checkRoot(root);
		checkWorkflowBundleDocument(root);
		
	}

	@Test
	public void workflowXml() throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		// To test that seeAlso URIs are stored
		serializer.workflowDoc(outStream, workflowBundle.getMainWorkflow(), URI.create(HELLOWORLD_RDF));
		System.out.write(outStream.toByteArray());
		Document doc = parseXml(outStream);
		Element root = doc.getRootElement();
		
		checkRoot(root);
		checkWorkflowDocument(root);
	}




	@Test
	public void usecaseWorkflowBundleXml() throws Exception {
		File f = new File("../scufl2-usecases/src/main/resources/workflows/example/workflowBundle.rdf");

		SAXBuilder saxBuilder = new SAXBuilder();
		Document doc = saxBuilder.build(f);
		
		Element root = doc.getRootElement();
		
		checkRoot(root);
		checkWorkflowBundleDocument(root);
		
	}
	
	@Test
	public void usecaseWorkflowXml() throws Exception {
		File f = new File("../scufl2-usecases/src/main/resources/workflows/example/workflow/HelloWorld.rdf");

		SAXBuilder saxBuilder = new SAXBuilder();
		Document doc = saxBuilder.build(f);
		
		Element root = doc.getRootElement();
		
		checkRoot(root);
		checkWorkflowDocument(root);
		
	}


	protected void checkWorkflowDocument(Element root) throws JDOMException {
		assertEquals("WorkflowDocument", root.getAttributeValue("type", XSI_NS));


		
		assertXpathEquals("HelloWorld/", root, "./@xml:base");
		
		
		Element wf = root.getChild("Workflow", SCUFL2_NS);
		assertSame(wf, root.getChildren().get(0));
		
		assertXpathEquals("", wf, "./@rdf:about");
		
		
		assertXpathEquals("HelloWorld", wf, "./s:name");		
		assertXpathEquals("http://ns.taverna.org.uk/2010/workflow/00626652-55ae-4a9e-80d4-c8e9ac84e2ca/", 
				wf, "./s:workflowIdentifier/@rdf:resource");
		
		assertXpathEquals("in/yourName", 
				wf, "./s:inputWorkflowPort/s:InputWorkflowPort/@rdf:about");
		assertXpathEquals("yourName", 
				wf, "./s:inputWorkflowPort/s:InputWorkflowPort/s:name");
		assertXpathEquals("0", 
				wf, "./s:inputWorkflowPort/s:InputWorkflowPort/s:portDepth");
		assertXpathEquals("http://www.w3.org/2001/XMLSchema#integer", 
				wf, "./s:inputWorkflowPort/s:InputWorkflowPort/s:portDepth/@rdf:datatype");
		
		
		assertXpathEquals("out/results", 
				wf, "./s:outputWorkflowPort/s:OutputWorkflowPort/@rdf:about");
		assertXpathEquals("results", 
				wf, "./s:outputWorkflowPort/s:OutputWorkflowPort/s:name");
		
		assertXpathEquals("processor/wait4me/", 
				wf, "./s:processor[1]/s:Processor/@rdf:about");
		assertXpathEquals("wait4me", 
				wf, "./s:processor[1]/s:Processor/s:name");
			

		assertXpathEquals("processor/Hello/", 
				wf, "./s:processor[2]/s:Processor/@rdf:about");
		assertXpathEquals("Hello", 
				wf, "./s:processor[2]/s:Processor/s:name");
		
		assertXpathEquals("processor/Hello/in/name", 
				wf, "./s:processor[2]/s:Processor/s:inputProcessorPort/s:InputProcessorPort/@rdf:about");
		assertXpathEquals("name", 
				wf, "./s:processor[2]/s:Processor/s:inputProcessorPort/s:InputProcessorPort/s:name");
		assertXpathEquals("0", 
				wf, "./s:processor[2]/s:Processor/s:inputProcessorPort/s:InputProcessorPort/s:portDepth");
		assertXpathEquals("http://www.w3.org/2001/XMLSchema#integer", 
				wf, "./s:processor[2]/s:Processor/s:inputProcessorPort/s:InputProcessorPort/s:portDepth/@rdf:datatype");
		
		

		assertXpathEquals("processor/Hello/out/greeting", 
				wf, "./s:processor[2]/s:Processor/s:outputProcessorPort/s:OutputProcessorPort/@rdf:about");

		assertXpathEquals("greeting", 
				wf, "./s:processor[2]/s:Processor/s:outputProcessorPort/s:OutputProcessorPort/s:name");
		assertXpathEquals("0", 
				wf, "./s:processor[2]/s:Processor/s:outputProcessorPort/s:OutputProcessorPort/s:portDepth");
		assertXpathEquals("http://www.w3.org/2001/XMLSchema#integer", 
				wf, "./s:processor[2]/s:Processor/s:outputProcessorPort/s:OutputProcessorPort/s:portDepth/@rdf:datatype");
		assertXpathEquals("0", 
				wf, "./s:processor[2]/s:Processor/s:outputProcessorPort/s:OutputProcessorPort/s:granularPortDepth");
		assertXpathEquals("http://www.w3.org/2001/XMLSchema#integer", 
				wf, "./s:processor[2]/s:Processor/s:outputProcessorPort/s:OutputProcessorPort/s:granularPortDepth/@rdf:datatype");
		
		// FIXME: probably not what we want - at least we should say it's an *instance* of the default dispatch stack
		assertXpathEquals("http://ns.taverna.org.uk/2010/taverna/2.2/DefaultDispatchStack", 
				wf, "./s:processor[2]/s:Processor/s:dispatchStack/s:DispatchStack/rdf:type/@rdf:resource");
		assertXpathEquals("processor/Hello/dispatchstack/", 
				wf, "./s:processor[2]/s:Processor/s:dispatchStack/s:DispatchStack/@rdf:about");		
		
		assertXpathEquals("processor/Hello/iterationstrategy/", wf, "./s:processor[2]/s:Processor/s:iterationStrategyStack/s:IterationStrategyStack/@rdf:about");
		assertXpathEquals("Collection", wf, "./s:processor[2]/s:Processor/s:iterationStrategyStack/s:IterationStrategyStack/s:iterationStrategies/@rdf:parseType");
		assertXpathEquals("processor/Hello/iterationstrategy/0/", wf, "./s:processor[2]/s:Processor/s:iterationStrategyStack/s:IterationStrategyStack/s:iterationStrategies/s:CrossProduct/@rdf:about");		
		assertXpathEquals("Collection", wf, "./s:processor[2]/s:Processor/s:iterationStrategyStack/s:IterationStrategyStack/s:iterationStrategies/s:CrossProduct/s:productOf/@rdf:parseType");
		assertXpathEquals("processor/Hello/in/name", wf, "./s:processor[2]/s:Processor/s:iterationStrategyStack/s:IterationStrategyStack/s:iterationStrategies/s:CrossProduct/s:productOf/s:InputProcessorPort/@rdf:about");
		
		assertXpathEquals("datalink?from=processor/Hello/out/greeting&to=out/results&mergePosition=0", 
				wf, "./s:datalink[1]/s:DataLink/@rdf:about");

		assertXpathEquals("datalink?from=in/yourName&to=processor/Hello/in/name", 
				wf, "./s:datalink[2]/s:DataLink/@rdf:about");
	
		assertXpathEquals("datalink?from=in/yourName&to=out/results&mergePosition=1", 
				wf, "./s:datalink[3]/s:DataLink/@rdf:about");
		assertXpathEquals("in/yourName", 
				wf, "./s:datalink[3]/s:DataLink/s:receivesFrom/@rdf:resource");
		assertXpathEquals("out/results", 
				wf, "./s:datalink[3]/s:DataLink/s:sendsTo/@rdf:resource");
		assertXpathEquals("1", 
				wf, "./s:datalink[3]/s:DataLink/s:mergePosition");
		assertXpathEquals("http://www.w3.org/2001/XMLSchema#integer", 
				wf, "./s:datalink[3]/s:DataLink/s:mergePosition/@rdf:datatype");

		assertXpathEquals("control?block=processor/Hello/&untilFinished=processor/wait4me/", 
				wf, "./s:control/s:Blocking/@rdf:about");
		

		assertXpathEquals("processor/Hello/", 
				wf, "./s:control/s:Blocking/s:block/@rdf:resource");
		assertXpathEquals("processor/wait4me/", 
				wf, "./s:control/s:Blocking/s:untilFinished/@rdf:resource");
		
		assertXpathEquals("datalink?from=processor/Hello/out/greeting&to=out/results&mergePosition=0", 
				wf, "./s:datalink[1]/s:DataLink/@rdf:about");
		assertXpathEquals("processor/Hello/out/greeting", 
				wf, "./s:datalink[1]/s:DataLink/s:receivesFrom/@rdf:resource");
		assertXpathEquals("out/results", 
				wf, "./s:datalink[1]/s:DataLink/s:sendsTo/@rdf:resource");
		assertXpathEquals("0", 
				wf, "./s:datalink[1]/s:DataLink/s:mergePosition");
		assertXpathEquals("http://www.w3.org/2001/XMLSchema#integer", 
				wf, "./s:datalink[1]/s:DataLink/s:mergePosition/@rdf:datatype");
		


		assertXpathEquals("datalink?from=in/yourName&to=processor/Hello/in/name", 
				wf, "./s:datalink[2]/s:DataLink/@rdf:about");
		assertXpathEquals("in/yourName", 
				wf, "./s:datalink[2]/s:DataLink/s:receivesFrom/@rdf:resource");
		assertXpathEquals("processor/Hello/in/name", 
				wf, "./s:datalink[2]/s:DataLink/s:sendsTo/@rdf:resource");
		assertNull(xpathSelectElement(wf, "./s:datalink[2]/s:DataLink/s:mergePosition"));
		

		
		assertXpathEquals("datalink?from=in/yourName&to=out/results&mergePosition=1", 
				wf, "./s:datalink[3]/s:DataLink/@rdf:about");
		assertXpathEquals("in/yourName", 
				wf, "./s:datalink[3]/s:DataLink/s:receivesFrom/@rdf:resource");
		assertXpathEquals("out/results", 
				wf, "./s:datalink[3]/s:DataLink/s:sendsTo/@rdf:resource");
		assertXpathEquals("1", 
				wf, "./s:datalink[3]/s:DataLink/s:mergePosition");
		assertXpathEquals("http://www.w3.org/2001/XMLSchema#integer", 
				wf, "./s:datalink[3]/s:DataLink/s:mergePosition/@rdf:datatype");

		assertXpathEquals("control?block=processor/Hello/&untilFinished=processor/wait4me/", 
				wf, "./s:control/s:Blocking/@rdf:about");
		

		assertXpathEquals("processor/Hello/", 
				wf, "./s:control/s:Blocking/s:block/@rdf:resource");
		assertXpathEquals("processor/wait4me/", 
				wf, "./s:control/s:Blocking/s:untilFinished/@rdf:resource");
		
	}


	protected void checkWorkflowBundleDocument(Element root) throws JDOMException {
		assertEquals("WorkflowBundleDocument", root.getAttributeValue("type", XSI_NS));

		assertXpathEquals("./", root, "./@xml:base");

		Element wbundle = root.getChild("WorkflowBundle", SCUFL2_NS);
		assertSame(wbundle, root.getChildren().get(0));
		

		
		assertXpathEquals("", wbundle, "./@rdf:about");		
		
		assertXpathEquals("HelloWorld", wbundle, "./s:name");		
		assertXpathEquals("http://ns.taverna.org.uk/2010/workflowBundle/28f7c554-4f35-401f-b34b-516e9a0ef731/", 
				wbundle, "./s:sameBaseAs/@rdf:resource");
		
		assertXpathEquals("workflow/HelloWorld/", 
				wbundle, "./s:mainWorkflow/@rdf:resource");
		assertXpathEquals("workflow/HelloWorld/", 
				wbundle, "./s:workflow/s:Workflow/@rdf:about");
		assertXpathEquals(HELLOWORLD_RDF, 
				wbundle, "./s:workflow/s:Workflow/rdfs:seeAlso/@rdf:resource");

		assertXpathEquals("profile/tavernaWorkbench/", 
				wbundle, "./s:mainProfile/@rdf:resource");
		
		
		assertXpathEquals("profile/tavernaWorkbench/", 
				wbundle, "./s:profile[1]/s:Profile/@rdf:about");
		assertXpathEquals(TAVERNAWORKBENCH_RDF, 
				wbundle, "./s:profile[1]/s:Profile/rdfs:seeAlso/@rdf:resource");
		
		
		assertXpathEquals("profile/tavernaServer/", 
				wbundle, "./s:profile[2]/s:Profile/@rdf:about");
		assertXpathEquals("profile/tavernaServer.rdf", wbundle, "./s:profile[2]/s:Profile/rdfs:seeAlso/@rdf:resource");

	}


	protected void assertXpathEquals(String expected, Element element,
			String xpath) throws JDOMException {
		Object o = xpathSelectElement(element, xpath);
		if (o == null) {
			fail("Can't find " + xpath  + " in " + element);
			return;
		}
		String text;
		if (o instanceof Attribute) {
			text = ((Attribute)o).getValue();
		} else {
			text = ((Element)o).getValue();
		}
		assertEquals(expected, text);		
	}


	protected Object xpathSelectElement(Element element, String xpath) throws JDOMException {
		XPath x = XPath.newInstance(xpath);
		x.addNamespace(SCUFL2_NS);
		x.addNamespace(RDF_NS);
		x.addNamespace(RDSF_NS);
		//x.addNamespace(XML_NS);

		return x.selectSingleNode(element);
	}

	protected void checkRoot(Element root) {
		assertEquals(RDF_NS, root.getNamespace());		
		assertEquals("rdf", root.getNamespacePrefix());
		assertEquals("RDF", root.getName());		
		assertEquals(SCUFL2_NS, root.getNamespace(""));		
		String schemaLocation = root.getAttributeValue("schemaLocation", XSI_NS);
		String[] schemaLocations = schemaLocation.split(" ");
		String[] expectedSchemaLocations = {
				"http://ns.taverna.org.uk/2010/scufl2#","http://ns.taverna.org.uk/2010/scufl2/scufl2.xsd",
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#","http://ns.taverna.org.uk/2010/scufl2/rdf.xsd"
		};
		assertArrayEquals(expectedSchemaLocations, schemaLocations);
	}


	protected Document parseXml(ByteArrayOutputStream outStream)
			throws JDOMException, IOException {
		SAXBuilder saxBuilder = new SAXBuilder();
		return saxBuilder.build(new ByteArrayInputStream(outStream.toByteArray()));
	}
}
