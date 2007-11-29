package net.sf.taverna.t2.cloudone.refscheme.blob;

import net.sf.taverna.t2.util.beanable.BeanableFactory;

public class BlobReferenceSchemeFactory extends
		BeanableFactory<BlobReferenceSchemeImpl, BlobReferenceBean> {

	public BlobReferenceSchemeFactory() {
		super(BlobReferenceSchemeImpl.class, BlobReferenceBean.class);
	}

}
