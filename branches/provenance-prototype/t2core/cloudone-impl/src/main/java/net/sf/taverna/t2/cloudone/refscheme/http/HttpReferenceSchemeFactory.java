package net.sf.taverna.t2.cloudone.refscheme.http;

import net.sf.taverna.t2.util.beanable.BeanableFactory;
import net.sf.taverna.t2.util.beanable.BeanableFactoryRegistry;
import net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser;

/**
 * Used to associate the {@link HttpReferenceScheme} with
 * {@link HttpReferenceBean} for the purpose of serialisation/deserialisation
 * 
 * @see BeanSerialiser
 * @see BeanableFactory
 * @see BeanableFactoryRegistry
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class HttpReferenceSchemeFactory extends
		BeanableFactory<HttpReferenceScheme, HttpReferenceBean> {

	public HttpReferenceSchemeFactory() {
		super(HttpReferenceScheme.class, HttpReferenceBean.class);
	}

}
