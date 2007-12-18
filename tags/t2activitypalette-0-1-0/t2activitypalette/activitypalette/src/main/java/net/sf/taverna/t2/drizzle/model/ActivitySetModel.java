/**
 * 
 */
package net.sf.taverna.t2.drizzle.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.drizzle.bean.ActivitySetModelBean;
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
public final class ActivitySetModel  implements Beanable<ActivitySetModelBean> {
	private PropertiedObjectSet<ProcessorFactoryAdapter> propertiedProcessorFactoryAdapterSet;
	private PropertiedGraphView<ProcessorFactoryAdapter> graphView;
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public ActivitySetModel() {
		this.propertiedProcessorFactoryAdapterSet = ObjectFactory.getInstance(PropertiedObjectSet.class);
		this.graphView = ObjectFactory.getInstance(PropertiedGraphView.class);
		this.graphView.setPropertiedObjectSet(this.propertiedProcessorFactoryAdapterSet);
	}

	/**
	 * @param query
	 * @return
	 */
	public synchronized ActivitySubsetIdentification addImmediateQuery(ActivityQuery<?> query) {
		if (query == null) {
			throw new NullPointerException("query cannot be null"); //$NON-NLS-1$
		}
		ActivitySubsetIdentification ident =
			query.runQuery(this.propertiedProcessorFactoryAdapterSet);
		return ident;
	}

	/**
	 * @return the propertiedProcessorFactoryAdapterSet
	 */
	public synchronized final PropertiedObjectSet<ProcessorFactoryAdapter> getPropertiedProcessorFactoryAdapterSet() {
		return this.propertiedProcessorFactoryAdapterSet;
	}

	/**
	 * @return the graphView
	 */
	public synchronized final PropertiedGraphView<ProcessorFactoryAdapter> getGraphView() {
		return this.graphView;
	}
	
	public synchronized Set<PropertyKey> getAllPropertyKeys() {
		return this.propertiedProcessorFactoryAdapterSet.getAllPropertyKeys();
	}

	/**
	 * @see net.sf.taverna.t2.util.beanable.Beanable#getAsBean()
	 */
	public ActivitySetModelBean getAsBean() {
		ActivitySetModelBean result = new ActivitySetModelBean();
		
		List<ProcessorFactoryAdapterBean> adapterBeans = new ArrayList<ProcessorFactoryAdapterBean>();
		for (ProcessorFactoryAdapter adapter : this.getPropertiedProcessorFactoryAdapterSet().getObjects()) {
			ProcessorFactoryAdapterBean adapterBean = new ProcessorFactoryAdapterBean();

			adapterBean.setXmlFragment(adapter.getSerializedVersion());
			adapterBeans.add(adapterBean);
		}
		result.setAdapterBeans(adapterBeans);

		return result;
	}

	/**
	 * @see net.sf.taverna.t2.util.beanable.Beanable#setFromBean(java.lang.Object)
	 */
	public void setFromBean(ActivitySetModelBean arg0) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @param arg0
	 */
	public void mergeWithBean(ActivitySetModelBean arg0){

		addImmediateQuery(new ActivitySavedConfigurationQuery(arg0));

	}
}
