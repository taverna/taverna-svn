/*******************************************************************************
 * Copyright (C) 2011 The University of Manchester
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
package net.sf.taverna.t2.activities.apiconsumer;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import uk.org.taverna.configuration.app.ApplicationConfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An {@link ActivityFactory} for creating <code>ApiConsumerActivity</code>.
 *
 * @author David Withers
 */
public class ApiConsumerActivityFactory implements ActivityFactory {

	private ApplicationConfiguration applicationConfiguration;
	private Edits edits;

	@Override
	public ApiConsumerActivity createActivity() {
		return new ApiConsumerActivity(applicationConfiguration);
	}

	@Override
	public URI getActivityType() {
		return URI.create(ApiConsumerActivity.URI);
	}

	@Override
	public JsonNode getActivityConfigurationSchema() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
 			return objectMapper.readTree(getClass().getResource("/schema.json"));
		} catch (IOException e) {
			return objectMapper.createObjectNode();
		}
	}

	@Override
	public Set<ActivityInputPort> getInputPorts(JsonNode json) {
		Set<ActivityInputPort> inputPorts = new HashSet<>();
		// All non-static methods need the object to invoke the method on and it is
		// passed through the input port called 'object'. Non-static methods and constructors
		// also return the same object through the output port called 'object'
		if (!json.get("isMethodStatic").asBoolean() && !json.get("isMethodConstructor").asBoolean()) {
			inputPorts.add(edits.createActivityInputPort("object", 0, false, null, null));
		}

		// Add input ports for method's parameters
		JsonNode parameterNames = json.get("parameterNames");
		JsonNode parameterTypes = json.get("parameterTypes");
		JsonNode parameterDimensions = json.get("parameterDimensions");
		for (int i = 0; i < parameterNames.size(); i++) {
			// Create input ports...
			if (ApiConsumerActivity.canRegisterAsString(parameterTypes.get(i).textValue())) {
				if(parameterTypes.get(i).textValue().equals("char")) {
					// char, char[] are treated as a string (where char is a string with only one character),
					// so the port depth for char[] is 0 rather than 1 as is expected for an array
					if(parameterDimensions.get(i).intValue() == 0 ||
							parameterDimensions.get(i).intValue() == 1){
						inputPorts.add(edits.createActivityInputPort(parameterNames.get(i).textValue(), 0, true, null, String.class));
					}// char[][], etc.
					else {
						inputPorts.add(edits.createActivityInputPort(parameterNames.get(i).textValue(), parameterDimensions.get(i).intValue()-1, true, null, String.class));
					}
				}
				else{
					inputPorts.add(edits.createActivityInputPort(parameterNames.get(i).textValue(), parameterDimensions.get(i).intValue(), true, null, String.class));
				}
			}
			else if(parameterTypes.get(i).textValue().equals("byte")) {
				// byte, byte[] are treated as a stream of depth 0 rather that array of bytes of depth 1.
				// byte is treated as byte[] with only one element.
				// Note that the port depth is set to 0 rather than 1 as is expected for an array
				if(parameterDimensions.get(i).intValue() == 0 ||
						parameterDimensions.get(i).intValue() == 1){
					inputPorts.add(edits.createActivityInputPort(parameterNames.get(i).textValue(), 0, true, null, byte[].class));
				}
				// byte[][], etc.
				else {
					inputPorts.add(edits.createActivityInputPort(parameterNames.get(i).textValue(), parameterDimensions.get(i).intValue()-1, true, null, byte[].class));
				}
			}
			else{  //POJO
				inputPorts.add(edits.createActivityInputPort(parameterNames.get(i).textValue(), parameterDimensions.get(i).intValue(), false, null, null));
			}
		}

		return inputPorts;
	}

	@Override
	public Set<ActivityOutputPort> getOutputPorts(JsonNode json) {
		Set<ActivityOutputPort> outputPorts = new HashSet<>();
		// All non-static methods need the object to invoke the method on and it is
		// passed through the input port called 'object'. Non-static methods and constructors
		// also return the same object through the output port called 'object'
		if (!json.get("isMethodStatic").asBoolean()) {
			outputPorts.add(edits.createActivityOutputPort("object", 0, 0));
		}

		// Add output port 'result' for the return value of non-void methods
		String returnType = json.get("returnType").textValue();
		int returnDimension = json.get("returnDimension").intValue();
		if (!returnType.equals("void") && !json.get("isMethodConstructor").asBoolean()) {
			// byte[] return type maps to port dimension 0! It is treated as a stream rather than a list of bytes.
			// Similar for char and char[] - not implemented yet
			if (returnType.equals("byte") && returnDimension==1){
				outputPorts.add(edits.createActivityOutputPort("result", 0, 0));
			}
			else
				outputPorts.add(edits.createActivityOutputPort("result", returnDimension, returnDimension));
		}
		return outputPorts;
	}

	public void setEdits(Edits edits) {
		this.edits = edits;
	}

	public void setApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

}
