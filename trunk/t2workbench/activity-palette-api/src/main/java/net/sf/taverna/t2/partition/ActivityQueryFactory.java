package net.sf.taverna.t2.partition;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workbench.configuration.Configurable;

/**
 * A query factory specialised for use with Activities. 
 * <p>
 * The getPropertyKey and createQuery need to be implemented to make this class concrete.
 * <br>
 * When discovered as an SPI, factories of this type will have the Configurable set on it, and this configurable
 * will provide a list of property values to relate to the propertyKey.
 * <br>
 * For each property defined in the configuration a Query will be requested to be constructed with a call to createQuery.
 * </p>
 * 
 * @author Stuart Owen
 * @see QueryFactory
 * @see QueryFactoryRegistry
 * @see Query
 * @see ActivityQuery
 *
 */
public abstract class ActivityQueryFactory implements QueryFactory {
	
	public List<Query<?>> getQueries() {
		List<Query<?>> result = new ArrayList<Query<?>>();
		if (getPropertyKey()==null) { //no property required
			result.add(createQuery(null));
		}
		else {
			List<String> properties = (List<String>)config.getPropertyMap().get(getPropertyKey());
			
			if (properties!=null) {
				for (String property : properties) {
					result.add(createQuery(property));
				}
			}
		}
		return result;
	}

	protected Configurable config;

	/**
	 * The implementation of this method will return the key for the property values held in the configuration object passed to setConfigurable.
	 * <br>
	 * If the implementation doesnot require a property to do a query, then this method should return null
	 * @return
	 * @see Configurable 
	 */
	protected abstract String getPropertyKey();
	
	/**
	 * The implementation of this method should construct an ActivityQuery using the property provided.
	 * 
	 * @param property
	 * @return
	 * @see ActivityQuery
	 */
	protected abstract ActivityQuery createQuery(String property);
 
	/**
	 * @param config - the Configurable object that holds the values for the propertyKey
	 */
	public void setConfigurable(Configurable config) {
		this.config=config;
	}
	
	Configurable getConfigurable() {
		return this.config;
	}

}
