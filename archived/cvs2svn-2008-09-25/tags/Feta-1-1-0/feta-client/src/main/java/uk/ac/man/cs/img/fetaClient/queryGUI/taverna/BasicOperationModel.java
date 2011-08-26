package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

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
		// setOperationResourceContent(xmlWrapper.getOperationResourceContent());
		// setOperationApplication(xmlWrapper.getOperationApplication());
	}

	public void setOperationName(String operName) {
		this.operationName = operName;

	}

	public void setOperationDescriptionText(String operDescText) {
		this.operationDescriptionText = operDescText;

	}

	public void setOperationMethod(String method) {
		if (method != null) {
			this.operationMethod = new FetaOntologyTermModel(method);
		}

	}

	public void setOperationTask(String task) {
		if (task != null) {
			this.operationTask = new FetaOntologyTermModel(task);
		}

	}

	public void setOperationResource(String resource) {
		if (resource != null) {
			this.operationResource = new FetaOntologyTermModel(resource);
		}

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
		return this.operationName;
	}

	public String getOperationDescriptionText() {
		return this.operationDescriptionText;
	}

	/*
	 * public FetaOntologyTermModel getOperationApplication() { return
	 * this.operationApplication; }
	 * 
	 */
	public FetaOntologyTermModel getOperationMethod() {
		return this.operationMethod;
	}

	public FetaOntologyTermModel getOperationTask() {
		return this.operationTask;
	}

	public FetaOntologyTermModel getOperationResource() {
		return this.operationResource;
	}

	/*
	 * public FetaOntologyTermModel getOperationResourceContent() { return
	 * this.operationResourceContent; }
	 */
	public void copyFrom(BasicOperationModel model) {
		this.setOperationName(model.getOperationName());
		this.setOperationDescriptionText(model.getOperationDescriptionText());
		if (model.getOperationMethod() != null) {
			this.setOperationMethod(model.getOperationMethod().getID());
		} else {
			this.operationMethod = null;
		}
		if (model.getOperationTask() != null) {
			this.setOperationTask(model.getOperationTask().getID());
		} else {
			this.operationTask = null;
		}
		if (model.getOperationResource() != null) {
			this.setOperationResource(model.getOperationResource().getID());
		} else {
			this.operationResource = null;
		}
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

}
