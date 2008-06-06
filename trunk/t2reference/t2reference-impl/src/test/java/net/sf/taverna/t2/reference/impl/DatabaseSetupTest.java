package net.sf.taverna.t2.reference.impl;

import java.util.HashSet;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.h3.ReferenceSetImpl;
import net.sf.taverna.t2.reference.h3.ReferenceSetT2ReferenceImpl;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DatabaseSetupTest {

	@Test
	public void testDatabaseReadWriteWithoutPlugins() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"vanillaHibernateAppContext.xml");
		HibernateReferenceSetDao o = (HibernateReferenceSetDao) context
				.getBean("testDao");
		ReferenceSetT2ReferenceImpl id = new ReferenceSetT2ReferenceImpl();
		id.setNamespacePart("testNamespace");
		id.setLocalPart("testLocal");
		ReferenceSetImpl rs = new ReferenceSetImpl(
				new HashSet<ExternalReferenceSPI>(), id);
		o.store(rs);

		ReferenceSet returnedset = o.get(id);
		System.out.println(returnedset);

	}

}
