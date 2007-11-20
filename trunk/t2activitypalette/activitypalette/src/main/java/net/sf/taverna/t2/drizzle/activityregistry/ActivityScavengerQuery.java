/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

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
	private ActivityQueryRunIdentification lastRun = null;
	
	public ActivityScavengerQuery(Scavenger configuration) {
		configure(configuration);
	}

	public void configure(Scavenger configuration) {
		if (configuration == null) {
			throw new NullPointerException ("configuration cannot be null"); //$NON-NLS-1$
		}
		this.scavenger = configuration;
	}

	public ActivityQueryRunIdentification runQuery(PropertiedObjectSet<ProcessorFactory> targetSet) {
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
		result.setName(this.scavenger.toString());
		result.setObjectFilter(factoriesFilter);
		result.setTimeOfRun(ident.getTimeOfRun());
		result.setPropertyKeyProfile(ident.getPropertyKeyProfile());

		this.lastRun = result;
		return result;
	}

	public ActivityQueryRunIdentification lastRun() {
		return this.lastRun;
	}

}
