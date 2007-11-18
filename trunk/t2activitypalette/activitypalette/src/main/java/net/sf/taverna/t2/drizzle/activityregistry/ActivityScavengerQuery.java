/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.Set;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.drizzle.util.ObjectMembershipFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;

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
		final PropertyDecoder<Scavenger, ProcessorFactory> decoder = PropertyDecoderRegistry.getDecoder(Scavenger.class, ProcessorFactory.class);
		if (decoder == null) {
			throw new NullPointerException("No decoder found for " + this.scavenger.getClass().getName()); //$NON-NLS-1$
		}
		final Set<ProcessorFactory> factories =
			decoder.decode(targetSet, this.scavenger);
		final PropertiedObjectFilter<ProcessorFactory> factoriesFilter = 
			new ObjectMembershipFilter<ProcessorFactory>(factories);
		final long timeOfRun = System.currentTimeMillis();
		ActivityQueryRunIdentification result = new ActivityQueryRunIdentification () {

			public String getName() {
				return ActivityScavengerQuery.this.scavenger.toString();
			}

			public PropertiedObjectFilter<ProcessorFactory> getObjectFilter() {
				return factoriesFilter;
			}

			public long getTimeOfRun() {
				return timeOfRun;
			}

			public Set<PropertyKey> getPropertyKeyProfile() {
				return decoder.getPropertyKeyProfile();
			}
			
		};
		this.lastRun = result;
		return result;
	}

	public ActivityQueryRunIdentification lastRun() {
		return this.lastRun;
	}

}
