package net.sf.taverna.t2.partition;

public class SoaplabActivityItem extends ActivityItem {

	public String category;
	private final String operation;
	
	public SoaplabActivityItem(String type, String name, String category, String operation) {
		super(type, name);
		this.category=category;
		this.operation = operation;
	}

	protected String getCategory() {
		return category;
	}

	protected void setCategory(String category) {
		this.category = category;
	}

	protected String getOperation() {
		return operation;
	}

	@Override
	public String toString() {
		return this.operation;
	}
	
	

}
