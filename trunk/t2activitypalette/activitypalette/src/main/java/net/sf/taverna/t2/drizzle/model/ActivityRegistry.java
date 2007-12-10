/**
 * 
 */
package net.sf.taverna.t2.drizzle.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.drizzle.bean.ActivityRegistryBean;
import net.sf.taverna.t2.drizzle.bean.ProcessorFactoryAdapterBean;
import net.sf.taverna.t2.drizzle.query.ActivityQuery;
import net.sf.taverna.t2.drizzle.query.ActivitySavedConfigurationQuery;
import net.sf.taverna.t2.drizzle.util.ObjectFactory;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphView;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * @author alanrw
 *
 */
// TODO should this be forced to be a singleton?
public final class ActivityRegistry  implements Beanable<ActivityRegistryBean> {
	private PropertiedObjectSet<ProcessorFactoryAdapter> registry;
	private PropertiedGraphView<ProcessorFactoryAdapter> graphView;
	
	@SuppressWarnings("unchecked")
	public ActivityRegistry() {
		this.registry = ObjectFactory.getInstance(PropertiedObjectSet.class);
		this.graphView = ObjectFactory.getInstance(PropertiedGraphView.class);
		this.graphView.setPropertiedObjectSet(this.registry);
	}

	public synchronized ActivityRegistrySubsetIdentification addImmediateQuery(ActivityQuery<?> query) {
		if (query == null) {
			throw new NullPointerException("query cannot be null"); //$NON-NLS-1$
		}
		ActivityRegistrySubsetIdentification ident =
			query.runQuery(this.registry);
		return ident;
	}

	/**
	 * @return the registry
	 */
	public synchronized final PropertiedObjectSet<ProcessorFactoryAdapter> getRegistry() {
		return this.registry;
	}

	/**
	 * @return the graphView
	 */
	public synchronized final PropertiedGraphView<ProcessorFactoryAdapter> getGraphView() {
		return this.graphView;
	}
	
	public synchronized Set<PropertyKey> getAllPropertyKeys() {
		return this.registry.getAllPropertyKeys();
	}

	public ActivityRegistryBean getAsBean() {
		ActivityRegistryBean result = new ActivityRegistryBean();
		
		List<ProcessorFactoryAdapterBean> adapterBeans = new ArrayList<ProcessorFactoryAdapterBean>();
		for (ProcessorFactoryAdapter adapter : this.getRegistry().getObjects()) {
			ProcessorFactoryAdapterBean adapterBean = new ProcessorFactoryAdapterBean();

			adapterBean.setXmlFragment(adapter.getSerializedVersion());
			adapterBeans.add(adapterBean);
		}
		result.setAdapterBeans(adapterBeans);

		return result;
	}

	public void setFromBean(ActivityRegistryBean arg0) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}
	
	public void mergeWithBean(ActivityRegistryBean arg0){

		addImmediateQuery(new ActivitySavedConfigurationQuery(arg0));

	}
}
