package net.sf.taverna.t2.partition;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.ui.activitypalette.ActivityPaletteConfiguration;

public class QueryFactoryRegistry extends SPIRegistry<QueryFactory> {
	
	private static QueryFactoryRegistry instance = new QueryFactoryRegistry();

	public static QueryFactoryRegistry getInstance() {
		return instance;
	}
	
	private QueryFactoryRegistry() {
		super(QueryFactory.class);
	}
	
	@Override
	public List<QueryFactory> getInstances() {
		List<QueryFactory> instances = super.getInstances();
		for (QueryFactory fac : instances) {
			if (fac instanceof ActivityQueryFactory) {
				((ActivityQueryFactory)fac).setConfigurable(getConfigurable());
			}
		}
		return instances;
	}

	public List<Query<?>> getQueries() {
		List<QueryFactory> instances = getInstances();
		List<Query<?>> result = new ArrayList<Query<?>>();
		for (QueryFactory fac : instances) {
			result.addAll(fac.getQueries());
		}
		return result;
	}
	
	protected Configurable getConfigurable() {
		return ActivityPaletteConfiguration.getInstance();
	}
}
