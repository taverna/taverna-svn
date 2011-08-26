/**
 * 
 */
package net.sf.taverna.t2.lineageService.util;

/**
 * @author paolo
 *
 */
public class Arc {

	String wfInstanceRef;
	String sourcePnameRef;
	String sourceVarNameRef;
	String sinkPnameRef;
	String sinkVarNameRef;
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
	 * @return the sourcePnameRef
	 */
	public String getSourcePnameRef() {
		return sourcePnameRef;
	}
	/**
	 * @param sourcePnameRef the sourcePnameRef to set
	 */
	public void setSourcePnameRef(String sourcePnameRef) {
		this.sourcePnameRef = sourcePnameRef;
	}
	/**
	 * @return the sourceVarNameRef
	 */
	public String getSourceVarNameRef() {
		return sourceVarNameRef;
	}
	/**
	 * @param sourceVarNameRef the sourceVarNameRef to set
	 */
	public void setSourceVarNameRef(String sourceVarNameRef) {
		this.sourceVarNameRef = sourceVarNameRef;
	}
	/**
	 * @return the sinkPnameRef
	 */
	public String getSinkPnameRef() {
		return sinkPnameRef;
	}
	/**
	 * @param sinkPnameRef the sinkPnameRef to set
	 */
	public void setSinkPnameRef(String sinkPnameRef) {
		this.sinkPnameRef = sinkPnameRef;
	}
	/**
	 * @return the sinkVarNameRef
	 */
	public String getSinkVarNameRef() {
		return sinkVarNameRef;
	}
	/**
	 * @param sinkVarNameRef the sinkVarNameRef to set
	 */
	public void setSinkVarNameRef(String sinkVarNameRef) {
		this.sinkVarNameRef = sinkVarNameRef;
	}
	
	
}
