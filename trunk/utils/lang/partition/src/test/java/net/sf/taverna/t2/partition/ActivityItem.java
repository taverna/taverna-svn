/**
 * 
 */
package net.sf.taverna.t2.partition;

public class ActivityItem {
	public String type;
	public String name;
	
	public ActivityItem(String type, String name) {
		super();
		this.type = type;
		this.name = name;
	}
	
	protected String getType() {
		return type;
	}
	
	protected void setType(String type) {
		this.type = type;
	}
	
	protected String getName() {
		return name;
	}
	protected void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return name;
	}
	
}