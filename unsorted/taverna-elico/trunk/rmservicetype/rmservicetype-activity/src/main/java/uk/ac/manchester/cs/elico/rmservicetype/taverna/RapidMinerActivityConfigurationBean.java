package uk.ac.manchester.cs.elico.rmservicetype.taverna;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

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
	private String inputLocation, outputLocation;
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
	
	
	public void setInputLocation(String input) {
		inputLocation = input;
	}
	
	public String getInputLocation() {
		return inputLocation;
	}
	
	public void setOutputLocation(String output) {
		outputLocation = output;
	}
	
	public String getOutputLocation() {
		return outputLocation;
	}
	
	public void setIsExplicit(boolean val) {
		isExplicit = val;
	}
	
	public boolean getIsExplicit() {
		return isExplicit;
	}
	
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
