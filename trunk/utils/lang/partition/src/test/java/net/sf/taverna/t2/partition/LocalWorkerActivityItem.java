package net.sf.taverna.t2.partition;

public class LocalWorkerActivityItem extends ActivityItem {


	private final String category;

	public LocalWorkerActivityItem(String type, String name, String category) {
		super(type, name);
		this.category = category;
	}

	protected String getCategory() {
		return category;
	}

}
