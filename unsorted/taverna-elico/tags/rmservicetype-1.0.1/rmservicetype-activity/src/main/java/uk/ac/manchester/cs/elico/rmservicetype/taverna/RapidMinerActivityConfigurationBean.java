package uk.ac.manchester.cs.elico.rmservicetype.taverna;

import java.io.Serializable;
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

/**
 * Example activity configuration bean.
 * 
 */
public class RapidMinerActivityConfigurationBean implements Serializable {

	/*
	 * TODO: Remove this comment.
	 * 
	 * The configuration specifies the variable options and configurations for
	 * an activity that has been added to a workflow. For instance for a WSDL
	 * activity, the configuration contains the URL for the WSDL together with
	 * the method name. String constant configurations contain the string that
	 * is to be returned, while Beanshell script configurations contain both the
	 * scripts and the input/output ports (by subclassing
	 * ActivityPortsDefinitionBean).
	 * 
	 * Configuration beans are serialised as XML (currently by using XMLBeans)
	 * when Taverna is saving the workflow definitions. Therefore the
	 * configuration beans need to follow the JavaBeans style and only have
	 * fields of 'simple' types such as Strings, integers, etc. Other beans can
	 * be referenced as well, as long as they are part of the same plugin.
	 */
	
	// TODO: Remove the example fields and getters/setters and add your own	

	private String operatorName;
	private String callName;

//    private String inputLocation = new String();
//    private String outputLocation = new String();

    private LinkedHashMap<String, IOInputPort> inputPorts = new LinkedHashMap<String, IOInputPort>();
    private LinkedHashMap<String, IOOutputPort> outputPorts = new LinkedHashMap<String, IOOutputPort>();

    public LinkedHashMap<String, IOInputPort> getInputPorts() {
        return inputPorts;
    }

    public void setInputPorts(LinkedHashMap<String, IOInputPort> inputPorts) {
        this.inputPorts = inputPorts;
    }

    public LinkedHashMap<String, IOOutputPort> getOutputPorts() {
        return outputPorts;
    }

    public void setOutputPorts(LinkedHashMap<String, IOOutputPort> outputPorts) {
        this.outputPorts = outputPorts;
    }


	private List<RapidMinerParameterDescription> parameterDescriptions;
	private HashMap<String, String> invocationParameters = new HashMap<String, String>();
	private boolean isExplicit;
	private boolean isParametersConfigured = false;
	private boolean hasDescriptions = false;




	public void setIsParametersConfigured(boolean val) {
		isParametersConfigured = val;
	}
	
	public boolean getIsParametersConfigured() {
		return isParametersConfigured;
	}
	
//	public void setIsExplicit(boolean val) {
//		isExplicit = val;
//	}
//
//	public boolean getIsExplicit() {
//		return isExplicit;
//	}
	
	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String exampleString) {
		this.operatorName = exampleString;
	}
	
	public void setCallName(String name) {
		callName = name;
	}
	
	public void setInvocationParameters(HashMap<String, String> params) {
		invocationParameters = params;
	}
	
	public HashMap<String, String> getInvocationParameters() {
		return invocationParameters;
	}
	
	public String getCallName() {
		return callName;
	}
	
	public void setParameterDescriptions(List<RapidMinerParameterDescription> desc) {
		parameterDescriptions = desc;
	}
	 
	public List<RapidMinerParameterDescription> getParameterDescriptions() {
		return parameterDescriptions;
	}
	
	public void setHasDescriptions(boolean val) {
		hasDescriptions = val;
	}
	
	public boolean getHasDescriptions() {
		return hasDescriptions;
	}


}
