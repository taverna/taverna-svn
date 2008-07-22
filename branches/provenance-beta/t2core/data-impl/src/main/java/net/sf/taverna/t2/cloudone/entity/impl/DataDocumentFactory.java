package net.sf.taverna.t2.cloudone.entity.impl;

import net.sf.taverna.t2.cloudone.bean.DataDocumentBean;
import net.sf.taverna.t2.util.beanable.BeanableFactory;
import net.sf.taverna.t2.util.beanable.BeanableFactoryRegistry;
import net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser;

/**
 * Factory for relating the {@link DataDocumentImpl} with its bean, the
 * {@link DataDocumentBean} for the purpose of serialisation/deserialisation
 * 
 * @see BeanSerialiser
 * @see BeanableFactory
 * @see BeanableFactoryRegistry
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class DataDocumentFactory extends
		BeanableFactory<DataDocumentImpl, DataDocumentBean> {

	public DataDocumentFactory() {
		super(DataDocumentImpl.class, DataDocumentBean.class);
	}

}
