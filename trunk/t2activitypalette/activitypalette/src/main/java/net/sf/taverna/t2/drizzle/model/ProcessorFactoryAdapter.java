/**
 * 
 */
package net.sf.taverna.t2.drizzle.model;

import net.sf.taverna.t2.drizzle.bean.ProcessorFactoryAdapterBean;
import net.sf.taverna.t2.util.beanable.Beanable;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class ProcessorFactoryAdapter<FactoryType extends ProcessorFactory> implements Beanable<ProcessorFactoryAdapterBean>{

	private FactoryType theFactory;
	
	public ProcessorFactoryAdapter(FactoryType theFactory) {
		this.theFactory = theFactory;
	}

	public ProcessorFactoryAdapterBean getAsBean() {
		// TODO
		return null;
	}

	public void setFromBean(ProcessorFactoryAdapterBean arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
	}

	/**
	 * @return the theFactory
	 */
	public synchronized final FactoryType getTheFactory() {
		return theFactory;
	}
	
}
