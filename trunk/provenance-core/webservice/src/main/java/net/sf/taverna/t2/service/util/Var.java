package net.sf.taverna.t2.service.util;

/**
 * a Var that has no pName is either a WF input or output, depending on isInput
 * @author paolo
 */
public class Var {
	String vName, pName;
	boolean isInput;
	/**
	 * @return the vName
	 */
	public String getVName() {
		return vName;
	}
	/**
	 * @param name the vName to set
	 */
	public void setVName(String name) {
		vName = name;
	}
	/**
	 * @return the pName
	 */
	public String getPName() {
		return pName;
	}
	/**
	 * @param name the pName to set
	 */
	public void setPName(String name) {
		pName = name;
	}
	/**
	 * @return the isInput
	 */
	public boolean isInput() {
		return isInput;
	}
	/**
	 * @param isInput the isInput to set
	 */
	public void setInput(boolean isInput) {
		this.isInput = isInput;
	}


}
