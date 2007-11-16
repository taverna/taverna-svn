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

/**
 * @author alanrw
 *
 */
public final class ActivityScavengerQuery implements ActivityQuery <Scavenger>{
	
	private Scavenger scavenger;
	private ActivityQueryRunIdentification lastRun = null;
	
	public ActivityScavengerQuery(Scavenger configuration) {
		configure(configuration);
	}

	public void configure(Scavenger configuration) {
		if (configuration == null) {
			throw new NullPointerException ("configuration cannot be null");
		}
		this.scavenger = configuration;
	}

	public ActivityQueryRunIdentification runQuery(PropertiedObjectSet<ProcessorFactory> targetSet) {
		if (targetSet == null) {
			throw new NullPointerException("targetSet cannot be null");
		}
		PropertyDecoder decoder = PropertyDecoderRegistry.getDecoder(Scavenger.class, ProcessorFactory.class);
		if (decoder == null) {
			throw new NullPointerException("No decoder found for " + this.scavenger.getClass().getName());
		}
		final Set<ProcessorFactory> factories =
			decoder.decode(targetSet, scavenger);
		final PropertiedObjectFilter<ProcessorFactory> factoriesFilter = 
			new ObjectMembershipFilter<ProcessorFactory>(factories);
		final long timeOfRun = System.currentTimeMillis();
		ActivityQueryRunIdentification result = new ActivityQueryRunIdentification () {

			public String getName() {
				return scavenger.toString();
			}

			public PropertiedObjectFilter<ProcessorFactory> getObjectFilter() {
				return factoriesFilter;
			}

			public long getTimeOfRun() {
				return timeOfRun;
			}
			
		};
		lastRun = result;
		return result;
	}

	public ActivityQueryRunIdentification lastRun() {
		return this.lastRun;
	}

}
