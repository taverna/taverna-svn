package net.sf.taverna.t2.activities.biomart.query;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.partition.ActivityQueryFactory;
import net.sf.taverna.t2.partition.AddQueryActionHandler;

public class BiomartQueryFactory extends ActivityQueryFactory {

	@Override
	protected ActivityQuery createQuery(String property) {
		return new BiomartQuery(property);
	}

	@Override
	protected String getPropertyKey() {
		return "taverna.defaultmartregistry";
	}

	@Override
	public AddQueryActionHandler getAddQueryActionHandler() {
		return new BiomartAddQueryActionHandler();
	}

	@Override
	public boolean hasAddQueryActionHandler() {
		return true;
	}

	
}
