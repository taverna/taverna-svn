package uk.ac.manchester.cs.elico.rmservicetype.taverna;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sf.taverna.t2.activities.wsdl.T2WSDLSOAPInvoker;
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
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.soap.WSDLSOAPInvoker;

public class RapidMinerExampleActivity extends
		AbstractAsynchronousActivity<RapidMinerActivityConfigurationBean>
		implements AsynchronousActivity<RapidMinerActivityConfigurationBean> {

	/*
	 * Best practice: Keep port names as constants to avoid misspelling. This
	 * would not apply if port names are looked up dynamically from the service
	 * operation, like done for WSDL services.
	 */
	private static final String IN_FIRST_INPUT = "firstInput";
	private static final String IN_EXTRA_DATA = "extraData";
	private static final String OUT_MORE_OUTPUTS = "moreOutputs";
	private static final String OUT_SIMPLE_OUTPUT = "outputLocation";
	private static final String OUT_REPORT = "report";
	
    static String securityProfile = SecurityProfiles.HTTP_BASIC_AUTHN;
	
    NodeList myTempList;

	
	private RapidMinerActivityConfigurationBean configBean;
	List<String> portListing;
	@Override
	public void configure(RapidMinerActivityConfigurationBean configBean)
			throws ActivityConfigurationException {
		
		// Any pre-config sanity checks
		if (configBean.getOperatorName().equals("invalidExample")) {
			throw new ActivityConfigurationException(
					"Example string can't be 'invalidExample'");
		}
		// Store for getConfiguration(), but you could also make
		// getConfiguration() return a new bean from other sources
		this.configBean = configBean;
		System.out.println(" THE CALL NAME IS " + this.configBean.getCallName());
	
		// OPTIONAL: 
		// Do any server-side lookups and configuration, like resolving WSDLs

		// myClient = new MyClient(configBean.getExampleUri());
		// this.service = myClient.getService(configBean.getExampleString());
		List<String> locationPorts = new ArrayList<String>();
			locationPorts.add("inputLocation");
			locationPorts.add("outputLocation");
			portListing = locationPorts;
			
		if (configBean.getHasDescriptions()) {
			
			configurePorts();
			
		} else {
			
			
			
			portListing = getParametersForOperation(configBean.getCallName());
			//portListing = locationPorts;
			
		
				List<RapidMinerParameterDescription> descList = getParameterDescriptions(myTempList);
			
	
			
			if (!configBean.getIsParametersConfigured()) {
				configBean.setParameterDescriptions(descList);
	
			}
					
			// REQUIRED: (Re)create input/output ports depending on configuration
			configurePorts();
			configBean.setHasDescriptions(true);
			
		}
			
			
		
	}

	public List<RapidMinerParameterDescription> getParameterDescriptions(NodeList myList) {
		
		System.out.println(" number of RETURNS " + myList.getLength());
		List<RapidMinerParameterDescription> listOfDescriptions = new ArrayList<RapidMinerParameterDescription>();
		
		for (int i = 0; i < myList.getLength(); i++) {			// for each "return" node	 (parameter)
			
			NodeList returnList = myList.item(i).getChildNodes();		// get "return"s children
			RapidMinerParameterDescription aDescription = new RapidMinerParameterDescription();
			List<String> choices = new ArrayList<String>();
			boolean use = false;
		
			aDescription.setUseParameter(use);
			for (int j = 0; j < returnList.getLength(); j++) {	// for each of "returns" children check..
				
				if (returnList.item(j).getNodeName().equals("name")) {
					//System.out.println(" names " +getCharacterDataFromElement((Element)returnList.item(j)));
					aDescription.setParameterName(getCharacterDataFromElement((Element)returnList.item(j)));
				}
				
				if (returnList.item(j).getNodeName().equals("description")) {
					//System.out.println(" descriptions " +getCharacterDataFromElement((Element)returnList.item(j)));
					aDescription.setDescription(getCharacterDataFromElement((Element)returnList.item(j)));

				}
				
				if (returnList.item(j).getNodeName().equals("expert")) {
					//System.out.println(" expert " +getCharacterDataFromElement((Element)returnList.item(j)));
					aDescription.setExpert(getCharacterDataFromElement((Element)returnList.item(j)));
				}
				
				if (returnList.item(j).getNodeName().equals("mandatory")) {
					//System.out.println(" mandatory " +getCharacterDataFromElement((Element)returnList.item(j)));
					aDescription.setMandatory(getCharacterDataFromElement((Element)returnList.item(j)));

				}
				
				if (returnList.item(j).getNodeName().equals("max")) {
					//System.out.println(" max " +getCharacterDataFromElement((Element)returnList.item(j)));
					aDescription.setMax(getCharacterDataFromElement((Element)returnList.item(j)));

				}
				
				if (returnList.item(j).getNodeName().equals("min")) {
					//System.out.println(" min " +getCharacterDataFromElement((Element)returnList.item(j)));
					aDescription.setMin(getCharacterDataFromElement((Element)returnList.item(j)));

				}
				
				if (returnList.item(j).getNodeName().equals("defaultValue")) {
					//System.out.println(" defaultValue " +getCharacterDataFromElement((Element)returnList.item(j)));
					aDescription.setDefaultValue(getCharacterDataFromElement((Element)returnList.item(j)));
				}
				
				if (returnList.item(j).getNodeName().equals("type")) {
					//System.out.println(" type " +getCharacterDataFromElement((Element)returnList.item(j)));
					aDescription.setType(getCharacterDataFromElement((Element)returnList.item(j)));

				}
				
				if (returnList.item(j).getNodeName().equals("choices")) {
					//System.out.println(" choices " +getCharacterDataFromElement((Element)returnList.item(j)));
					choices.add(getCharacterDataFromElement((Element)returnList.item(j)));
				}
		
			}
			
			aDescription.setChoices(choices);
			listOfDescriptions.add(aDescription);
		}
		
		// sort before returning
		Collections.sort(listOfDescriptions);
		
		return listOfDescriptions;
		
	
	}
	
	protected void configurePorts() {
		// In case we are being reconfigured - remove existing ports first
		// to avoid duplicates
		removeInputs();
		removeOutputs();
		/*[important]
		try {
			createXMLDocument();

		} catch (Exception e) {
			e.printStackTrace();
		}
		// FIXME: Replace with your input and output port definitions
		 * 
		 * 
				*/
		
		
		
		if (configBean.getIsExplicit()) {
			
			//check if both input and output locations are set
			
			if(configBean.getInputLocation().equals("") && configBean.getOutputLocation().equals("")) {
				
				System.out.println(" NO INPUT OR OUTPUT LOCATIONS SPECIFIED >>>>* SHOW BOTH PORTS");
				removeInputs();
				portListing.clear();
				portListing.add("inputLocation");
				portListing.add("outputLocation");
				
			} else {	// both are filled in 
			
				System.out.println(" BOTH PORTS ARE SPECIFIED >>>>>>* REMOVING ALL PORTS ");
				removeInputs();
				portListing.clear();
			}
			
			if (configBean.getInputLocation().equals("") && !configBean.getOutputLocation().equals("")) {	// only input is filled in
				
				System.out.println(" ONLY OUTPUT LOCATION IS SPECIFIED >>>>* SHOW INPUT PORT ONLY");
				removeInputs();
				portListing.clear();
				portListing.add("inputLocation");
			
			}
			
			if (!configBean.getInputLocation().equals("") && configBean.getOutputLocation().equals("")) {	// only output is filled in
				
				System.out.println(" ONLY INPUT LOCATION IS SPECIFIED >>>>* SHOW OUTPUT PORT ONLY");
				removeInputs();
				portListing.clear();
				portListing.add("outputLocation");
				
			}
						
			
		} else {	// Implicit
				
			
		}
		
		Iterator inputIterator = portListing.iterator();
		
		while (inputIterator.hasNext()) {
			
			//System.out.println("port " + inputIterator.next());
			addInput((String)inputIterator.next(), 0, true, null, String.class);
			
		}

		// Hard coded input port, expecting a single String
		//addInput(IN_FIRST_INPUT, 0, true, null, String.class);

		// Optional ports depending on configuration
		if (configBean.getOperatorName().equals("specialCase")) {
			// depth 1, ie. list of binary byte[] arrays
			addInput(IN_EXTRA_DATA, 1, true, null, byte[].class);
			addOutput(OUT_REPORT, 0);
		}
		
		// Single value output port (depth 0)
		addOutput(OUT_SIMPLE_OUTPUT, 0);
		// Output port with list of values (depth 1)
		//addOutput(OUT_MORE_OUTPUTS, 1);

	}
	
	public Map<Object, String> constructInvocationInputMap(String inputLocation, String outputLocation) {
		
		// add parameters to the hashmap from Parameter Configurations
		HashMap<String, String> params = new HashMap<String, String>();
		
			List<RapidMinerParameterDescription>  paramDescriptions = configBean.getParameterDescriptions();
		
			Iterator paramIterator = paramDescriptions.iterator();
			
			while (paramIterator.hasNext()) {
				
				// check whether the current parameter is Use (true)
				RapidMinerParameterDescription des = (RapidMinerParameterDescription)paramIterator.next();
				
				if (des.getUseParameter()) {	// true
					
					params.put(des.getParameterName(), des.getExecutionValue());
					
				}
				
			}
			System.out.println(" THE PARAMETERS AND THEIR VALUES : " + params.toString());
			
		String inputDoc;
		//String executorType, String operatorName, HashMap<String, String> operatorParameters, String inputLocations, String outputLocations
		inputDoc = createInputDocument("executeBasicOperatorExplicitOutput", configBean.getCallName(), params, inputLocation, outputLocation);
		
		Map<Object, String> inputMap = new HashMap<Object, String>();
		inputMap.put("parameters", inputDoc);
		
		return inputMap;
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
				
				Map<Object, String> inputMap = null;
				// if explicit
				
					// if both input and output locations are specified - dont get from ports (because they won't exist)

					// if both input and output locations are not specified - get info from ports
				
					// if just input is specified - get the input from the config bean and the output from the port
				
					// if just output is specified - get the output from the configbean and the input from the port
				
				if (configBean.getIsExplicit()) {		// is explicit
					
					// if both input and output locations are specified - dont get from ports (because they won't exist)
					if (!configBean.getInputLocation().equals("") && !configBean.getOutputLocation().equals("")) {
						
						System.out.println(" TEST CASE 1. " + configBean.getInputLocation() + " " + configBean.getOutputLocation());

						inputMap = constructInvocationInputMap(configBean.getInputLocation(), configBean.getOutputLocation());
						
					}
					
					// if both input and output locations are not specified - get info from ports
					if (configBean.getInputLocation().equals("") && configBean.getOutputLocation().equals("")) {
		
						String inputValue = (String) referenceService.renderIdentifier(inputs.get("inputLocation"), String.class, context);
						String outputValue = (String) referenceService.renderIdentifier(inputs.get("outputLocation"), String.class, context);
						
						System.out.println(" TEST CASE 2. " + inputValue + " " + outputValue);

						inputMap = constructInvocationInputMap(inputValue, outputValue);
					}
					
					// if just input is specified - get the input from the config bean and the output from the port
					if (!configBean.getInputLocation().equals("") && configBean.getOutputLocation().equals("")) {
						
						String outputValue = (String) referenceService.renderIdentifier(inputs.get("outputLocation"), String.class, context);
						
						System.out.println(" TEST CASE 3. " + configBean.getInputLocation() + " " + outputValue);
						
						inputMap = constructInvocationInputMap(configBean.getInputLocation(), outputValue);

						
					}
					
					// if just output is specified - get the output from the configbean and the input from the port
					if (configBean.getInputLocation().equals("") && !configBean.getOutputLocation().equals("")) {
						
						String inputValue = (String) referenceService.renderIdentifier(inputs.get("inputLocation"), String.class, context);
						System.out.println(" TEST CASE 4. " + inputValue + " " + configBean.getOutputLocation());
						
						inputMap = constructInvocationInputMap(inputValue, configBean.getOutputLocation());
						
					}
					
					
					
				} else {								// is implicit	
					
										
					
				}
				
			//	String firstInput = (String) referenceService.renderIdentifier(inputs.get(IN_FIRST_INPUT),
            //            String.class, context);

				
			
				
				System.out.println(" +++ ONE +++");
				

				
				System.out.println(" +++ FOUR +++");

				WSDLActivity wrapper = new WSDLActivity();
				WSDLActivityConfigurationBean myBean = new WSDLActivityConfigurationBean();
				myBean.setWsdl("http://rpc295.cs.man.ac.uk:8081/e-LICO/ExecutorService?wsdl");
				myBean.setOperation("executeBasicOperatorExplicitOutput");
				//myBean.setSecurityProfile(securityProfile);
				
				try {
					wrapper.configure(myBean);
				} catch (ActivityConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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
				
				List<String> outputNames = new ArrayList<String>();
				List<String> inputNames = new ArrayList<String>();

				for (OutputPort port: wrapper.getOutputPorts()) {
					outputNames.add(port.getName());
				}
				
				for (InputPort port: wrapper.getInputPorts()) {
					inputNames.add(port.getName());
				}
				System.out.println(" INPUT NAMES ARE " + inputNames.toString() + " " + myBean.getOperation());

				System.out.println(" OUTPUT NAMES ARE " + outputNames.toString() + " " + myBean.getOperation());
				T2WSDLSOAPInvoker invoker = new T2WSDLSOAPInvoker(parser, myBean.getOperation(), outputNames);
				
				// call
				Service service = new Service();
				Call call = null;
				
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
				

				MessageContext context1 = call.getMessageContext();
				context1.setUsername(usernamePassword.getUsername());
				context1.setPassword(usernamePassword.getPasswordAsString());
				usernamePassword.resetPassword();
				
				call.setTargetEndpointAddress("http://rpc295.cs.man.ac.uk:8081/e-LICO/ExecutorService?wsdl");
				call.setOperationName("executeBasicOperatorExplicitOutput");
				
				// end of call

				try {
					Map<String, Object> invokerOutputMap = invoker.invoke(inputMap, call);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			

				/* +++++

				
				WSDLActivity wrapper = new WSDLActivity();
				
				System.out.println(" +++ FIVE +++");

				WSDLActivityConfigurationBean bean = new WSDLActivityConfigurationBean();
				System.out.println(" +++ SIX +++");

				bean.setOperation("executeBasicOperatorExplicitOutput_input");
				System.out.println(" +++ SEVEN +++");

				bean.setWsdl("http://rapid-i.dyndns.org:8080/e-LICO/ExecutorService");
				System.out.println(" +++ EIGHT +++");

				bean.setSecurityProfile(securityProfile);
				System.out.println(" +++ NINE +++");
				
				List<String> outputNames = new ArrayList<String>();
				
				System.out.println(" +++ TEN +++");
				
				for (InputPort port: wrapper.getInputPorts()) {
					
					System.out.println(" INPUT PORTS ARE " + port.getName());
					
				}
				
							
				for (OutputPort port: wrapper.getOutputPorts()) {
					outputNames.add(port.getName());
				}
				System.out.println(" output names " + outputNames.toString());
				System.out.println(" +++ ELEVEN +++");
				
				
				
				wrapper.executeAsynch(invokerInputMap, callback);
		
				System.out.println(" +++ TWELVE +++");

				
				// Support our configuration-dependendent input
				boolean optionalPorts = configBean.getExampleString().equals("specialCase"); 
				
				System.out.println(" +++ THIRTEEN +++");

				
				// test 
				
				List<byte[]> special = null;
				// We'll also allow IN_EXTRA_DATA to be optionally not provided
				if (optionalPorts && inputs.containsKey(IN_EXTRA_DATA)) {
					// Resolve as a list of byte[]
					special = (List<byte[]>) referenceService.renderIdentifier(
							inputs.get(IN_EXTRA_DATA), byte[].class, context);
				}
				
				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
				String simpleValue = "output";
				T2Reference simpleRef = referenceService.register(simpleValue, 0, true, context);
				outputs.put("output", simpleRef);
				
				callback.receiveResult(outputs, new int[0]);
				
				System.out.println(" OUTPUT " + outputs.toString());
				
				
				/*
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
				/*
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
				*/
				
				// return map of output data, with empty index array as this is
				// the only and final result (this index parameter is used if
				// pipelining output)
				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
				String simpleValue = configBean.getOutputLocation();
				T2Reference simpleRef = referenceService.register(simpleValue, 0, true, context);
				outputs.put(OUT_SIMPLE_OUTPUT, simpleRef);
				callback.receiveResult(outputs, new int[0]);
				
			}
		});
	}

	@Override
	public RapidMinerActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}
	
	public String createInputDocument(String executorType, String operatorName, HashMap<String, String> operatorParameters, String inputLocations, String outputLocations) {
		
		// for explicit output
		
		// Root executeBasicOperatorExplicitOutput
		
		org.jdom.Element root = new org.jdom.Element("executeBasicOperatorExplicitOutput","http://elico.rapid_i.com/");
		
		// operatorName
		org.jdom.Element operatorNameElement = new org.jdom.Element("operatorName");
			
			operatorNameElement.setText(operatorName);
		
			root.addContent(operatorNameElement);
			
		// operatorParameters
				
		Iterator keys = operatorParameters.keySet().iterator();
		
		while (keys.hasNext()) 
		{
			// parameters root element
			org.jdom.Element operatorParameterElement = new org.jdom.Element("operatorParameters");
			
			// for this key
			String key, value;
			
			key =  (String) keys.next();
	
			value = operatorParameters.get(key);
			
			// make an element
			org.jdom.Element parameterKey = new org.jdom.Element("key", "");
			
				parameterKey.setText(key);
			
			org.jdom.Element parameterValue = new org.jdom.Element("value", "");
				
				parameterValue.setText(value);
			
			// add it to the element
			operatorParameterElement.addContent(parameterKey);
			operatorParameterElement.addContent(parameterValue);
			
			// add this to the root doc element
			root.addContent(operatorParameterElement);
		}
		
		// input location
		
		org.jdom.Element inputLocationElement = new org.jdom.Element("inputLocations");
		
			inputLocationElement.setText(inputLocations);
			
			root.addContent(inputLocationElement);
			
		// output location
			
		org.jdom.Element outputLocationElement = new org.jdom.Element("outputLocations");
		
			outputLocationElement.setText(outputLocations);
			
			root.addContent(outputLocationElement);
			
		// create the document
		org.jdom.Document myDoc = new org.jdom.Document(root);
		
		// print the contents of the document
		String finalOutput = new String();
		
		try {
			
			System.out.println("THE XML Document OUTPUT : ");
			new XMLOutputter().output(myDoc, System.out);
			finalOutput = new XMLOutputter().outputString(myDoc);
			
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}

		return finalOutput;
		
	}
	
	public String createXMLDocument(String key, String value) {
		org.jdom.Element childElement = new org.jdom.Element("key", "");
		childElement.setText(key);
		
		org.jdom.Element childElement2 = new org.jdom.Element("value", "");
		childElement2.setText(value);
		
		org.jdom.Element root = new org.jdom.Element("operatorParameter", "http://elico.rapid_i.com/");
		root.addContent(childElement);
		root.addContent(childElement2);
		
		org.jdom.Document myDoc = new org.jdom.Document(root);
		String returnVal = new String();
		try {
			new XMLOutputter().output(myDoc, System.out);
			returnVal = new XMLOutputter().outputString(myDoc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return returnVal;
		
	}
	
	
	public String transformOperatorName(String myString) {
		
		//String[] tokens = myString.split("[ ]+");
		//String a;
		//for (int i = 0; i < tokens.length; i++) {
		//	tokens[i].replaceAll(regex, replacement)
		//}
		String updatedName = myString.toLowerCase();
		updatedName = updatedName.replace(" ", "_");
		System.out.println(" UPDATED NAME " + updatedName);
		return updatedName;
	}
	
	public List <String> getParametersForOperation(String operationName) {
		
		//operationName = transformOperatorName(operationName);
		
		System.out.println("^^^Starting tester " + operationName);

		Map<Object, String> inputMap = new HashMap<Object, String>();
		String inputString = "<getParameterTypes xmlns=\"http://elico.rapid_i.com/\"><operatorName xmlns=\"\">" + operationName + "</operatorName></getParameterTypes>";
		inputMap.put("parameters", inputString);
		
		System.out.println("^^^Starting tester2");
		
		// WSDLActivityConfigurationBean
		WSDLActivityConfigurationBean myBean = new WSDLActivityConfigurationBean();
		myBean.setWsdl("http://rpc295.cs.man.ac.uk:8081/e-LICO/ExecutorService?wsdl");
		myBean.setOperation("getParameterTypes");
		
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
		
		WSDLSOAPInvoker myInvoker = new WSDLSOAPInvoker(parser, "getParameterTypes", outputNames);
		
		System.out.println("^^^Point 2");

		
		
		System.out.println("^^^Point 3");
		
		// Create Call Object
		
		Service service = new Service();
		Call call = null;
		
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
		call.setOperationName("getParameterTypes");

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
		
		String finalOutput = "<myroot>" + newOutput +  "</myroot>";;
		
		System.out.println("parsed Parameters " + finalOutput);
		
		// get names for parameters
		NodeList rtnList;
		NodeList children = null;
		
		try { 
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(finalOutput));
			
			Document doc = db.parse(is);
			
			children = doc.getElementsByTagName("name");	//	All children nodes
			myTempList = doc.getElementsByTagName("return");
			
			System.out.println(" The number of children returned by Parameter Types is :" + children.getLength());
			
		} catch (Exception e) {
			
		}
		
		List <String> myParameters = new ArrayList<String>();
		
		for (int i = 0; i < children.getLength(); i++) {
			
			//[confirmed]System.out.println(" the parameters are " + getCharacterDataFromElement((Element)children.item(i)));
			myParameters.add(getCharacterDataFromElement((Element)children.item(i)));
		}
		

		return myParameters;
	}
	
	public List <String> getParameterNames() {
		
		return null;
		
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

// rip elements out of xml

/*	METHOD TO REPLACE
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
*/ 

//this method will need to know what parameters need filling in
/*
Element e = null;
Node n = null;
Document xmlDocument = null;

DocumentBuilderFactory dbf =
    DocumentBuilderFactory.newInstance();
dbf.setNamespaceAware(true);

try {
	DocumentBuilder db = dbf.newDocumentBuilder();
	xmlDocument = db.newDocument();
} catch (ParserConfigurationException e1) {
	// TODO Auto-generated catch block
	e1.printStackTrace();
}
*/

//Element root =  xmlDocument.createElementNS("http://eli", "operatorParameter");
//root.setAttributeNS(null, qualifiedName, value)
//root.setAttributeNS(null, "asd", "namespacevalue");
/*
org.jdom.Element root = new org.jdom.Element("ns", "oper");
e = xmlDocument.createElement("key");


n = xmlDocument.createTextNode("myKey");
e.appendChild(n);
((Node) root).appendChild(e);

	
Element root = xmlDocument.createElement("USERS");
String[] id = {"PWD122","MX787","A4Q45"};
String[] type = {"customer","manager","employee"};
String[] desc = {"Tim@Home","Jack&Moud","John D'oŽ"};
for (int i=0;i<id.length;i++)
{
  // Child i.
  e = xmlDocument.createElementNS(null, "USER");
  e.setAttributeNS(null, "ID", id[i]);
  e.setAttributeNS(null, "TYPE", type[i]);
  n = xmlDocument.createTextNode(desc[i]);
  e.appendChild(n);
  root.appendChild(e);
}


xmlDocument.appendChild((Node) root);
FileOutputStream fos = null;
try {
	fos = new FileOutputStream("myfile123");
} catch (FileNotFoundException e1) {
	// TODO Auto-generated catch block
	e1.printStackTrace();
}
// XERCES 1 or 2 additionnal classes.
OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
of.setIndent(1);
of.setIndenting(true);
of.setDoctype(null,"users.dtd");
XMLSerializer serializer = new XMLSerializer(fos,of);
// As a DOM Serializer
try {
	serializer.asDOMSerializer();
} catch (IOException e1) {
	// TODO Auto-generated catch block
	e1.printStackTrace();
}
try {
	serializer.serialize( xmlDocument.getDocumentElement() );
} catch (IOException e1) {
	// TODO Auto-generated catch block
	e1.printStackTrace();
}
try {
	fos.close();
} catch (IOException e1) {
	// TODO Auto-generated catch block
	e1.printStackTrace();
}
*/

/*
while (myIterator.hasNext()) {
	
	RapidMinerParameterDescription desc = (RapidMinerParameterDescription)myIterator.next();
	System.out.println("\n[VERIFY] name " + desc.getParameterName());
	System.out.println("[VERIFY] description " + desc.getDescription());
	System.out.println("[VERIFY] expert " + desc.getExpert());
	System.out.println("[VERIFY] mandatory " + desc.getMandatory());
	System.out.println("[VERIFY] max " + desc.getMax());
	System.out.println("[VERIFY] min " + desc.getMin());
	System.out.println("[VERIFY] defaultValue " + desc.getDefaultValue());
	System.out.println("[VERIFY] type " + desc.getType());
	System.out.println("[VERIFY] choices " + desc.getChoices().toString());
	
	
}
*/

/*	TESTER SCRIPT
 * 
RapidMinerParameterDescription desc1 = new RapidMinerParameterDescription();
RapidMinerParameterDescription desc2 = new RapidMinerParameterDescription();
desc1.setParameterName("a parameter Name 1");
desc2.setParameterName("a parameter Name 2");
List<RapidMinerParameterDescription> descList = new ArrayList<RapidMinerParameterDescription>();
descList.add(desc1);
descList.add(desc2);
configBean.setParameterDescriptions(descList);

*/

// tester

// tester
/*	WORKING TEST CASE
HashMap<String, String> params = new HashMap<String, String>();
params.put("attribute_type_filter", "single");
params.put("attribute", "a1");
String inputDoc;

inputDoc = createInputDocument(null, "discretize_by_bins", params, "/groups/elico/templates/data/Iris/", "/home/jupp/fromPluginAgain/");

Map<Object, String> inputMap = new HashMap<Object, String>();
inputMap.put("executeBasicOperatorExplicitOutput", inputDoc);
// end of test
*/

/*
// Resolve inputs 				
String oneParam = createXMLDocument("attribute_type_filter","single");
String twoParam = createXMLDocument("attribute","a1");

List<String> operatorParams = new ArrayList<String>();
operatorParams.add(oneParam);
operatorParams.add(twoParam);

System.out.println(" +++ TWO +++");

// ++
Map<String, Object> invokerInputMap = new HashMap<String, Object>();

T2Reference operatorParameters = referenceService.register(operatorParams, 1, true, context);
System.out.println(" +++ THREE +++");

//T2Reference inputLocation  = referenceService.referenceFromString("/groups/elico/templates/data/Iris/");
T2Reference inputLocation  = referenceService.register("/groups/elico/templates/data/Iris/", 0, true, context);

//T2Reference operatorName = referenceService.referenceFromString("discretize_by_bins");
T2Reference operatorName = referenceService.register("discretize_by_bins", 0, true, context);

//T2Reference outputLocations = referenceService.referenceFromString("/home/jupp/fromPlugin/");
T2Reference outputLocations = referenceService.register("/home/jupp/fromPlugin/", 0, true, context);

//String firstInput = (String) referenceService.renderIdentifier(inputs.get(IN_FIRST_INPUT), 
//		String.class, context);


invokerInputMap.put("inputLocations", (Object)inputLocation);
invokerInputMap.put("operatorName", (Object)operatorName);
invokerInputMap.put("operatorParameters", (Object)operatorParameters);
invokerInputMap.put("outputLocations", (Object)outputLocations);

*/
