/**
 * 
 */
package net.sf.taverna.t2.drizzle.query;

import java.util.Date;

import net.sf.taverna.t2.drizzle.bean.ActivitySetModelBean;
import net.sf.taverna.t2.drizzle.decoder.PropertyDecoder;
import net.sf.taverna.t2.drizzle.decoder.PropertyDecoderRegistry;
import net.sf.taverna.t2.drizzle.model.ActivityQueryRunIdentification;
import net.sf.taverna.t2.drizzle.model.ActivitySubsetIdentification;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.util.ObjectMembershipFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

/**
 * @author alanrw
 *
 */
public final class ActivitySavedConfigurationQuery implements ActivityQuery <ActivitySetModelBean>{
	
	ActivitySetModelBean bean;
	
	private ActivitySubsetIdentification lastRun = null;
	
	/**
	 * @param configuration
	 */
	public ActivitySavedConfigurationQuery(ActivitySetModelBean configuration) {
		if (configuration == null) {
			throw new NullPointerException("configuration cannot be null"); //$NON-NLS-1$
		}
		configure(configuration);
	}

	/**
	 * @see net.sf.taverna.t2.drizzle.query.ActivityQuery#configure(java.lang.Object)
	 */
	public void configure(ActivitySetModelBean configuration) {
		if (configuration == null) {
			throw new NullPointerException ("configuration cannot be null"); //$NON-NLS-1$
		}
		this.bean = configuration;
	}

	/**
	 * @see net.sf.taverna.t2.drizzle.query.ActivityQuery#runQuery(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet)
	 */
	@SuppressWarnings("unchecked")
	public ActivitySubsetIdentification runQuery(PropertiedObjectSet<ProcessorFactoryAdapter> targetSet) {
		if (targetSet == null) {
			throw new NullPointerException("targetSet cannot be null"); //$NON-NLS-1$
		}
	    PropertyDecoder<ActivitySetModelBean, ProcessorFactoryAdapter> decoder =
	    	PropertyDecoderRegistry.getDecoder(ActivitySetModelBean.class, ProcessorFactoryAdapter.class);
		if (decoder == null) {
			throw new NullPointerException("No decoder found for " + this.bean.getClass().getName()); //$NON-NLS-1$
		}
		DecodeRunIdentification<ProcessorFactoryAdapter> ident =
			decoder.decode(targetSet, this.bean);
	    PropertiedObjectFilter<ProcessorFactoryAdapter> factoriesFilter = 
			new ObjectMembershipFilter<ProcessorFactoryAdapter>(ident.getAffectedObjects());
	    ActivityQueryRunIdentification result = new ActivityQueryRunIdentification ();
	    
		result.setName("Load at " + (new Date()).toString()); //$NON-NLS-1$
		result.setObjectFilter(factoriesFilter);
		result.setTimeOfRun(ident.getTimeOfRun());
		result.setKind("allactivities"); //$NON-NLS-1$
		result.setPropertyKeyProfile(ident.getPropertyKeyProfile());

		this.lastRun = result;
		return result;
	}

	/**
	 * @see net.sf.taverna.t2.drizzle.query.ActivityQuery#lastRun()
	 */
	public ActivitySubsetIdentification lastRun() {
		return this.lastRun;
	}

}
