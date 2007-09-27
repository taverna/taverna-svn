package net.sf.taverna.t2.workflowmodel.processor.activity.config;

import java.util.List;

/**
 * A generic bean that describes the shared properties of input and output ports.
 * 
 * @author Stuart Owen
 *
 */
public abstract class ActivityPortDefinitionBean {
	private String name;
	private int depth;
	private List<String> mimeTypes;
	
	/**
	 * @return the port name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the port name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the depth of the port
	 */
	public int getDepth() {
		return depth;
	}
	
	/**
	 * @param depth the depth of the port
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	/**
	 * @return a list a MIME types that describe the port
	 */
	public List<String> getMimeTypes() {
		return mimeTypes;
	}
	
	/**
	 * @param mimeTypes the list of MIME-types that describe the port
	 */
	public void setMimeTypes(List<String> mimeTypes) {
		this.mimeTypes = mimeTypes;
	}
}
