package net.sf.taverna.t2.reference.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;

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

		// Retrieve with a new instance of an anonymous subclass of
		// ReferenceSetT2ReferenceImpl, just to check that hibernate can cope
		// with this. It can, but this *must* be a subclass of the registered
		// component type, which means we need to modify the component type to
		// be the fully generic T2Reference with all fields accessed via
		// properties.
		T2Reference newReference = new ReferenceSetT2ReferenceImpl() {

			public boolean containsErrors() {
				return false;
			}

			public int getDepth() {
				return 0;
			}

			public String getLocalPart() {
				return "testLocal";
			}

			public String getNamespacePart() {
				return "testNamespace";
			}

			URI cachedUri = null;

			public synchronized URI toUri() {
				if (cachedUri != null) {
					return cachedUri;
				} else {
					try {
						URI result = new URI("t2:ref//" + getNamespacePart()
								+ "?" + getLocalPart());
						cachedUri = result;
						return result;
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}
				}
			}

			/**
			 * Use the equality operator over the URI representation of this
			 * bean.
			 */
			@Override
			public boolean equals(Object other) {
				if (other == this) {
					return true;
				}
				if (other instanceof ReferenceSetT2ReferenceImpl) {
					ReferenceSetT2ReferenceImpl otherRef = (ReferenceSetT2ReferenceImpl) other;
					return (toUri().equals(otherRef.toUri()));
				} else {
					return false;
				}
			}

			/**
			 * Use hashcode method from the URI representation of this bean
			 */
			@Override
			public int hashCode() {
				return toUri().hashCode();
			}
		};

		ReferenceSet returnedset = o.get(newReference);
		System.out.println(returnedset);

	}

}
