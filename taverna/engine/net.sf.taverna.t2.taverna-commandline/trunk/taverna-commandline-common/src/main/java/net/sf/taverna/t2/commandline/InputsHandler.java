/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.commandline;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.commandline.exceptions.InputMismatchException;
import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;
import net.sf.taverna.t2.commandline.exceptions.ReadInputException;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;

import org.apache.commons.io.IOUtils;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class InputsHandler {
	

	public void checkProvidedInputs(List<? extends DataflowInputPort> list, CommandLineOptions options) throws InputMismatchException  {
		//we dont check for the document 
		if (options.getInputDocument()==null) {
			List<String> providedInputNames = new ArrayList<String>();
			for (int i=0;i<options.getInputs().length;i+=2) {
				providedInputNames.add(options.getInputs()[i]);								
			}
			
			List<String> portNames = new ArrayList<String>();
			for (DataflowInputPort port : list) {
				portNames.add(port.getName());
			}
			
			if (list.size()*2!=options.getInputs().length) {
				throw new InputMismatchException("The number of inputs provided does not match the number of input ports.",portNames,providedInputNames);
			}
			
			for (String portName : portNames) {
				if (!providedInputNames.contains(portName)) {
					throw new InputMismatchException("The provided inputs does not contain an input for the port '"+portName+"'",portNames,providedInputNames);
				}
			}
		}
	}

	public Map<String, WorkflowDataToken> registerInputs(CommandLineOptions options,
			InvocationContext context) throws InvalidOptionException, ReadInputException  {
		Map<String,WorkflowDataToken> inputs = new HashMap<String, WorkflowDataToken>();
		URL url;
		try {
			url = new URL("file:");
		} catch (MalformedURLException e1) {
			//Should never happen, but just incase:
			throw new ReadInputException("The was an internal error setting up the URL to open the inputs. You should contact Taverna support.",e1);
		}
		
		if (options.hasInputs()) {
			String[] inputParams = options.getInputs();
			for (int i = 0; i < inputParams.length; i = i + 2) {
				String inputName = inputParams[i];
				try {
					
					URL inputURL = new URL(url, inputParams[i + 1]);
					
					Object inputValue=IOUtils.toString(inputURL.openStream());
					
					T2Reference entityId=context.getReferenceService().register(inputValue, 0, true, context);
					WorkflowDataToken token = new WorkflowDataToken("",new int[]{}, entityId, context);
					inputs.put(inputName, token);
					
				} catch (IndexOutOfBoundsException e) {
					throw new InvalidOptionException("Missing input filename for input "+ inputName);					
				} catch (IOException e) {
					throw new InvalidOptionException("Could not read input " + inputName + ": " + e.getMessage());				
				}
			}
		}
		
		if (options.getInputDocument()!=null) {
			String inputDocPath = options.getInputDocument();
			URL inputDocURL;
			try {
				inputDocURL = new URL(url, inputDocPath);
			} catch (MalformedURLException e1) {
				throw new ReadInputException("The a problem reading the input document from : "+inputDocPath+", "+e1.getMessage(),e1);
			}
			SAXBuilder builder = new SAXBuilder();
			Document inputDoc;
			try {
				inputDoc = builder.build(inputDocURL.openStream());
			} catch (IOException e) {
				throw new ReadInputException("There was an error reading the input document file: "+e.getMessage(),e);
			} catch (JDOMException e) {
				throw new ReadInputException("There was a problem processing the input document XML: "+e.getMessage(),e);
			}
			Map<String,DataThing> things = DataThingXMLFactory.parseDataDocument(inputDoc);
			for (String inputName : things.keySet()) {
				DataThing thing = things.get(inputName);
				T2Reference entityId=context.getReferenceService().register(thing.getDataObject(), 0, true, context);
				WorkflowDataToken token = new WorkflowDataToken("",new int[]{}, entityId, context);
				inputs.put(inputName, token);
			}
		}
		
		return inputs;
	}
}
