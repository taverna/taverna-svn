package uk.ac.manchester.cs.elico.rmservicetype.taverna;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.activities.wsdl.security.SecurityProfiles;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLInputSplitterActivity;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLSplitterConfigurationBean;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.soap.WSDLSOAPInvoker;

public class ExampleActivity extends
		AbstractAsynchronousActivity<ExampleActivityConfigurationBean>
		implements AsynchronousActivity<ExampleActivityConfigurationBean> {

	/*
	 * Best practice: Keep port names as constants to avoid misspelling. This
	 * would not apply if port names are looked up dynamically from the service
	 * operation, like done for WSDL services.
	 */
	private static final String IN_FIRST_INPUT = "firstInput";
	private static final String IN_EXTRA_DATA = "extraData";
	private static final String OUT_MORE_OUTPUTS = "moreOutputs";
	private static final String OUT_SIMPLE_OUTPUT = "Output";
	private static final String OUT_REPORT = "report";
	
    static String securityProfile = SecurityProfiles.HTTP_BASIC_AUTHN;

	
	private ExampleActivityConfigurationBean configBean;
	List<String> portListing;
	@Override
	public void configure(ExampleActivityConfigurationBean configBean)
			throws ActivityConfigurationException {

		// Any pre-config sanity checks
		if (configBean.getExampleString().equals("invalidExample")) {
			throw new ActivityConfigurationException(
					"Example string can't be 'invalidExample'");
		}
		// Store for getConfiguration(), but you could also make
		// getConfiguration() return a new bean from other sources
		this.configBean = configBean;
		
		// OPTIONAL: 
		// Do any server-side lookups and configuration, like resolving WSDLs

		// myClient = new MyClient(configBean.getExampleUri());
		// this.service = myClient.getService(configBean.getExampleString());
		portListing = getParametersForOperation(configBean.getExampleString());
		
		// REQUIRED: (Re)create input/output ports depending on configuration
		configurePorts();
	}

	protected void configurePorts() {
		// In case we are being reconfigured - remove existing ports first
		// to avoid duplicates
		removeInputs();
		removeOutputs();

		// FIXME: Replace with your input and output port definitions
		
		Iterator inputIterator = portListing.iterator();
		
		while (inputIterator.hasNext()) {
			
			//System.out.println("port " + inputIterator.next());
			addInput((String)inputIterator.next(), 0, true, null, String.class);
			
		}
		
		// Hard coded input port, expecting a single String
		//addInput(IN_FIRST_INPUT, 0, true, null, String.class);

		// Optional ports depending on configuration
		if (configBean.getExampleString().equals("specialCase")) {
			// depth 1, ie. list of binary byte[] arrays
			addInput(IN_EXTRA_DATA, 1, true, null, byte[].class);
			addOutput(OUT_REPORT, 0);
		}
		
		// Single value output port (depth 0)
		addOutput(OUT_SIMPLE_OUTPUT, 0);
		// Output port with list of values (depth 1)
		//addOutput(OUT_MORE_OUTPUTS, 1);

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs,
			final AsynchronousActivityCallback callback) {
		// Don't execute service directly now, request to be run ask to be run
		// from thread pool and return asynchronously
		callback.requestRun(new Runnable() {
			
			public void run() {
				InvocationContext context = callback
						.getContext();
				ReferenceService referenceService = context
						.getReferenceService();
				// Resolve inputs 				
				String firstInput = (String) referenceService.renderIdentifier(inputs.get(IN_FIRST_INPUT), 
						String.class, context);
				
				// Support our configuration-dependendent input
				boolean optionalPorts = configBean.getExampleString().equals("specialCase"); 
				
				List<byte[]> special = null;
				// We'll also allow IN_EXTRA_DATA to be optionally not provided
				if (optionalPorts && inputs.containsKey(IN_EXTRA_DATA)) {
					// Resolve as a list of byte[]
					special = (List<byte[]>) referenceService.renderIdentifier(
							inputs.get(IN_EXTRA_DATA), byte[].class, context);
				}
				
				WSDLActivity wrapper = new WSDLActivity();
				WSDLActivityConfigurationBean bean = new WSDLActivityConfigurationBean();;
				bean.setOperation("executeBasicOperatorExplicitOutput_input");
				bean.setWsdl("http://rpc295.cs.man.ac.uk:8081/e-LICO/ExecutorService?wsdl");
				bean.setSecurityProfile(securityProfile);
				
				try {
				
					wrapper.configure(bean);
				
				} catch (ActivityConfigurationException e) {
				
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
				//XMLInputSplitterActivity mySplitter = new XMLInputSplitterActivity();
				//XMLSplitterConfigurationBean beansplitter = new XMLSplitterConfigurationBean();
				//ActivityInputPortDefinitionBean activityInputBean = new ActivityInputPortDefinitionBean();
				//activityInputBean.
				//beansplitter.setInputPortDefinitions(portDefinitions)
				//mySplitter.configure(config)
				
				// TODO: Do the actual service invocation
//				try {
//					results = this.service.invoke(firstInput, special)
//				} catch (ServiceException ex) {
//					callback.fail("Could not invoke Example service " + configBean.getExampleUri(),
//							ex);
//					// Make sure we don't call callback.receiveResult later 
//					return;
//				}

				// Register outputs
				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
				String simpleValue = "simple";
				T2Reference simpleRef = referenceService.register(simpleValue, 0, true, context);
				outputs.put(OUT_SIMPLE_OUTPUT, simpleRef);

				// For list outputs, only need to register the top level list
				List<String> moreValues = new ArrayList<String>();
				moreValues.add("Value 1");
				moreValues.add("Value 2");
				T2Reference moreRef = referenceService.register(moreValues, 1, true, context);
				outputs.put(OUT_MORE_OUTPUTS, moreRef);

				if (optionalPorts) {
					// Populate our optional output port					
					// NOTE: Need to return output values for all defined output ports
					String report = "Everything OK";
					outputs.put(OUT_REPORT, referenceService.register(report,
							0, true, context));
				}
				
				// return map of output data, with empty index array as this is
				// the only and final result (this index parameter is used if
				// pipelining output)
				callback.receiveResult(outputs, new int[0]);
			}
		});
	}

	@Override
	public ExampleActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}
	
	public List <String> getParametersForOperation(String operationName) {
		System.out.println("^^^Starting tester");

		Map<Object, String> inputMap = new HashMap<Object, String>();
		String inputString = "<parameters xmlns=\"http://elico.rapid_i.com/\"><operatorName xmlns=\"\">" + operationName + "</operatorName></parameters>";
		inputMap.put("parameters", inputString);
		
		System.out.println("^^^Starting tester2");
		
		// WSDLActivityConfigurationBean
		WSDLActivityConfigurationBean myBean = new WSDLActivityConfigurationBean();
		myBean.setWsdl("http://rpc295.cs.man.ac.uk:8081/e-LICO/ExecutorService?wsdl");
		myBean.setOperation("getParameterNames");
		
		// Output and Parser for WSDLSOAPInvoker
		List<String> outputNames = new ArrayList<String>();
		outputNames.add("attachmentList");
		outputNames.add("parameters");
		
		System.out.println("^^^Point 1");
		
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
		
		WSDLSOAPInvoker myInvoker = new WSDLSOAPInvoker(parser, "getParameterNames", outputNames);
		
		System.out.println("^^^Point 2");

		Service service = new Service();
		Call call = null;
		
		System.out.println("^^^Point 3");
		
		// Create Call Object
		try {
			call = (Call)service.createCall();
		} catch (ServiceException e3) {
			e3.printStackTrace();
		}
		
		System.out.println("^^^Point 4");
		
		// Set Username and Password (credential manager)
		UsernamePassword usernamePassword = null;
		
		try {
			usernamePassword = getUsernameAndPasswordForService(myBean, true);
		} catch (CMException e2) {
			e2.printStackTrace();
		}
		
		System.out.println("^^^Point 5");
		

		MessageContext context = call.getMessageContext();
		context.setUsername(usernamePassword.getUsername());
		context.setPassword(usernamePassword.getPasswordAsString());
		usernamePassword.resetPassword();
		
		System.out.println("^^^Point 6");
	
		// Set wsdl endpoint address and operation name
		
		System.out.println("^^^Point 7");

		call.setTargetEndpointAddress("http://rpc295.cs.man.ac.uk:8081/e-LICO/ExecutorService?wsdl");
		call.setOperationName("getParameterNames");
		
		System.out.println("^^^Point 8");
		
		// Invoke 
		Map<String, Object> myOutputs = new HashMap<String, Object>();
	
		try {
			myOutputs = myInvoker.invoke(inputMap,call);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("^^^Point 9");
		
		System.out.println("Complete");
		System.out.println( "NEW NEW NEW NEW PARAMETERS " + myOutputs.toString());

		// Parse stuff
		String myOutput = myOutputs.toString();
		int a, b;
		a = myOutput.indexOf("<return>");
		System.out.println(" number : " + a);
		
		b = myOutput.indexOf("</return></");
		System.out.println(" second number : " + b);
		b += 9;
		String newOutput = myOutput.substring(a, b);
		
		String finalOutput = "<myroot>" + newOutput +  "</myroot>";
		System.out.println("parsed Paraeters " + finalOutput);
		
		
		// rip elements out of xml
			
		
		List<String> portList = new ArrayList<String>();
		try  {
			
			DocumentBuilderFactory dbf =
	            DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(finalOutput));
		
	        Document doc = db.parse(is);
	        NodeList nodes = doc.getElementsByTagName("return");
	        
	        System.out.println(" length " + nodes.getLength());
	        
	        for (int i = 0; i < nodes.getLength(); i++) {
	        	
	        	Element element = (Element) nodes.item(i);
	        	Element line = (Element) nodes.item(i);
	        	//System.out.println("value: " + getCharacterDataFromElement(line));
	        	portList.add(getCharacterDataFromElement(line));
	        }
		 		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return portList;
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

}
