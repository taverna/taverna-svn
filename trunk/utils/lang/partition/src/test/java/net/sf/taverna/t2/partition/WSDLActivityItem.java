/**
 * 
 */
package net.sf.taverna.t2.partition;


public class WSDLActivityItem extends ActivityItem {
	private String operation;
	
	

	public WSDLActivityItem(String type, String name,String operation) {
		super(type, name);
		this.operation = operation;
	}

	protected String getOperation() {
		return operation;
	}

	protected void setOperation(String operation) {
		this.operation = operation;
	}

	@Override
	public String toString() {
		return operation;
	}
	
	
	
}