package net.sf.taverna.t2.activities.rshell.query;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.partition.ActivityQueryFactory;

public class RshellQueryFactory extends ActivityQueryFactory {

	@Override
	protected ActivityQuery createQuery(String property) {
		return new RshellQuery(property);
	}

	@Override
	protected String getPropertyKey() {
		// TODO Auto-generated method stub
		return null;
	}

}
