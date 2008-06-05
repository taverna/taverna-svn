package net.sf.taverna.t2.reference.impl;

import java.util.HashSet;
import java.util.List;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.h3.ReferenceSetImpl;
import net.sf.taverna.t2.reference.h3.ReferenceSetT2ReferenceImpl;
import net.sf.taverna.t2.reference.impl.HibernateReferenceSetDao;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class DatabaseSetupTest {

	@Test
	public void testDatabaseReadWriteWithoutPlugins() {
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"vanillaHibernateAppContext.xml");
		HibernateReferenceSetDao o = (HibernateReferenceSetDao) context
				.getBean("testDao");
		ReferenceSetT2ReferenceImpl id = new ReferenceSetT2ReferenceImpl();
		id.setNamespacePart("testNamespace");
		id.setLocalPart("testLocal");
		ReferenceSetImpl rs = new ReferenceSetImpl(
				new HashSet<ExternalReferenceSPI>(), id);
		o.storeOrUpdate(rs);

		List<? extends ReferenceSet> returnedSet = o.getReferenceSetImpl(id);
		for (ReferenceSet set : returnedSet) {
			System.out.println("Found a set! : " + set.toString());
		}
	}
}
