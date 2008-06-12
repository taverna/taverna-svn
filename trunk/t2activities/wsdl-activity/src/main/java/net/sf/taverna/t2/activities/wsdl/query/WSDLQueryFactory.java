package net.sf.taverna.t2.activities.wsdl.query;

import net.sf.taverna.t2.partition.ActivityQueryFactory;
import net.sf.taverna.t2.partition.Query;

public class WSDLQueryFactory extends ActivityQueryFactory {

	@Override
	protected Query<?> createQuery(String property) {
		return new WSDLQuery(property);
	}

	@Override
	protected String getPropertyKey() {
		return "taverna.defaultwsdl";
	}

}
