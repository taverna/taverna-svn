package net.sf.taverna.t2.reference.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DatabaseSetupTest {

	@Test
	public void testListStorage() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"vanillaHibernateAppContext.xml");
		HibernateListDao o = (HibernateListDao)context.getBean("testListDao");
		T2ReferenceImpl listReference = new T2ReferenceImpl();
		listReference.setContainsErrors(false);
		listReference.setDepth(1);
		listReference.setLocalPart("list1");
		listReference.setNamespacePart("testNamespace");
		listReference.setReferenceType(T2ReferenceType.IdentifiedList);
		
		T2ReferenceListImpl l = new T2ReferenceListImpl();
		
		T2ReferenceImpl itemId1 = new T2ReferenceImpl();
		itemId1.setNamespacePart("testNamespace");
		itemId1.setLocalPart("item1");
		T2ReferenceImpl itemId2 = new T2ReferenceImpl();
		itemId2.setNamespacePart("testNamespace");
		itemId2.setLocalPart("item2");
		
		l.add(itemId1);
		l.add(itemId2);
		
		l.setTypedId(listReference);
		
		System.out.println(l);
		
		o.store(l);
		
		T2ReferenceImpl listReference2 = new T2ReferenceImpl();
		listReference2.setContainsErrors(false);
		listReference2.setDepth(1);
		listReference2.setLocalPart("list1");
		listReference2.setNamespacePart("testNamespace");
		listReference2.setReferenceType(T2ReferenceType.IdentifiedList);
		
		System.out.println(o.get(listReference2));
		
	}

	@Test
	public void testDatabaseReadWriteWithoutPlugins() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"vanillaHibernateAppContext.xml");
		HibernateReferenceSetDao o = (HibernateReferenceSetDao) context
				.getBean("testDao");
		T2ReferenceImpl id = new T2ReferenceImpl();
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
		T2Reference newReference = new T2ReferenceImpl() {

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
				if (other instanceof T2ReferenceImpl) {
					T2ReferenceImpl otherRef = (T2ReferenceImpl) other;
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
