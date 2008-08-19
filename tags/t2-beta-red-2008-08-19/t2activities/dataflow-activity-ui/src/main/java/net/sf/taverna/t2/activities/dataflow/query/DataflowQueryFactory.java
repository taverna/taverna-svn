package net.sf.taverna.t2.activities.dataflow.query;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.partition.ActivityQueryFactory;

public class DataflowQueryFactory extends ActivityQueryFactory {

	@Override
	protected ActivityQuery createQuery(String property) {
		return new DataflowQuery(property);
	}

	@Override
	protected String getPropertyKey() {
		return null;
	}

}
