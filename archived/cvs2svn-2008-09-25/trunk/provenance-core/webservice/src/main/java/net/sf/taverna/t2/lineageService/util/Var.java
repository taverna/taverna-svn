package net.sf.taverna.t2.lineageService.util;

/**
 * a Var that has no pName is either a WF input or output, depending on isInput
 * @author paolo
 */
public class Var {
	String vName, pName;
	boolean isInput;
	String wfInstanceRef;
	String type;
	int typeNestingLevel = 0;
	int actualNestingLevel = 0;
	boolean isANLset = false;  // set to true when the ANL has been set 
	
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
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the typeNestingLevel
	 */
	public int getTypeNestingLevel() {
		return typeNestingLevel;
	}
	/**
	 * @param typeNestingLevel the typeNestingLevel to set
	 */
	public void setTypeNestingLevel(int typeNestingLevel) {
		this.typeNestingLevel = typeNestingLevel;
	}
	/**
	 * @return the actualNestingLevel
	 */
	public int getActualNestingLevel() {
		return actualNestingLevel;
	}
	/**
	 * @param actualNestingLevel the actualNestingLevel to set
	 */
	public void setActualNestingLevel(int actualNestingLevel) {
		this.actualNestingLevel = actualNestingLevel;
	}
	/**
	 * @return the isANLset
	 */
	public boolean isANLset() {
		return isANLset;
	}
	/**
	 * @param isANLset the isANLset to set
	 */
	public void setANLset(boolean isANLset) {
		this.isANLset = isANLset;
	}


}
