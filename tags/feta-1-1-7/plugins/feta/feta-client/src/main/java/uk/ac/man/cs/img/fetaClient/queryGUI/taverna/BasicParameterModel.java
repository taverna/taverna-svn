package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

/*
 * BasicParameterModel.java
 *
 * Created on 01 April 2005, 00:18
 */

/**
 * 
 * @author alperp
 */
public class BasicParameterModel {

	private FetaOntologyTermModel semanticType;

	private String parameterName;

	private String parameterDescriptionText;

	/** Creates a new instance of BasicParameterModel */
	public BasicParameterModel(PedroXMLWrapper xmlWrapper) {
		/*
		 * setOperationName(xmlWrapper.getOperationName());
		 * setOperationDescriptionText(xmlWrapper.getOperationDescriptionText());
		 */

	}

	public void setParameterName(String paramName) {
		this.parameterName = paramName;
	}

	public void setParameterDescription(String paramDescText) {
		this.parameterDescriptionText = paramDescText;
	}

	public void setSemanticType(FetaOntologyTermModel semType) {
		this.semanticType = semType;
	}

	public String setParameterName() {
		return this.parameterName;
	}

	public String setParameterDescription() {
		return this.parameterDescriptionText;
	}

	public FetaOntologyTermModel setSemanticType() {
		return this.semanticType;
	}

}
