package net.sf.taverna.t2.activities.wsdl.query;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.partition.ActivityQueryFactory;
import net.sf.taverna.t2.partition.AddQueryActionHandler;

public class WSDLQueryFactory extends ActivityQueryFactory {

	@Override
	protected ActivityQuery createQuery(String property) {
		return new WSDLQuery(property);
	}

	@Override
	protected String getPropertyKey() {
		return "taverna.defaultwsdl";
	}

	@Override
	public AddQueryActionHandler getAddQueryActionHandler() {
		return new WSDLAddQueryActionHandler();
	}

	@Override
	public boolean hasAddQueryActionHandler() {
		return true;
	}
	
	

}
