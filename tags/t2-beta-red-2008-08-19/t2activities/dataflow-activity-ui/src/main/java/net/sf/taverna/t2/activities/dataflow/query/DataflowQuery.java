package net.sf.taverna.t2.activities.dataflow.query;

import net.sf.taverna.t2.partition.ActivityQuery;

public class DataflowQuery extends ActivityQuery {

	public DataflowQuery(String property) {
		super(property);
	}

	@Override
	public void doQuery() {
		add(new DataflowActivityItem());
	}

}
