/**
 * 
 */
package net.sf.taverna.t2.lineageService.util;

/**
 * @author paolo
 *
 */
public class ProcBinding {

	String pNameRef;
	String execIDRef;
	String actName;
	String iterationVector;
	
	
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("**** ProcBinding: \n").
			append("execIDRef = "+execIDRef+"\n").
			append("PNameRef = "+pNameRef+"\n").
			append("actName = "+actName+"\n").
			append("iteration = "+iterationVector+"\n");

		return sb.toString();
	}
	
	/**
	 * @return the pNameRef
	 */
	public String getPNameRef() {
		return pNameRef;
	}
	/**
	 * @param nameRef the pNameRef to set
	 */
	public void setPNameRef(String nameRef) {
		pNameRef = nameRef;
	}
	/**
	 * @return the execIDRef
	 */
	public String getExecIDRef() {
		return execIDRef;
	}
	/**
	 * @param execIDRef the execIDRef to set
	 */
	public void setExecIDRef(String execIDRef) {
		this.execIDRef = execIDRef;
	}
	/**
	 * @return the actName
	 */
	public String getActName() {
		return actName;
	}
	/**
	 * @param actName the actName to set
	 */
	public void setActName(String actName) {
		this.actName = actName;
	}
	/**
	 * @return the iteration
	 */
	public String getIterationVector() {
		return iterationVector;
	}
	/**
	 * @param iterationVector the iteration to set
	 */
	public void setIterationVector(String iterationVector) {
		this.iterationVector = iterationVector;
	}
	
	
}
