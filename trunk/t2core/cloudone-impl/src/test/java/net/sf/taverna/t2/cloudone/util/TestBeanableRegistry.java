package net.sf.taverna.t2.cloudone.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import net.sf.taverna.t2.cloudone.bean.Beanable;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.entity.impl.DataDocumentImpl;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.impl.BlobReferenceSchemeImpl;
import net.sf.taverna.t2.cloudone.impl.http.HttpReferenceScheme;

@SuppressWarnings("unchecked")
public class TestBeanableRegistry {

	BeanableRegistry registry = BeanableRegistry.getInstance();

	Class<? extends Beanable>[] beanableClasses = new Class[]{
			DataDocumentIdentifier.class,
			EntityListIdentifier.class,
			ErrorDocumentIdentifier.class,
			Literal.class,
			HttpReferenceScheme.class,
			BlobReferenceSchemeImpl.class,
			EntityList.class,
			ErrorDocument.class,
			DataDocumentImpl.class,
	};
	
	@Before
	public void setUpRaven() {
		System.setProperty("raven.eclipse", "true");
	}
	
	@Test
	public void getBeanables() {
		for (Class<? extends Beanable> c : beanableClasses) {
			Beanable<?> beanable = registry.getBeanable(c.getCanonicalName());
			assertTrue("Was not an instance of " + c + ": " + beanable, 
					c.isInstance(beanable));
		}
	}

}
