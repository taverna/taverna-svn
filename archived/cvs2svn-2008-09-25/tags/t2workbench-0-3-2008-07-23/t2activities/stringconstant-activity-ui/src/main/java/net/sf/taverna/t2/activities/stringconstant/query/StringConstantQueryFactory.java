package net.sf.taverna.t2.activities.stringconstant.query;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.partition.ActivityQueryFactory;

public class StringConstantQueryFactory extends ActivityQueryFactory {

	@Override
	protected ActivityQuery createQuery(String property) {
		return new StringConstantQuery(property);
	}

	@Override
	protected String getPropertyKey() {
		return null;
	}


}
