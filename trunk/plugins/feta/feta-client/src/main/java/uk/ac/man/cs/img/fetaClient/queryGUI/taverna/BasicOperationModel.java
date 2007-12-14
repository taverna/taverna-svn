package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.util.Map;

/*
 * BasicOperationModel.java
 *
 * Created on 31 March 2005, 23:22
 */

/**
 * 
 * @author alperp
 */

// import java.util.List;
// import java.util.ArrayList;
public class BasicOperationModel {

	private String operationName;

	private String operationDescriptionText;

	private FetaOntologyTermModel operationTask;

	private FetaOntologyTermModel operationMethod;

	// private FetaOntologyTermModel operationApplication;
	private FetaOntologyTermModel operationResource;

	private Map<String, String> inputParameters;

	private Map<String, String> outputParameters;
	
	// private FetaOntologyTermModel operationResourceContent;
	// private ArrayList inputParameters;
	// private ArrayList outputParameters;

	/** Creates a new instance of BasicOperationModel */
	public BasicOperationModel() {
		super();
		// inputParameters = new ArrayList();
		// outputParameters = new ArrayList();
	}

	public BasicOperationModel(IServiceModelFiller xmlWrapper) {
		super();
		// inputParameters = new ArrayList();
		// outputParameters = new ArrayList();
		setOperationName(xmlWrapper.getOperationName());
		setOperationDescriptionText(xmlWrapper.getOperationDescriptionText());

		setOperationMethod(xmlWrapper.getOperationMethod());
		setOperationTask(xmlWrapper.getOperationTask());
		setOperationResource(xmlWrapper.getOperationResource());
		setInputParameters(xmlWrapper.getInputParameters());
		setOutputParameters(xmlWrapper.getOutputParameters());
	}

	public void setOperationName(String operName) {
		operationName = operName;

	}

	public void setOperationDescriptionText(String operDescText) {
		operationDescriptionText = operDescText;

	}

	public void setOperationMethod(String method) {
		if (method != null) {
			operationMethod = new FetaOntologyTermModel(method);
		}

	}

	public void setOperationTask(String task) {
		if (task != null) {
			operationTask = new FetaOntologyTermModel(task);
		}
	}

	public void setOperationResource(String resource) {
		if (resource != null) {
			operationResource = new FetaOntologyTermModel(resource);
		}
	}

	public void setInputParameters(Map<String, String> inputParameters) {
		this.inputParameters = inputParameters;
	}
	
	public void setOutputParameters(Map<String, String> outputParameters) {
		this.outputParameters = outputParameters;
	}
	

	/*
	 * public void setOperationResourceContent(String resourceContent){ if
	 * (resourceContent != null) { this.operationResourceContent = new
	 * FetaOntologyTermModel(resourceContent); }
	 *  } public void setOperationApplication(String application){ if
	 * (application != null) { this.operationApplication = new
	 * FetaOntologyTermModel(application); }
	 *  }
	 */
	public String getOperationName() {
		return operationName;
	}

	public String getOperationDescriptionText() {
		return operationDescriptionText;
	}

	/*
	 * public FetaOntologyTermModel getOperationApplication() { return
	 * this.operationApplication; }
	 * 
	 */
	public FetaOntologyTermModel getOperationMethod() {
		return operationMethod;
	}

	public FetaOntologyTermModel getOperationTask() {
		return operationTask;
	}

	public FetaOntologyTermModel getOperationResource() {
		return operationResource;
	}
	

	/*
	 * public FetaOntologyTermModel getOperationResourceContent() { return
	 * this.operationResourceContent; }
	 */
	public void copyFrom(BasicOperationModel model) {
		setOperationName(model.getOperationName());
		setOperationDescriptionText(model.getOperationDescriptionText());
		if (model.getOperationMethod() != null) {
			setOperationMethod(model.getOperationMethod().getID());
		} else {
			operationMethod = null;
		}
		if (model.getOperationTask() != null) {
			setOperationTask(model.getOperationTask().getID());
		} else {
			operationTask = null;
		}
		if (model.getOperationResource() != null) {
			setOperationResource(model.getOperationResource().getID());
		} else {
			operationResource = null;
		}
		setInputParameters(model.getInputParameters());
		setOutputParameters(model.getOutputParameters());
		/*
		 * if ( model.getOperationResourceContent() != null ) {
		 * this.setOperationResource(model.getOperationResourceContent().getID()); }
		 * else { this.operationResourceContent = null; } if (
		 * model.getOperationApplication() != null ) {
		 * this.setOperationApplication(model.getOperationApplication().getID()); }
		 * else{ this.operationApplication = null; }
		 */
		// parameters.copyFrom(model.getOperationModel());
	}

	public Map<String, String> getInputParameters() {
		return inputParameters;
	}

	public Map<String, String> getOutputParameters() {
		return outputParameters;
	}



}
