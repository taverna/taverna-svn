package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.serviceprovider;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
		//String[] ops = getRegisteredOperatorNames();
		try {
			getOperatorTree();
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		// FIXME: Implement the actual service search/lookup instead
		// of dummy for-loop
						
		Object [] keys = rootHash.keySet().toArray();
		int operatorCount = rootHash.keySet().size();
		System.out.println(" OPERATOR COUNT " + operatorCount + " \n " + keys[0] + " " + keys[1]);
		String a; 
		
		for (int i = 1; i < operatorCount; i++) {
			
			RapidMinerServiceDesc service = new RapidMinerServiceDesc();
			// Populate the service description bean
			service.setOperatorName(displayHash.get(keys[i].toString()));
			service.setExampleUri(URI.create("http://rapid-i.dyndns.org:8080/e-LICO/ExecutorService"));
			System.out.println(" CALL NAME " + keys[i].toString());
			service.setCallName(keys[i].toString());
			List<String> myList = seperateGroupName(rootHash.get(keys[i]));
			service.setPath(myList);
			
			// Optional: set description
			//service.setDescription("Rapid Miner Operation " + i);
			results.add(service);
		}

		// partialResults() can also be called several times from inside
		// for-loop if the full search takes a long time
		callBack.partialResults(results);

		// No more results will be coming
		callBack.finished();
		
		rootHash.clear();
		displayHash.clear();
	}
	
	HashMap<String, String> rootHash = new HashMap<String, String>();
	HashMap<String, String> displayHash = new HashMap<String, String>();

	
	public void getOperatorTree() {
		
		Map inputMap = null;
		
		System.out.println("Starting get Operator Tree");
		
		// WSDLActivityConfigurationBean
		WSDLActivityConfigurationBean myBean = new WSDLActivityConfigurationBean();
		myBean.setWsdl("http://rapid-i.dyndns.org:8080/e-LICO/ExecutorService?wsdl");
		myBean.setOperation("getOperatorTree");
		
		// Output and Parser for WSDLSOAPInvoker
		List<String> outputNames = new ArrayList<String>();
		outputNames.add("attachmentList");
		outputNames.add("getOperatorTreeResponse");
		
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
					
		WSDLSOAPInvoker myInvoker = new WSDLSOAPInvoker(parser, "getOperatorTree", outputNames);
			
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
		
		call.setTargetEndpointAddress("http://rapid-i.dyndns.org:8080/e-LICO/ExecutorService?wsdl");
		call.setOperationName("getOperatorTree");
		
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
		
		System.out.println("Complete");
		
		System.out.println("Here 1");
		
		System.out.println(myOutputs.toString());
		
		Object myObjs = myOutputs.get("getRegisteredOperatorTreeResponse");
		System.out.println("Here 2");
		
		String myOutput = myOutputs.toString();
		int a, b;
		a = myOutput.indexOf("<return>");
		System.out.println(" number : " + a);
		
		b = myOutput.indexOf("</return></");
		System.out.println(" second number : " + b);
		b += 9;
		String newOutput = myOutput.substring(a, b);
		
		System.out.println(" new Output " + newOutput.toString());
		// New parsing bit
		
		NodeList rtnList = null;
		NodeList children = null;
		NodeList groupName = null;
		NodeList operatorNames = null;
		
		HashMap myHashMap = new HashMap();
		
		try { 
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(newOutput));
			
			Document doc = db.parse(is);
			rtnList = doc.getElementsByTagName("return");	 
			children = doc.getElementsByTagName("children");	//	All children nodes
			operatorNames = doc.getElementsByTagName("operatorNames");
			
			System.out.println(" the number of elements in rtnList is " + rtnList.getLength());
			System.out.println(" the number of elements in children is " + children.getLength());
			
			NodeList all = rtnList.item(0).getChildNodes();
			System.out.println(" all length " + all.getLength());
					
			// lists
			NodeList rootChildren = null;
			for (int i = 0; i < all.getLength(); i++) { 	// for all Root elements
				
				if (all.item(i).getNodeName().equals("children")) {	// if it equals children (should be 11 children)
					
					System.out.println(" a child node " + i + " " + all.item(i).getNodeName());
					NodeList childrenList = all.item(i).getChildNodes();
					
					List<String> myTemporaryList = new ArrayList<String>();
					String operationName = null;
					String displayName = null;
					String path = null;
					for (int j = 0; j < childrenList.getLength(); j++) {	// for all childnodes of children
						
						
						if (childrenList.item(j).getNodeName().equals("groupName")) {	// if there's a groupname, set it to the operatorNames path
							
				//			System.out.println(" inner child " + getCharacterDataFromElement((Element)childrenList.item(j)));
							myTemporaryList.add(getCharacterDataFromElement((Element)childrenList.item(j)));
							path = getCharacterDataFromElement((Element)childrenList.item(j));
						}
						
						if (childrenList.item(j).getNodeName().equals("operators")) {
							
				//			System.out.println(" operator name " + getCharacterDataFromElement((Element)childrenList.item(j)));
							
							NodeList operators = childrenList.item(j).getChildNodes();
							
								for (int k = 0; k < operators.getLength(); k++) {
									
									if (operators.item(k).getNodeName().equals("name")) {
										operationName = getCharacterDataFromElement((Element)operators.item(k));
									}
									
									if (operators.item(k).getNodeName().equals("displayName")) {
										displayName = getCharacterDataFromElement((Element)operators.item(k));
									}
																		
								}
							
						}
						
						if (childrenList.item(j).getNodeName().equals("children")) {
							//System.out.println("Child node found.");
							getChildrenOperatorNames(childrenList.item(j).getChildNodes());
						}
						displayHash.put(operationName, displayName);
						rootHash.put(operationName, path);
						//path.split("[.]");
						
						//rootHash.put(operationName, seperateGroupName(path));
						
					}
					
				}
			}
			
			// for each children
		
		} catch (Exception e) {
			
		}
		System.out.println(" set " + rootHash.keySet().size());

		
		
		
	}
	
	public List<String> seperateGroupName(String myString) {
		
		List<String> parsed = new ArrayList<String>();
		String[] tokens = myString.split("[.]+");
		for (int j = 0; j < tokens.length; j++)
		    parsed.add(tokens[j]);
		
		return parsed;
	}
	
	// return a hashmap with the operatornames with a list of groupnames
	public HashMap getChildrenOperatorNames(NodeList children) {
		
		// find the operatornames and a list of groupnames
		String groupName = null;
		String displayName = null;
		String name = null;
		for (int i = 0; i < children.getLength(); i++) {
			
			if (children.item(i).getNodeName().equals("groupName")) {
				
				System.out.println(" found the groupname for this list of children " + getCharacterDataFromElement((Element)children.item(i)));
				groupName = getCharacterDataFromElement((Element)children.item(i));
				break;

			}
			
			
		}
		
		for (int i = 0; i < children.getLength(); i++){

			if (children.item(i).getNodeName().equals("operators")) {
				
				NodeList operators = children.item(i).getChildNodes();
				
				for (int k = 0; k < operators.getLength(); k++) {
					
					if (operators.item(k).getNodeName().equals("name")) {
						rootHash.put(getCharacterDataFromElement((Element)operators.item(k)), groupName);
						name = getCharacterDataFromElement((Element)operators.item(k));
					}
					
					if (operators.item(k).getNodeName().equals("displayName")) {
						displayName = getCharacterDataFromElement((Element)operators.item(k));
					}
					
				}				
				displayHash.put(name, displayName);
				System.out.println(" operator name " + getCharacterDataFromElement((Element)children.item(i)) + " in group " + groupName);
				//rootHash.put(getCharacterDataFromElement((Element)children.item(i)), seperateGroupName(groupName) );
			}
			
			if (children.item(i).getNodeName().equals("children")) {
				
				getChildrenOperatorNames(children.item(i).getChildNodes());
				
			}
			
			
		}
		
		
		
		return null;
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
		return RapidMinerIcon.getIcon();
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



/*
for (int i = 0; i < children.getLength(); i++) {
	
	if (children.item(i).hasChildNodes()) {
							
		temporaryChild = children.item(i).getChildNodes();
		
		System.out.println(" child " + i + " has " + temporaryChild.getLength());
		
			for (int j = 0; j < temporaryChild.getLength(); j++) {
				
				System.out.println(" something " + temporaryChild.item(j).getNodeName());

			}
	
	}
	
}
*/


/*
//String newnewOutput = "<myroot>" + newOutput +  "</myroot>";
//System.out.println("newOutput " + newnewOutput);

// Parsing bit

NodeList name = null;
NodeList returnnodes = null;
NodeList operatorNamesNodes = null;
NodeList operatorNamesNodesAbba = null;

try  {
	
	DocumentBuilderFactory dbf =
        DocumentBuilderFactory.newInstance();
	dbf.setNamespaceAware(true);
	DocumentBuilder db = dbf.newDocumentBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(newOutput));

    Document doc = db.parse(is);
    returnnodes = doc.getElementsByTagName("children");

    NodeList nodes = doc.getElementsByTagName("return");
    
    operatorNamesNodes = doc.getElementsByTagName("operatorNames");
    
    System.out.println("The number of nodes : " + returnnodes.getLength());
    
        for (int i = 0; i < nodes.getLength(); i++) {
	    
        	//Element element = (Element) nodes.item(i);
        	//System.out.println( " A element " + i + " " + nodes.item(i).toString());
        	Element element = (Element) nodes.item(i);
        	name = element.getElementsByTagName("groupName");
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
*/
