package net.sf.taverna.t2.cloudone.refscheme.blob;

import net.sf.taverna.t2.util.beanable.BeanableFactory;
import net.sf.taverna.t2.util.beanable.BeanableFactoryRegistry;
import net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser;

/**
 * Factory for associating {@link BlobReferenceSchemeImpl} with
 * {@link BlobReferenceBean} for the purpose of serialising/deserialising
 * 
 * @see BeanSerialiser
 * @see BeanableFactory
 * @see BeanableFactoryRegistry
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class BlobReferenceSchemeFactory extends
		BeanableFactory<BlobReferenceSchemeImpl, BlobReferenceBean> {

	public BlobReferenceSchemeFactory() {
		super(BlobReferenceSchemeImpl.class, BlobReferenceBean.class);
	}

}
