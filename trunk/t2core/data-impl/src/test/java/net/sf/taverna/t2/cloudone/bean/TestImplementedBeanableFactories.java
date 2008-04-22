package net.sf.taverna.t2.cloudone.bean;

import static org.junit.Assert.assertSame;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.entity.impl.DataDocumentImpl;
import net.sf.taverna.t2.cloudone.refscheme.blob.BlobReferenceSchemeImpl;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;
import net.sf.taverna.t2.util.beanable.Beanable;
import net.sf.taverna.t2.util.beanable.BeanableFactory;
import net.sf.taverna.t2.util.beanable.BeanableFactoryRegistry;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class TestImplementedBeanableFactories {

	private BeanableFactoryRegistry registry = BeanableFactoryRegistry.getInstance();

	Class<? extends Beanable>[] beanableClasses = new Class[]{
			Literal.class,
			HttpReferenceScheme.class,
			BlobReferenceSchemeImpl.class,
			EntityList.class,
			ErrorDocument.class,
			DataDocumentImpl.class,
	};
	

	@Test
	public void getBeanables() {
		for (Class<? extends Beanable> c : beanableClasses) {
			BeanableFactory factory = registry.getFactoryForBeanableType(c.getCanonicalName());
			assertSame("SPI registry didn't return same class", c, factory.getBeanableType());
		}
	}

}
