package net.sf.taverna.t2.partition;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workbench.configuration.Configurable;

public abstract class ActivityQueryFactory implements QueryFactory {
	
	public List<Query<?>> getQueries() {
		List<String> properties = (List<String>)config.getPropertyMap().get(getPropertyKey());
		List<Query<?>> result = new ArrayList<Query<?>>();
		if (properties!=null) {
			for (String property : properties) {
				result.add(createQuery(property));
			}
		}
		return result;
	}

	protected Configurable config;

	protected abstract String getPropertyKey();
	protected abstract Query<?> createQuery(String property);
 
	public void setConfigurable(Configurable config) {
		this.config=config;
	}
	
	Configurable getConfigurable() {
		return this.config;
	}

}
