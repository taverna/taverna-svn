package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.util.Map;

import uk.ac.man.cs.img.fetaEngine.commons.ServiceType;

public interface IServiceModelFiller {

	/* Operation Related */
	public String getOperationName();

	public String getOperationDescriptionText();

	/* Operation Annotation Related */
	public String getOperationMethod();

	public String getOperationTask();

	// public String getOperationApplication();
	public String getOperationResource();

	// public String getOperationResourceContent();
	public String getOperationSpec();

	/* Service Related */
	public String getServiceName();

	public String getDescriptionLocation();

	public String getServiceDescriptionText();

	public ServiceType getServiceType();

	public String getServiceInterfaceLocation();

	public String getLocationURL();

	public String getOrganisationName();
	
	public Map<String, String> getInputParameters();
	
	public Map<String, String> getOutputParameters();

}
