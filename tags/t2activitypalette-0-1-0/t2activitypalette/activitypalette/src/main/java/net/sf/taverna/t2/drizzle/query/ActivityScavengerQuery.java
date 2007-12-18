/**
 * 
 */
package net.sf.taverna.t2.drizzle.query;

import net.sf.taverna.t2.drizzle.decoder.PropertyDecoder;
import net.sf.taverna.t2.drizzle.decoder.PropertyDecoderRegistry;
import net.sf.taverna.t2.drizzle.model.ActivityQueryRunIdentification;
import net.sf.taverna.t2.drizzle.model.ActivitySubsetIdentification;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.util.ObjectMembershipFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class ActivityScavengerQuery implements ActivityQuery <Scavenger>{
	
	Scavenger scavenger;
	private ActivitySubsetIdentification lastRun = null;
	
	/**
	 * @param configuration
	 */
	public ActivityScavengerQuery(Scavenger configuration) {
		if (configuration == null) {
			throw new NullPointerException("configuration cannot be null"); //$NON-NLS-1$
		}
		configure(configuration);
	}

	/**
	 * @see net.sf.taverna.t2.drizzle.query.ActivityQuery#configure(java.lang.Object)
	 */
	public void configure(Scavenger configuration) {
		if (configuration == null) {
			throw new NullPointerException ("configuration cannot be null"); //$NON-NLS-1$
		}
		this.scavenger = configuration;
	}

	/**
	 * @see net.sf.taverna.t2.drizzle.query.ActivityQuery#runQuery(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet)
	 */
	@SuppressWarnings("unchecked")
	public ActivitySubsetIdentification runQuery(PropertiedObjectSet<ProcessorFactoryAdapter> targetSet) {
		if (targetSet == null) {
			throw new NullPointerException("targetSet cannot be null"); //$NON-NLS-1$
		}
	    PropertyDecoder<Scavenger, ProcessorFactoryAdapter> decoder =
	    	PropertyDecoderRegistry.getDecoder(Scavenger.class, ProcessorFactoryAdapter.class);
		if (decoder == null) {
			throw new NullPointerException("No decoder found for " + this.scavenger.getClass().getName()); //$NON-NLS-1$
		}
		DecodeRunIdentification<ProcessorFactoryAdapter> ident =
			decoder.decode(targetSet, this.scavenger);
	    PropertiedObjectFilter<ProcessorFactoryAdapter> factoriesFilter = 
			new ObjectMembershipFilter<ProcessorFactoryAdapter>(ident.getAffectedObjects());
	    ActivityQueryRunIdentification result = new ActivityQueryRunIdentification ();
	    Object userObject = this.scavenger.getUserObject();
	    String scavengerName = null;
	    if (userObject instanceof ProcessorFactory) {
	    	scavengerName = ((ProcessorFactory)userObject).getName();
	    } else {
	    	scavengerName = this.scavenger.toString();
	    }
		result.setName(scavengerName);
		result.setObjectFilter(factoriesFilter);
		result.setTimeOfRun(ident.getTimeOfRun());
		result.setKind(this.scavenger.getClass().getName());
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
