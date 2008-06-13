package net.sf.taverna.t2.activities.soaplab.query;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.partition.ActivityQueryFactory;

public class SoaplabQueryFactory extends ActivityQueryFactory {

	@Override
	protected ActivityQuery createQuery(String property) {
		return new SoaplabQuery(property);
	}

	@Override
	protected String getPropertyKey() {
		return "taverna.defaultsoaplab";
	}

}
