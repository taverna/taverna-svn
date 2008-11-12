package net.sf.taverna.t2.provenance.lineageservice;

public class LineageQueryResultRecord {

	String pname;
	String vname;
	String wfInstance;
	String iteration;
	String value;     // atomic or XML-formatted collection
	String type;  // one of referenceSet, referenceSetCollection
	/**
	 * @return the pname
	 */
	public String getPname() {
		return pname;
	}
	/**
	 * @param pname the pname to set
	 */
	public void setPname(String pname) {
		this.pname = pname;
	}
	/**
	 * @return the vname
	 */
	public String getVname() {
		return vname;
	}
	/**
	 * @param vname the vname to set
	 */
	public void setVname(String vname) {
		this.vname = vname;
	}
	/**
	 * @return the wfInstance
	 */
	public String getWfInstance() {
		return wfInstance;
	}
	/**
	 * @param wfInstance the wfInstance to set
	 */
	public void setWfInstance(String wfInstance) {
		this.wfInstance = wfInstance;
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
	 * @return the iteration
	 */
	public String getIteration() {
		return iteration;
	}
	/**
	 * @param iteration the iteration to set
	 */
	public void setIteration(String iteration) {
		this.iteration = iteration;
	}

}