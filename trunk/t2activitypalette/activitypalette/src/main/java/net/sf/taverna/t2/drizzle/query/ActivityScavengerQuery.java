/**
 * 
 */
package net.sf.taverna.t2.drizzle.query;

import net.sf.taverna.t2.drizzle.decoder.PropertyDecoder;
import net.sf.taverna.t2.drizzle.decoder.PropertyDecoderRegistry;
import net.sf.taverna.t2.drizzle.model.ActivityQueryRunIdentification;
import net.sf.taverna.t2.drizzle.model.ActivityRegistrySubsetIdentification;
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
	private ActivityRegistrySubsetIdentification lastRun = null;
	
	public ActivityScavengerQuery(Scavenger configuration) {
		if (configuration == null) {
			throw new NullPointerException("configuration cannot be null"); //$NON-NLS-1$
		}
		configure(configuration);
	}

	public void configure(Scavenger configuration) {
		if (configuration == null) {
			throw new NullPointerException ("configuration cannot be null"); //$NON-NLS-1$
		}
		this.scavenger = configuration;
	}

	public ActivityRegistrySubsetIdentification runQuery(PropertiedObjectSet<ProcessorFactory> targetSet) {
		if (targetSet == null) {
			throw new NullPointerException("targetSet cannot be null"); //$NON-NLS-1$
		}
	    PropertyDecoder<Scavenger, ProcessorFactory> decoder = PropertyDecoderRegistry.getDecoder(Scavenger.class, ProcessorFactory.class);
		if (decoder == null) {
			throw new NullPointerException("No decoder found for " + this.scavenger.getClass().getName()); //$NON-NLS-1$
		}
		DecodeRunIdentification<ProcessorFactory> ident =
			decoder.decode(targetSet, this.scavenger);
	    PropertiedObjectFilter<ProcessorFactory> factoriesFilter = 
			new ObjectMembershipFilter<ProcessorFactory>(ident.getAffectedObjects());
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

	public ActivityRegistrySubsetIdentification lastRun() {
		return this.lastRun;
	}

}
