package net.sf.taverna.t2.activities.stringconstant.query;

import net.sf.taverna.t2.partition.ActivityQuery;

public class StringConstantQuery extends ActivityQuery {

	public StringConstantQuery(String property) {
		super(property);
	}

	@Override
	public void doQuery() {
		add(new StringConstantActivityItem());

	}

}
