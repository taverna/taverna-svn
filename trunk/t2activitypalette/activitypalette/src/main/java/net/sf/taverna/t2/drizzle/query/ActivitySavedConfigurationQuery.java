/**
 * 
 */
package net.sf.taverna.t2.drizzle.query;

import java.util.Date;

import net.sf.taverna.t2.drizzle.bean.ActivityPaletteModelBean;
import net.sf.taverna.t2.drizzle.decoder.PropertyDecoder;
import net.sf.taverna.t2.drizzle.decoder.PropertyDecoderRegistry;
import net.sf.taverna.t2.drizzle.model.ActivityQueryRunIdentification;
import net.sf.taverna.t2.drizzle.model.ActivityRegistrySubsetIdentification;
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
public final class ActivitySavedConfigurationQuery implements ActivityQuery <ActivityPaletteModelBean>{
	
	ActivityPaletteModelBean bean;
	
	private ActivityRegistrySubsetIdentification lastRun = null;
	
	public ActivitySavedConfigurationQuery(ActivityPaletteModelBean configuration) {
		if (configuration == null) {
			throw new NullPointerException("configuration cannot be null"); //$NON-NLS-1$
		}
		configure(configuration);
	}

	public void configure(ActivityPaletteModelBean configuration) {
		if (configuration == null) {
			throw new NullPointerException ("configuration cannot be null"); //$NON-NLS-1$
		}
		this.bean = configuration;
	}

	public ActivityRegistrySubsetIdentification runQuery(PropertiedObjectSet<ProcessorFactoryAdapter> targetSet) {
		if (targetSet == null) {
			throw new NullPointerException("targetSet cannot be null"); //$NON-NLS-1$
		}
	    PropertyDecoder<ActivityPaletteModelBean, ProcessorFactoryAdapter> decoder = PropertyDecoderRegistry.getDecoder(ActivityPaletteModelBean.class, ProcessorFactoryAdapter.class);
		if (decoder == null) {
			throw new NullPointerException("No decoder found for " + this.bean.getClass().getName()); //$NON-NLS-1$
		}
		DecodeRunIdentification<ProcessorFactoryAdapter> ident =
			decoder.decode(targetSet, this.bean);
	    PropertiedObjectFilter<ProcessorFactoryAdapter> factoriesFilter = 
			new ObjectMembershipFilter<ProcessorFactoryAdapter>(ident.getAffectedObjects());
	    ActivityQueryRunIdentification result = new ActivityQueryRunIdentification ();
	    
		result.setName("Load at " + (new Date()).toString());
		result.setObjectFilter(factoriesFilter);
		result.setTimeOfRun(ident.getTimeOfRun());
		result.setKind("allactivities");
		result.setPropertyKeyProfile(ident.getPropertyKeyProfile());

		this.lastRun = result;
		return result;
	}

	public ActivityRegistrySubsetIdentification lastRun() {
		return this.lastRun;
	}

}
