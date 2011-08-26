/**
 * 
 */
package net.sf.taverna.t2.lineageService.util;

/**
 * @author paolo
 *
 */
public class VarBinding {

	String varNameRef;
	String wfInstanceRef;
	String value;
	String collIDRef;
	int positionInColl;
	String PNameRef;
	String valueType;
	String ref;
	String    iterationVector;
	
	
	public String toString() {
	
		StringBuffer sb = new StringBuffer();
		
		sb.append("**** VarBinding: \n").
			append("wfInstanceRef = "+wfInstanceRef+"\n").
			append("PNameRef = "+PNameRef+"\n").
			append("varNameRef = "+varNameRef+"\n").
			append("iteration = "+iterationVector+"\n").
			append("collIdef  " +collIDRef+"\n").
			append("positionInColl = "+positionInColl+"\n").
			append("value = "+value+"\n").
			append("ref = "+ref+"\n");

		return sb.toString();
	}
	
	
	/**
	 * @return the positionInColl
	 */
	public int getPositionInColl() {
		return positionInColl;
	}
	/**
	 * @param positionInColl the positionInColl to set
	 */
	public void setPositionInColl(int positionInColl) {
		this.positionInColl = positionInColl;
	}
	/**
	 * @return the valueType
	 */
	public String getValueType() {
		return valueType;
	}
	/**
	 * @param valueType the valueType to set
	 */
	public void setValueType(String valueType) {
		this.valueType = valueType;
	}
	/**
	 * @return the varNameRef
	 */
	public String getVarNameRef() {
		return varNameRef;
	}
	/**
	 * @param varNameRef the varNameRef to set
	 */
	public void setVarNameRef(String varNameRef) {
		this.varNameRef = varNameRef;
	}
	/**
	 * @return the wfInstanceRef
	 */
	public String getWfInstanceRef() {
		return wfInstanceRef;
	}
	/**
	 * @param wfInstanceRef the wfInstanceRef to set
	 */
	public void setWfInstanceRef(String wfInstanceRef) {
		this.wfInstanceRef = wfInstanceRef;
	}
	/**
	 * @return the pNameRef
	 */
	public String getPNameRef() {
		return PNameRef;
	}
	/**
	 * @param nameRef the pNameRef to set
	 */
	public void setPNameRef(String nameRef) {
		PNameRef = nameRef;
	}
	/**
	 * @return the collIDRef
	 */
	public String getCollIDRef() {
		return collIDRef;
	}
	/**
	 * @param collIDRef the collIDRef to set
	 */
	public void setCollIDRef(String collIDRef) {
		this.collIDRef = collIDRef;
	}
	/**
	 * @return the iteration
	 */
	public String getIteration() {
		return iterationVector;
	}
	/**
	 * @param iterationVector the iteration to set
	 */
	public void setIterationVector(String iterationVector) {
		this.iterationVector = iterationVector;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the ref
	 */
	public String getRef() {
		return ref;
	}
	/**
	 * @param ref the ref to set
	 */
	public void setRef(String ref) {
		this.ref = ref;
	}
	
	
}
