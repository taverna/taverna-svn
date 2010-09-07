package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.serviceprovider;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.wsdl.WSDLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;

import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.soap.WSDLSOAPInvoker;

public class ExampleServiceProvider implements ServiceDescriptionProvider {
	
	private static final URI providerId = URI
		.create("http://example.com/2010/service-provider/example-activity-ui");
	
	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@SuppressWarnings("unchecked")
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		// Use callback.status() for long-running searches
		// callBack.status("Resolving example services");

		List<ServiceDescription> results = new ArrayList<ServiceDescription>();
		String[] ops = getRegisteredOperatorNames();
		// FIXME: Implement the actual service search/lookup instead
		// of dummy for-loop
		for (int i = 1; i < ops.length; i++) {
			ExampleServiceDesc service = new ExampleServiceDesc();
			// Populate the service description bean
			service.setExampleString(ops[i]);
			service.setExampleUri(URI.create("http://rpc295.cs.man.ac.uk:8081/e-LICO/ExecutorService"));

			// Optional: set description
			service.setDescription("Rapid Miner Operation " + i);
			results.add(service);
		}

		// partialResults() can also be called several times from inside
		// for-loop if the full search takes a long time
		callBack.partialResults(results);

		// No more results will be coming
		callBack.finished();
	}
	
	public String[] getRegisteredOperatorNames() {
		
		Map inputMap = null;
		
		System.out.println("Starting tester");
		
		// WSDLActivityConfigurationBean
		WSDLActivityConfigurationBean myBean = new WSDLActivityConfigurationBean();
		myBean.setWsdl("http://rpc295.cs.man.ac.uk:8081/e-LICO/ExecutorService?wsdl");
		myBean.setOperation("getRegisteredOperatorNames");
		
		// Output and Parser for WSDLSOAPInvoker
		List<String> outputNames = new ArrayList<String>();
		outputNames.add("attachmentList");
		outputNames.add("getRegisteredOperatorNamesResponse");
		
		System.out.println("Point 1");
		
		WSDLParser parser = null;
			
		try {
			 parser = new WSDLParser(myBean.getWsdl()) ;
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (WSDLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		}
					
		WSDLSOAPInvoker myInvoker = new WSDLSOAPInvoker(parser, "getRegisteredOperatorNames", outputNames);
			
		System.out.println("Point 2");

		Service service = new Service();
		Call call = null;
		
		System.out.println("Point 3");
		
		// Create Call Object
		try {
			call = (Call)service.createCall();
		} catch (ServiceException e3) {
			e3.printStackTrace();
		}
		
		System.out.println("Point 4");
		
		// Set Username and Password (credential manager)
		UsernamePassword usernamePassword = null;
		
		try {
			usernamePassword = getUsernameAndPasswordForService(myBean, true);
		} catch (CMException e2) {
			e2.printStackTrace();
		}
		
		System.out.println("Point 5");

		MessageContext context = call.getMessageContext();
		context.setUsername(usernamePassword.getUsername());
		context.setPassword(usernamePassword.getPasswordAsString());
		usernamePassword.resetPassword();
		
		System.out.println("Point 6");
	
		// Set wsdl endpoint address and operation name
		
		System.out.println("Point 7");

		call.setTargetEndpointAddress("http://rpc295.cs.man.ac.uk:8081/e-LICO/ExecutorService?wsdl");
		call.setOperationName("getRegisteredOperatorNames");
		
		System.out.println("Point 8");
		
		// Invoke 
		Map<String, Object> myOutputs = null;
		try {
			myOutputs = myInvoker.invoke(inputMap,call);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Point 9");
		
		System.out.println("Complete");
		
		System.out.println("Here 1");
		
		System.out.println(myOutputs.toString());
		
		Object myObjs = myOutputs.get("getRegisteredOperatorNamesResponse");
		System.out.println("Here 2");
		
		String myOutput = myOutputs.toString();
		int a, b;
		a = myOutput.indexOf("<return>");
		System.out.println(" number : " + a);
		
		b = myOutput.indexOf("</return></");
		System.out.println(" second number : " + b);
		b += 9;
		String newOutput = myOutput.substring(a, b);
		
		String newnewOutput = "<myroot>" + newOutput +  "</myroot>";
		System.out.println("newOutput " + newnewOutput);

		

		
		// Parsing bit

		
		NodeList name = null;
		NodeList returnnodes = null;
		
		try  {
			
			DocumentBuilderFactory dbf =
	            DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(newnewOutput));
		
	        Document doc = db.parse(is);
	        returnnodes = doc.getElementsByTagName("return");

	        NodeList nodes = doc.getElementsByTagName("myroot");
	      
		        for (int i = 0; i < nodes.getLength(); i++) {
			    
		        	//Element element = (Element) nodes.item(i);
		        	//System.out.println( " A element " + i + " " + nodes.item(i).toString());
		        	Element element = (Element) nodes.item(i);
		        	name = element.getElementsByTagName("return");
		        	Element line = (Element) name.item(0);
		        	System.out.println("Element " + i + " " + getCharacterDataFromElement(line));
		        	
		        }
	        
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
		int num = returnnodes.getLength();
		String[] registeredOperatorNames = new String[num];
				
		for (int i = 0; i < num; i++) {
			Element line = (Element) name.item(i);
			System.out.println(" my list " + i + " " + getCharacterDataFromElement(line));
			registeredOperatorNames[i] = getCharacterDataFromElement(line);
		}
				
		return registeredOperatorNames;
		
	}

	protected UsernamePassword getUsernameAndPasswordForService(
			WSDLActivityConfigurationBean bean, boolean usePathRecursion) throws CMException {

			// Try to get username and password for this service from Credential
			// Manager (which should pop up UI if needed)
			CredentialManager credManager = null;
			credManager = CredentialManager.getInstance();
			String wsdl = bean
			.getWsdl();
			URI serviceUri = URI.create(wsdl); 
			UsernamePassword username_password = credManager.getUsernameAndPasswordForService(serviceUri, usePathRecursion, null);
			if (username_password == null) {
				throw new CMException("No username/password provided for service " + bean.getWsdl());
			} 
			return username_password;
	}
	
	public static String getCharacterDataFromElement(Element e) {
	    Node child = e.getFirstChild();
	    if (child instanceof CharacterData) {
	       CharacterData cd = (CharacterData) child;
	       return cd.getData();
	    }
	    return "?";
	  }
	
	/**
	 * Icon for service provider
	 */
	public Icon getIcon() {
		return ExampleServiceIcon.getIcon();
	}

	/**
	 * Name of service provider, appears in right click for 'Remove service
	 * provider'
	 */
	public String getName() {
		return "My example service";
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public String getId() {
		return providerId.toASCIIString();
	}

}
