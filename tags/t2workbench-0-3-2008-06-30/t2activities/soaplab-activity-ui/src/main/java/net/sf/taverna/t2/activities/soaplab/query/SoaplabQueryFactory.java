package net.sf.taverna.t2.activities.soaplab.query;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.partition.ActivityQueryFactory;
import net.sf.taverna.t2.partition.AddQueryActionHandler;

public class SoaplabQueryFactory extends ActivityQueryFactory {

	@Override
	public AddQueryActionHandler getAddQueryActionHandler() {
		return new SoaplabAddQueryActionHandler();
	}

	@Override
	public boolean hasAddQueryActionHandler() {
		return true;
	}

	@Override
	protected ActivityQuery createQuery(String property) {
		return new SoaplabQuery(property);
	}

	@Override
	protected String getPropertyKey() {
		return "taverna.defaultsoaplab";
	}

}
