package net.sf.taverna.t2.activities.stringconstant.query;

import net.sf.taverna.t2.partition.ActivityItem;

public class StringConstantActivityItem implements ActivityItem {

	public String getType() {
		return "String Constant";
	}

	@Override
	public String toString() {
		return getType();
	}
	
	
}
