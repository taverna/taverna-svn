package net.sf.taverna.t2.activities.wsdl.query;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.partition.ActivityQueryFactory;

public class WSDLQueryFactory extends ActivityQueryFactory {

	@Override
	protected ActivityQuery createQuery(String property) {
		return new WSDLQuery(property);
	}

	@Override
	protected String getPropertyKey() {
		return "taverna.defaultwsdl";
	}

}
