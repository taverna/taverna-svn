package net.sf.taverna.t2.activities.beanshell.query;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.partition.ActivityQueryFactory;

public class BeanshellQueryFactory extends ActivityQueryFactory {

	@Override
	protected ActivityQuery createQuery(String property) {
		return new BeanshellQuery(property);
	}

	@Override
	protected String getPropertyKey() {
		return null;
	}

}
