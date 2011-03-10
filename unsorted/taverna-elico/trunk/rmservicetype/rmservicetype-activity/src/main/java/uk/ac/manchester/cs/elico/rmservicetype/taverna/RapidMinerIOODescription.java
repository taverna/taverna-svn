package uk.ac.manchester.cs.elico.rmservicetype.taverna;

import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.soap.WSDLSOAPInvoker;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.config.RapidMinerPluginConfiguration;

import javax.swing.*;
import javax.wsdl.WSDLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.*;/*
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
 * Author: Simon Jupp<br>
 * Date: Mar 9, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class RapidMinerIOODescription {


    private UsernamePassword username_password;

	private RapidAnalyticsPreferences preferences;

    public LinkedHashMap<String, IOInputPort> getInputPort() {
        return inputPort;
    }

    public LinkedHashMap<String, IOOutputPort> getOutputPort() {
        return outputPort;
    }

    private LinkedHashMap<String, IOInputPort> inputPort = new LinkedHashMap<String, IOInputPort>();
    private LinkedHashMap<String, IOOutputPort> outputPort = new LinkedHashMap<String, IOOutputPort>();

    public void setUpUserNamePassword () {


        preferences = getPreferences();
        if (preferences != null) {
            CredentialManager credManager = null;
            try {
                credManager = CredentialManager.getInstance();
                username_password = credManager.getUsernameAndPasswordForService(URI.create(preferences.getExecutorServiceWSDL()), true, null);

                preferences.setUsername(username_password.getUsername());
                preferences.setUsername(username_password.getPasswordAsString());
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

    public RapidMinerIOODescription (String operatorName) {

        System.out.println("Getting IOO descriptions for " + operatorName);

        Map<Object, String> inputMap = new HashMap<Object, String>();
        String inputString = "<getIODescription xmlns=\"http://elico.rapid_i.com/\"><operatorName xmlns=\"\">" + operatorName + "</operatorName></getIODescription>";
        inputMap.put("getIODescription", inputString);

        System.out.println("Starting get io description wsdl config");

        // WSDLActivityConfigurationBean
        setUpUserNamePassword();
        WSDLActivityConfigurationBean myBean = new WSDLActivityConfigurationBean();
        myBean.setWsdl(preferences.getExecutorServiceWSDL());
        myBean.setOperation("getIODescription");

        // Output and Parser for WSDLSOAPInvoker
        List<String> outputNames = new ArrayList<String>();
        outputNames.add("attachmentList");
        outputNames.add("getIODescriptionResponse");

        System.out.println("^^^Point 1 IO");

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

        WSDLSOAPInvoker myInvoker = new WSDLSOAPInvoker(parser, "getIODescription", outputNames);

        // Create Call Object

        Service service = new Service();
        Call call = null;

        try {
            call = (Call)service.createCall();
        } catch (ServiceException e3) {
            e3.printStackTrace();
        }


        MessageContext context = call.getMessageContext();
		context.setUsername(username_password.getUsername());
		context.setPassword(username_password.getPasswordAsString());
		username_password.resetPassword();

		System.out.println("^^^Point 6");

		// Set wsdl endpoint address and operation name

		System.out.println("^^^Point 7");

		call.setTargetEndpointAddress(preferences.getExecutorServiceWSDL());
		call.setOperationName("getIODescription");

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
		System.out.println( "NEW IO port description " + myOutputs.toString());

		// Parse stuff
		String myOutput = myOutputs.toString();

        		int a, b;
		a = myOutput.indexOf("<return>");
		System.out.println(" number : " + a);

        if (a > -1) {

            b = myOutput.indexOf("</return></");



            System.out.println(" second number : " + b);
            b += 9;
            String newOutput = myOutput.substring(a, b);

            String finalOutput = "<myroot>" + newOutput +  "</myroot>";

            System.out.println("parsed Parameters " + finalOutput);

            // get names for parameters

            try {

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(finalOutput));

                Document doc = db.parse(is);

                int iL = doc.getElementsByTagName("inputPortDescription").getLength();
                for (int x = 0; x<iL ; x++ ) {
                    Node className = doc.getElementsByTagName("inputPortDescription").item(x).getFirstChild();
                    Node portName = doc.getElementsByTagName("inputPortDescription").item(x).getLastChild();
                    IOInputPort tmpInputPort = new IOInputPort(getCharacterDataFromElement((Element)className),
                            getCharacterDataFromElement((Element)portName),
                            null);

                    System.out.println("New Input port created: " + tmpInputPort.getClassName() + " -> " + tmpInputPort.getPortName());
                    inputPort.put(tmpInputPort.getPortName(), tmpInputPort);
                }

                int oL = doc.getElementsByTagName("outputPortDescription").getLength();
                for (int x = 0; x<oL ; x++ ) {
                    Node className = doc.getElementsByTagName("outputPortDescription").item(x).getFirstChild();
                    Node portName = doc.getElementsByTagName("outputPortDescription").item(x).getLastChild();
                    IOOutputPort tmpOutputPort = new IOOutputPort(getCharacterDataFromElement((Element)className),
                            getCharacterDataFromElement((Element)portName),
                            null);
                    System.out.println("New Output port created: " + tmpOutputPort.getClassName() + " -> " + tmpOutputPort.getPortName());
                    outputPort.put(tmpOutputPort.getPortName(), tmpOutputPort);
                }




            } catch (Exception e) {

                e.printStackTrace();

            }

        }

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
