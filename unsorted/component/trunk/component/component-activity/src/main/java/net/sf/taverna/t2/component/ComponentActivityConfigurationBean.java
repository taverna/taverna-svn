package net.sf.taverna.t2.component;

import java.io.Serializable;

/**
 * Component activity configuration bean.
 * 
 */
public class ComponentActivityConfigurationBean implements Serializable {

	public void setDataflowString(String dataflowString) {
		this.dataflowString = dataflowString;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5236221702931112807L;
	private String dataflowString;

	public String getDataflowString() {
		return dataflowString;
	}

}
