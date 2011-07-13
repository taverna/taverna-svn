package uk.ac.manchester.cs.elico.rmservicetype.taverna;

import net.sf.taverna.t2.activities.wsdl.T2WSDLSOAPInvoker;
import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.activities.wsdl.security.SecurityProfiles;
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
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.soap.WSDLSOAPInvoker;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import uk.ac.manchester.cs.elico.utilities.configuration.RapidAnalyticsPreferences;
import uk.ac.manchester.cs.elico.utilities.configuration.RapidMinerPluginConfiguration;

import javax.swing.*;
import javax.wsdl.WSDLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.*;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Rishi Ramgolam<br>
 * Date: Jul 13, 2011<br>
 * The University of Manchester<br>
 **/

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
	private static final String OUT_REPORT = "report";

    private UsernamePassword username_password;

	private RapidAnalyticsPreferences preferences;
	
    static String securityProfile = SecurityProfiles.HTTP_BASIC_AUTHN;
		
	private RapidMinerActivityConfigurationBean configBean;
	NodeList myTempList;
    private static final String EXECUTE_BASIC_OPERATOR_EXPLICIT_OUTPUT = "executeBasicOperatorExplicitOutput";
    private static final String HTTP_ELICO_RAPID_I_COM = "http://elico.rapid_i.com/";

    public RapidMinerExampleActivity () {

        setUpUserNamePassword();
    }


    public void setUpUserNamePassword () {


        preferences = getPreferences();
        if (preferences != null) {
            CredentialManager credManager = null;
            try {
                credManager = CredentialManager.getInstance();
                username_password = credManager.getUsernameAndPasswordForService(URI.create(preferences.getExecutorServiceWSDL()), true, null);

                preferences.setUsername(username_password.getUsername());
                preferences.setPassword(username_password.getPasswordAsString());
            } catch (CMException e) {
                e.printStackTrace();

            }
        }
        else {
            JOptionPane.showMessageDialog(new JFrame(),
                    new JLabel("<html>Please set the Rapid Analytics repository location <br> " +
                            "in the preferences panel</html>"));
        }
    }

    private RapidAnalyticsPreferences getPreferences() {

        RapidMinerPluginConfiguration config = RapidMinerPluginConfiguration.getInstance();
        String repos = config.getProperty(RapidMinerPluginConfiguration.RA_REPOSITORY_LOCATION);
        System.err.println("Got repository location: " + repos);
        if (repos.equals("")) {
            return null;
        }

        RapidAnalyticsPreferences pref = new RapidAnalyticsPreferences();
        pref.setRepositoryLocation(repos);
        return pref;

    }

    public RapidMinerExampleActivity (RapidAnalyticsPreferences prefs) {
        preferences = prefs;
        username_password = new UsernamePassword(preferences.getUsername(), preferences.getPasswordAsString());
    }

	@Override
	public void configure(RapidMinerActivityConfigurationBean configBean)
			throws ActivityConfigurationException {

		// Store for getConfiguration(), but you could also make
		// getConfiguration() return a new bean from other sources
		setUpUserNamePassword();
		
		this.configBean = configBean;
       
		//[debug]System.out.println(" THE CALL NAME IS " + this.configBean.getCallName());
		//[debug]System.out.println(" config bean " + configBean.getHasDescriptions() + " configured? " + configBean.getIsParametersConfigured());
		
		List<RapidMinerParameterDescription> descList = new ArrayList<RapidMinerParameterDescription>();	

		if (!configBean.getHasDescriptions()) {
			//[debug]System.out.println(" ALPHA 1");
			
			// does not have descriptions - go fetch them
			if (getParametersForOperation(configBean.getCallName()).size() > 0) {
				
				descList = getParameterDescriptions(myTempList);
				//[debug]System.out.println(" [debug] list value " + descList.size());
				
				configBean.setParameterDescriptions(descList);
				configBean.setHasDescriptions(true);
				
			} else {
				
				configBean.setParameterDescriptions( new ArrayList<RapidMinerParameterDescription>());
				
			}
			
		} else {
			
			if (configBean.getIsParametersConfigured()) {

				//[debug]System.out.println(" ALPHA 2");
				// make sure all the parameter descriptions are retrieved & set execution value

				if (getParametersForOperation(configBean.getCallName()).size() > 0) {
				
					descList = getParameterDescriptions(myTempList);
					//System.out.println(" list value " + descList.size());
					
					configBean.setParameterDescriptions(setExecutionValuesToParameters(descList));
									
				} else {
					
					configBean.setParameterDescriptions( new ArrayList<RapidMinerParameterDescription>());
	
				}
			
			}
		}
			
	
		
		configurePorts();
		
		/*
		if (configBean.getHasDescriptions()) {
			
			configurePorts();
			
		} else {

            List<RapidMinerParameterDescription> descList;

            if (getParametersForOperation(configBean.getCallName()).size() == 0) {
                descList = new ArrayList<RapidMinerParameterDescription>();
            }
            else {
                descList = getParameterDescriptions(myTempList);
            }


			if (!configBean.getIsParametersConfigured()) {
				
				configBean.setParameterDescriptions(descList);
				
			}
			
			// REQUIRED: (Re)create input/output ports depending on configuration
			configurePorts();
			configBean.setHasDescriptions(true);
			
		}
		*/
	}
	
	public List<RapidMinerParameterDescription> setExecutionValuesToParameters(List<RapidMinerParameterDescription> allDescriptions) {
		
		HashMap<String, String> parameterNameToExecutionValue = new HashMap<String, String>();
		List<RapidMinerParameterDescription> presetListOfDescriptions = configBean.getParameterDescriptions();
		
		for (RapidMinerParameterDescription aDescription : presetListOfDescriptions) {
			
			if (aDescription.getUseParameter()) {
				parameterNameToExecutionValue.put(aDescription.getParameterName(), aDescription.getExecutionValue());
			}
			
		}
		
		for (RapidMinerParameterDescription aDescription : allDescriptions) {
			
			String paramName = aDescription.getParameterName();
			
			if (parameterNameToExecutionValue.containsKey(paramName)) {
				
				//[debug]System.out.println( " MATCH --> setting USE and Execution Value");
				aDescription.setExecutionValue(parameterNameToExecutionValue.get(paramName));
				aDescription.setUseParameter(true);

			}
			
		}
		
		return allDescriptions;
	}

	public List<RapidMinerParameterDescription> getParameterDescriptions(NodeList myList) {
		
		// [for programmatic access] check whether the config bean already has any RapidMinerParameterDescriptions set

		
		//  parse into a list
		//[debug]System.out.println(" number of RETURNS " + myList.getLength());
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
					String paramName = getCharacterDataFromElement((Element)returnList.item(j));
					aDescription.setParameterName(paramName);
							
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
			
			// HACK <-- split_data returns no defaultValue element
			if (aDescription.getParameterName().equals("partitions") && configBean.getCallName().equals("split_data")) {
				aDescription.setDefaultValue("?");
			}
			
			aDescription.setChoices(choices);
			listOfDescriptions.add(aDescription);
		}
		
		// sort before returning
		Collections.sort(listOfDescriptions);
		configBean.setHasDescriptions(true);
		return listOfDescriptions;
		
	
	}
	
	protected void configurePorts() {
		// In case we are being reconfigured - remove existing ports first
		// to avoid duplicates
		removeInputs();
		removeOutputs();
		

        for (IOInputPort inputP : configBean.getInputPorts().values()) {
           // if (inputP.getFileLocationAt(0) == null || inputP.getFileLocationAt(0).isEmpty()) {	//REDO -done
           //     addInput(inputP.getPortName(), 0, true, null, String.class);
           // }
            
            // go through each port and check whether there mulitple file locataions
            // add inputs to empty file locations
            int iP = inputP.getNumberOfPorts();
            List<String> inputLocations = inputP.getFileLocations();
            for (int i = 0 ; i < iP; i++) {
            	
            	if (inputLocations.get(i) == null || inputLocations.get(i).isEmpty()) {
            		
            		addInput(inputP.getPortName() + i, 0, true, null, String.class);
            		
            	}
            	
            }
            
        }

        for (IOOutputPort outputP : configBean.getOutputPorts().values()) {
        	
        	// for every outputP - get the setText and parse
        	/*List<String> outputLocations = parseOutputLocationsToList(outputP.getFileLocation());
        	int i = 0;
        	for (String output : outputLocations) {
        		
        		addOutput(outputP.getPortName() + i, 0);
        		i++;
        	}
            */
        	//if (outputP.getFileLocationAt(0) == null || outputP.getFileLocationAt(0).isEmpty()) {	//REDO -done
            //    addOutput(outputP.getPortName(), 0);
           // }
        	// go through each port and check whether there mulitple file locataions
            // add inputs to empty file locations
            int iP = outputP.getNumberOfPorts();
            List<String> outputLocations = outputP.getFileLocations();
            for (int i = 0 ; i < iP; i++) {
            	
            	//if (outputLocations.get(i) == null || outputLocations.get(i).isEmpty()) {
            		
            		addOutput(outputP.getPortName() + i, 0);
            		
            	//}
            	
            }
        		
        }

		// Optional ports depending on configuration
		//		if (configBean.getOperatorName().equals("specialCase")) {
		//			// depth 1, ie. list of binary byte[] arrays
		//			addInput(IN_EXTRA_DATA, 1, true, null, byte[].class);
		//			addOutput(OUT_REPORT, 0);
		//		}
				
				// Single value output port (depth 0)
		//		addOutput(OUT_SIMPLE_OUTPUT, 0);
		// Output port with list of values (depth 1)
		// addOutput(OUT_MORE_OUTPUTS, 1);

	}
	
	public Map<Object, String> constructInvocationInputMap() {
		
		// add parameters to the hashmap from Parameter Configurations
		HashMap<String, String> params = new HashMap<String, String>();

        List<RapidMinerParameterDescription>  paramDescriptions = configBean.getParameterDescriptions();

        for (RapidMinerParameterDescription paramDescription : paramDescriptions) {

            // check whether the current parameter is Use (true)
            RapidMinerParameterDescription des = paramDescription;

            if (des.getUseParameter()) {    // true

                params.put(des.getParameterName(), des.getExecutionValue());

            }

        }

      //[debug]System.out.println(" THE PARAMETERS AND THEIR VALUES : " + params.toString());

        String inputDoc = createInputDocument(configBean.getCallName(), params, configBean.getInputPorts(), configBean.getOutputPorts());

        Map<Object, String> inputMap = new HashMap<Object, String>();

        inputMap.put(EXECUTE_BASIC_OPERATOR_EXPLICIT_OUTPUT, inputDoc);
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
				
				setUpUserNamePassword();
				
				InvocationContext context = callback
						.getContext();
				ReferenceService referenceService = context
						.getReferenceService();
				
				Map<Object, String> inputMap = null;

				// if both inputs are not specified, get from incoming connection

                String filePath = "/demo/";

                if (!preferences.getUsername().isEmpty()) {
                    filePath = "/home/" + preferences.getUsername() + "/tmp/";

                }
                /*	OLD POST MULITPLE INPUT LOCATIONS IMPLEMENTATION
                LinkedHashMap<String, IOInputPort> inputPorts = configBean.getInputPorts();
                for (String key  : inputPorts.keySet()) {
                    if (inputPorts.get(key).getFileLocationAt(0) == null || inputPorts.get(key).getFileLocationAt(0).trim().equals("")) {	// REDO
                        try {
                        String inputValue = (String) referenceService.renderIdentifier(inputs.get(key), String.class, context);
                        configBean.getInputPorts().get(key).setFileLocationAt(0, inputValue);												// REDO
                        }
                        catch (NullPointerException e) {
//                            callback.fail("You must specify some inputs for this service", e);
//                            e.printStackTrace();

                        }
                    }
                    else {
                        // find the base path to this file and use that
                        String currentFile = inputPorts.get(key).getFileLocationAt(0);														// REDO
                        int lastSlash = currentFile.lastIndexOf("/");
                        filePath = currentFile.substring(0, lastSlash + 1);

                    }}
                    */
                    // for each each input port
                	LinkedHashMap<String, IOInputPort> inputPorts = configBean.getInputPorts();
                  
                	  for (String akey  : inputPorts.keySet()) {
                      	
                      	List<String> fileLocations = inputPorts.get(akey).getFileLocations();
                      	int iP = inputPorts.get(akey).getNumberOfPorts();
                      	
                      	for (int i = 0; i < iP; i++) {	// for each file location
                      		
                      		if (fileLocations.get(i) == null || fileLocations.get(i).trim().equals("")) {
                      			
                      			try {
                      				
                                      String inputValue = (String) referenceService.renderIdentifier(inputs.get(akey), String.class, context);
                                      configBean.getInputPorts().get(akey).setFileLocationAt(i, inputValue);	
                      				
                      			} catch (NullPointerException e) {
                      				
                      			}
                      			
                      		} else {
                      			
                      			// find the base path to this file and use that
                                  String currentFile = inputPorts.get(akey).getFileLocationAt(i);														// REDO
                                  int lastSlash = currentFile.lastIndexOf("/");
                                  filePath = currentFile.substring(0, lastSlash + 1);
                      			
                      		}
                      		
                      	}
                      	
                      }
                


                // if outputs not specified, create random output file names

                LinkedHashMap<String, IOOutputPort> outputPorts = configBean.getOutputPorts();
                for (String key  : outputPorts.keySet()) {
                	
                	/* OLD POST MULITPLE OUTPUT LOCATIONS IMPLEMENTATION
                    if (outputPorts.get(key).getFileLocationAt(0)== null || outputPorts.get(key).getFileLocationAt(0).trim().equals("")) {	// REDO

                        // create a temp file name
                        
                        String opName = configBean.getOperatorName();
                        int random = (int)(Math.random() * 9999)+1000;
                        System.err.println("am i here!! without class.. key" + key);
                        String outputFile = filePath + key + "_"+  opName.replace(" ", "_").toLowerCase() + "_" + outputPorts.get(key).getPortClass() + "_" + random;
                        configBean.getOutputPorts().get(key).setFileLocationAt(0,outputFile);												// REDO

                    }
                    */
                    // for each output
                    int iP = outputPorts.get(key).getNumberOfPorts();
                    List<String> outputLocations =  outputPorts.get(key).getFileLocations();
                    
                    for (int i = 0; i < iP; i++) {
                    	
                    	if (outputLocations.get(i) == null || outputLocations.get(i).trim().equals("")) {
                    		
                    		// create a temp file name
                            
                            String opName = configBean.getOperatorName();
                            int random = (int)(Math.random() * 9999)+1000;
                            System.err.println("am i here!! without class.. key" + key);
                            String outputFile = filePath + key + "_"+  opName.replace(" ", "_").toLowerCase() + "_" + outputPorts.get(key).getPortClass() + "_" + random;
                            configBean.getOutputPorts().get(key).setFileLocationAt(i,outputFile);	
                    		
                    	}
                    	
                    }
                    

                }

					// if just input is specified - get the input from the config bean and the output from the port
				
					// if just output is specified - get the output from the configbean and the input from the port


                inputMap = constructInvocationInputMap();

              //[debug]System.out.println(" +++ ONE +++ ");
              //[debug]System.out.println(" +++ FOUR +++ ");

				WSDLActivity wrapper = new WSDLActivity();
				WSDLActivityConfigurationBean myBean = new WSDLActivityConfigurationBean();
				myBean.setWsdl(preferences.getExecutorServiceWSDL());
                myBean.setOperation(EXECUTE_BASIC_OPERATOR_EXPLICIT_OUTPUT);
		
				try {
					wrapper.configure(myBean);
				} catch (ActivityConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				WSDLParser parser = null;
				
				try {
					
					 parser = new WSDLParser(myBean.getWsdl());
					 
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
				
				//[debug]System.out.println(" INPUT NAMES ARE " + inputNames.toString() + " " + myBean.getOperation());

				//[debug]System.out.println(" OUTPUT NAMES ARE " + outputNames.toString() + " " + myBean.getOperation());
			
				T2WSDLSOAPInvoker invoker = new T2WSDLSOAPInvoker(parser, myBean.getOperation(), outputNames);
				
				// call
				Service service = new Service();
				Call call = null;
				
				try {
					
					call = (Call)service.createCall();
					
				} catch (ServiceException e3) {
					
					e3.printStackTrace();
					
				}
				
				//[debug]System.out.println("^^^Point 4");
				
				// Set Username and Password (credential manager)
					
				//[debug]System.out.println("^^^Point 5");

				MessageContext context1 = call.getMessageContext();
				context1.setUsername(username_password.getUsername());
				context1.setPassword(username_password.getPasswordAsString());
				username_password.resetPassword();
				
				call.setTargetEndpointAddress(preferences.getExecutorServiceWSDL());
				call.setOperationName(EXECUTE_BASIC_OPERATOR_EXPLICIT_OUTPUT);
				
				// end of call
				Map<String, Object> invokerOutputMap = null;
				try {
					
					//[debug]System.out.println(" INPUT MAP CONTENTS " + inputMap.toString());
					invokerOutputMap = invoker.invoke(inputMap, call);
					//[debug]System.out.println(" [DEBUG] OUTPUT MAP CONTENTS " + invokerOutputMap.toString());

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			


				// return map of output data, with empty index array as this is
				// the only and final result (this index parameter is used if
				// pipelining output)
							
				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
				
//				T2Reference simpleRef = null;
//				if (configBean.getIsExplicit()) {
//
//					String simpleValue = configBean.getOutputLocation();
//					simpleRef = referenceService.register(simpleValue, 0, true, context);
//
//				} else {
//
//                    String resultantOutputLocation = getOutputLocationfromOutputResult(invokerOutputMap);
//					simpleRef = referenceService.register(resultantOutputLocation, 0, true, context);
//
//				}

                for (String opName : configBean.getOutputPorts().keySet()) {
                	
                   // String outputsForOutputPort = configBean.getOutputPorts().get(opName).getFileLocationAt(0);	// REDO
                   // List<String> outputLocs = parseOutputLocationsToList(outputsForOutputPort);
                    List<String> outputLocs = configBean.getOutputPorts().get(opName).getFileLocations();
                    int i = 0;
                    for (String output : outputLocs) {
                    	
                    	//[debug]System.out.println(" output --> " + configBean.getOutputPorts().get(opName).getPortName() + i);
                    	T2Reference simpleRef = referenceService.register(output, 0, true, context);

                        outputs.put(configBean.getOutputPorts().get(opName).getPortName() + i, simpleRef);
                        i++;
                    }
    				
                    

                }
//				outputs.put(OUT_SIMPLE_OUTPUT, simpleRef);
				callback.receiveResult(outputs, new int[0]);
				
			}
		});
	}

	@Override
	public RapidMinerActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}
	
	public String createInputDocument(String operatorName, HashMap<String, String> operatorParameters,
                                      LinkedHashMap<String, IOInputPort> inputPorts, LinkedHashMap<String, IOOutputPort> outputPorts) {
		
		// for explicit output
		
		// Root executeBasicOperatorExplicitOutput
		org.jdom.Element root;
		
     	root = new org.jdom.Element(EXECUTE_BASIC_OPERATOR_EXPLICIT_OUTPUT, HTTP_ELICO_RAPID_I_COM);


		// operatorName
		org.jdom.Element operatorNameElement = new org.jdom.Element("operatorName");
			
			operatorNameElement.setText(operatorName);
		
			root.addContent(operatorNameElement);
			
		// operatorParameters

        for (String s : operatorParameters.keySet()) {
            // parameters root element
            org.jdom.Element operatorParameterElement = new org.jdom.Element("operatorParameters");

            // for this key
            String key, value;

            key = s;

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

        for (String ip : inputPorts.keySet()) {
        	/*
            org.jdom.Element inputLocationElement = new org.jdom.Element("inputLocations");

            System.err.println("looking up input port key: " + ip);
            inputLocationElement.setText(inputPorts.get(ip).getFileLocationAt(0));							// REDO

            root.addContent(inputLocationElement);
            */
            // NEW for this input port, find it's locations
            int iP = inputPorts.get(ip).getNumberOfPorts();
            List<String> inputLocations = inputPorts.get(ip).getFileLocations();
            
            for (String input : inputLocations) {
            	
            	org.jdom.Element inputLocationElement = new org.jdom.Element("inputLocations");
            	System.err.println("looking up input port key: " + ip);
            	
            	inputLocationElement.setText(input);
            	root.addContent(inputLocationElement);
            }
            
        }

        for (String op : outputPorts.keySet()) {

            System.err.println("looking up output port key: " + op);
            
            //String wholeOutputText = outputPorts.get(op).getFileLocationAt(0);								// REDO
           
            //List<String> outputs = parseOutputLocationsToList(wholeOutputText);
            List<String> outputs = outputPorts.get(op).getFileLocations();
            for (String output : outputs) {
                
            	org.jdom.Element outputLocationElement = new org.jdom.Element("outputLocations");

                outputLocationElement.setText(output);

                root.addContent(outputLocationElement);

            }

        }


//		// output location
//		// only give the output location if it's explicity specified
//		if (executorType.equals(EXECUTE_BASIC_OPERATOR_EXPLICIT_OUTPUT)) {
//
//			org.jdom.Element outputLocationElement = new org.jdom.Element("outputLocations");
//
//			outputLocationElement.setText(outputLocations);
//
//			root.addContent(outputLocationElement);
//
//		}
		
		// create the document
		org.jdom.Document myDoc = new org.jdom.Document(root);
		
		// print the contents of the document
		String finalOutput = "";
		
		try {
			
			//[debug]System.out.println("THE XML Document OUTPUT : ");
			new XMLOutputter().output(myDoc, System.out);
			finalOutput = new XMLOutputter().outputString(myDoc);
			
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}

		return finalOutput;
		
	}
	
	public List<String> parseOutputLocationsToList(String wholeOutputLocation) {
		
		List<String> outputLocations = new ArrayList<String>();

		String [] outputs = wholeOutputLocation.split(",");
		
		for (String outputFile : outputs) {
			
			outputLocations.add(outputFile);
			//[debug]System.out.println(" output location --> " + outputFile);
		}
		
		return outputLocations;
		
	}
	
	public String createXMLDocument(String key, String value) {
		
		org.jdom.Element childElement = new org.jdom.Element("key", "");
		childElement.setText(key);
		
		org.jdom.Element childElement2 = new org.jdom.Element("value", "");
		childElement2.setText(value);
		
		org.jdom.Element root = new org.jdom.Element("operatorParameter", HTTP_ELICO_RAPID_I_COM);
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
	
	public String getOutputLocationfromOutputResult(Map<String, Object> invokerOutputMap) {
		
		String xmlOutput = (String) invokerOutputMap.get("executeBasicOperatorImplicitOutputResponse");
		//[debug]System.out.println(" the resultant xml output is " + xmlOutput);
		
		NodeList children = null;
		
		try { 
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlOutput));
			
			Document doc = db.parse(is);
			
			children = doc.getElementsByTagName("return");	//	All children nodes
			
			
			//[debug]System.out.println(" number of returns from XML : " + children.getLength());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		String output = getCharacterDataFromElement((Element)children.item(0));
		//[debug]System.out.println(" FINAL IMPLICIT OUTPUT " + output);
		return output;
		
	}

	public String transformOperatorName(String myString) {
		
		//String[] tokens = myString.split("[ ]+");
		//String a;
		//for (int i = 0; i < tokens.length; i++) {
		//	tokens[i].replaceAll(regex, replacement)
		//}
		
		String updatedName = myString.toLowerCase();
		updatedName = updatedName.replace(" ", "_");
		//[debug]System.out.println(" UPDATED NAME " + updatedName);
		return updatedName;
		
	}
	
	public List <String> getParametersForOperation(String operationName) {
		
		//operationName = transformOperatorName(operationName);
		
		//[debug]System.out.println("^^^Starting tester " + operationName);

		Map<Object, String> inputMap = new HashMap<Object, String>();
		String inputString = "<getParameterTypes xmlns=\"http://elico.rapid_i.com/\"><operatorName xmlns=\"\">" + operationName + "</operatorName></getParameterTypes>";
		inputMap.put("getParameterTypes", inputString);
		
		//[debug]System.out.println("^^^Starting tester2");
		
		// WSDLActivityConfigurationBean
		WSDLActivityConfigurationBean myBean = new WSDLActivityConfigurationBean();
		myBean.setWsdl(preferences.getExecutorServiceWSDL());
		myBean.setOperation("getParameterTypes");
		
		// Output and Parser for WSDLSOAPInvoker
		List<String> outputNames = new ArrayList<String>();
		outputNames.add("attachmentList");
		outputNames.add("getParameterTypesResponse");
		
		//[debug]System.out.println("^^^Point 1");
		
		WSDLParser parser = null;
		
		try {
			
			 parser = new WSDLParser(myBean.getWsdl());
			 
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

		//[debug]System.out.println("^^^Point 2");
	
		//[debug]System.out.println("^^^Point 3");
		
		// Create Call Object
		
		Service service = new Service();
		Call call = null;
		
		try {
			call = (Call)service.createCall();
		} catch (ServiceException e3) {
			e3.printStackTrace();
		}
		
		//[debug]System.out.println("^^^Point 4");
		
		// Set Username and Password (credential manager)


		//[debug]System.out.println("^^^Point 5");
		
		MessageContext context = call.getMessageContext();
		context.setUsername(username_password.getUsername());
		context.setPassword(username_password.getPasswordAsString());
		username_password.resetPassword();
		
		//[debug]System.out.println("^^^Point 6");
	
		// Set wsdl endpoint address and operation name
		
		//[debug]System.out.println("^^^Point 7");

		call.setTargetEndpointAddress(preferences.getExecutorServiceWSDL());
		call.setOperationName("getParameterTypes");

		//[debug]System.out.println("^^^Point 8");
		
		// Invoke 
		Map<String, Object> myOutputs = new HashMap<String, Object>();
	
		try {
			myOutputs = myInvoker.invoke(inputMap,call);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//[debug]System.out.println("^^^Point 9");
		
		//[debug]System.out.println("Complete");
		//[debug]System.out.println( "NEW NEW NEW NEW PARAMETERS " + myOutputs.toString());

		// Parse stuff
		String myOutput = myOutputs.toString();
		int a, b;
		a = myOutput.indexOf("<return>");
		//[debug]System.out.println(" number : " + a);

        if (a == -1) {
            return new ArrayList<String>();
        }
		b = myOutput.indexOf("</return></");



		//[debug]System.out.println(" second number : " + b);
		b += 9;
		String newOutput = myOutput.substring(a, b);
		
		String finalOutput = "<myroot>" + newOutput +  "</myroot>";
		
		//[debug]System.out.println("parsed Parameters " + finalOutput);
		
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
			
			//[debug]System.out.println(" The number of children returned by Parameter Types is :" + children.getLength());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
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


        UsernamePassword username_password = new UsernamePassword();
        username_password.setUsername(preferences.getUsername());
        username_password.setPassword(preferences.getPassword());

        // Try to get username and password for this service from Credential
        // Manager (which should pop up UI if needed)
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
