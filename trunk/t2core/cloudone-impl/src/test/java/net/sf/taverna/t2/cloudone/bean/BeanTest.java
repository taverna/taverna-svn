package net.sf.taverna.t2.cloudone.bean;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.entity.impl.DataDocumentImpl;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceScheme;
import net.sf.taverna.t2.cloudone.util.EntitySerialiser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.junit.Test;


public class BeanTest {

	InMemoryDataManager dManager = new InMemoryDataManager("dataNS",
			new HashSet<LocationalContext>());

	XMLOutputter xo = new XMLOutputter();
	
	
	@Test
	public void testDataDocumentIdentifier() {
		DataDocumentIdentifier id = new DataDocumentIdentifier("urn:t2data:ddoc://dataNS/data0");
		assertEquals("dataNS", id.getNamespace());
		assertEquals(0, id.getDepth());
		assertEquals(IDType.Data, id.getType());
		assertEquals("data0", id.getName());
		String bean = serialised(id.getAsBean());
		
		DataDocumentIdentifier newId = (DataDocumentIdentifier) EntityIdentifiers.parse(bean);
		assertEquals("dataNS", newId.getNamespace());
		assertEquals(0, newId.getDepth());
		assertEquals(IDType.Data, newId.getType());
		assertEquals("data0", newId.getName());
		assertEquals(bean, newId.getAsBean());
		
	}
	
	@Test
	public void testEntityListIdentifier() {
		EntityListIdentifier id = new EntityListIdentifier("urn:t2data:list://fish/list5311/2");
		assertEquals("fish", id.getNamespace());
		assertEquals(2, id.getDepth());
		assertEquals(IDType.List, id.getType());
		assertEquals("list5311", id.getName());
		String bean = serialised(id.getAsBean());
		
		EntityListIdentifier newId = (EntityListIdentifier) EntityIdentifiers.parse(bean);
		assertEquals("fish", newId.getNamespace());
		assertEquals(2, newId.getDepth());
		assertEquals(IDType.List, newId.getType());
		assertEquals("list5311", newId.getName());
		assertEquals(bean, newId.getAsBean());
	}
	
	@Test
	public void testErrorDocumentIdentifier() {
		ErrorDocumentIdentifier id = new ErrorDocumentIdentifier("urn:t2data:error://fish/error1/3/2");
		assertEquals("fish", id.getNamespace());
		assertEquals(3, id.getDepth());
		assertEquals(2, id.getImplicitDepth());
		assertEquals(IDType.Error, id.getType());
		assertEquals("error1", id.getName());
		String bean = serialised(id.getAsBean());
		
		ErrorDocumentIdentifier newId = (ErrorDocumentIdentifier) EntityIdentifiers.parse(bean);
		assertEquals("fish", newId.getNamespace());
		assertEquals(3, newId.getDepth());
		assertEquals(2, newId.getImplicitDepth());
		assertEquals(IDType.Error, newId.getType());
		assertEquals("error1", newId.getName());
		assertEquals(bean, newId.getAsBean());
	}
	
	@Test
	public void testLiteral() throws MalformedIdentifierException, UnsupportedEncodingException {
		Literal id = new Literal("urn:t2data:literal://double.literal/-15.87");
		assertEquals("double.literal", id.getNamespace());
		assertEquals(0, id.getDepth());
		assertEquals(IDType.Literal, id.getType());
		assertEquals("-15.87", id.getName());
		String bean = serialised(id.getAsBean());
		
		Literal newId = (Literal) EntityIdentifiers.parse(bean);
		assertEquals("double.literal", newId.getNamespace());
		assertEquals(0, newId.getDepth());
		assertEquals(IDType.Literal, newId.getType());
		assertEquals("-15.87", newId.getName());
		assertEquals(bean, newId.getAsBean());
	}
	
	@Test
	public void testErrorDocument() throws JDOMException, IOException {
		ErrorDocumentIdentifier id = new ErrorDocumentIdentifier("urn:t2data:error://fish/error1/3/2");
		Throwable throwable = new Throwable("failure", new Exception("total failure"));
		ErrorDocument doc = new ErrorDocument(id, "did not work", throwable);
		assertEquals("urn:t2data:error://fish/error1/3/2", doc.getIdentifier().getAsBean());
		assertEquals(throwable, doc.getCause());
		assertEquals("did not work", doc.getMessage());
		
		ErrorDocumentBean bean = serialised(doc.getAsBean());
		ErrorDocument newDoc = new ErrorDocument();
		newDoc.setFromBean(bean);
		assertEquals("urn:t2data:error://fish/error1/3/2", newDoc.getIdentifier().getAsBean());
		//null because we can't serialise a Throwable
		assertNull(newDoc.getCause());
		assertEquals("did not work", newDoc.getMessage());
	}

	@Test
	public void testDataDocument() throws JDOMException, IOException {
		String urn = "urn:t2data:ddoc://dataNS/data0";
		
		// Generate a set of two reference schemes
		DataDocumentIdentifier id = new DataDocumentIdentifier(urn);
		HashSet<ReferenceScheme> refSchemes = new HashSet<ReferenceScheme>();
		URL url1 = new URL("http://taverna.sourceforge.net/");
		refSchemes.add(new URLReferenceScheme(url1));
		URL url2 = new URL("http://taverna.sourceforge.net/");
		refSchemes.add(new URLReferenceScheme(url2)); // Duplicate to be ignored
		URL url3 = new URL("http://www.mygrid.org.uk/");
		refSchemes.add(new URLReferenceScheme(url3));
		URL url4 = new URL("http://mygrid.org.uk/");
		
		// Disabled because Java looks up in DNS
		//assertEquals("Java not buggy", url3, url4);
		// should DIFFER from url3 although Javas URL thing it equals
		refSchemes.add(new URLReferenceScheme(url4));

		
		DataDocument doc = new DataDocumentImpl(id, refSchemes);
		
		assertEquals(urn, doc.getIdentifier().getAsBean());
		assertEquals(3, doc.getReferenceSchemes().size());
		
		DataDocumentBean bean = serialised(doc.getAsBean());
		DataDocument newDoc = new DataDocumentImpl();
		newDoc.setFromBean(bean);
		assertEquals(urn, newDoc.getIdentifier().getAsBean());
		assertNotSame("Did not reconstruct set", newDoc.getReferenceSchemes(), refSchemes); 
		assertEquals(3, newDoc.getReferenceSchemes().size());
		assertTrue(newDoc.getReferenceSchemes().containsAll(refSchemes));
	}
	
	@Test
	public void testEntityList() {
		EntityListIdentifier id = new EntityListIdentifier("urn:t2data:list://myNS/list1/2");
		EntityListIdentifier id2 = new EntityListIdentifier("urn:t2data:list://myNS/list2/4");
		EntityListIdentifier id3 = new EntityListIdentifier("urn:t2data:list://myNS/list3/3");
		List<EntityIdentifier> entList = new ArrayList<EntityIdentifier>();
		entList.add(id2);
		entList.add(id3);
		EntityList list = new EntityList(id, entList);
		assertEquals(2, list.size());
		assertEquals(id2, list.get(0));
		assertEquals(id3, list.get(1));
		assertEquals(id, list.getIdentifier());
		
		EntityListBean bean = serialised(list.getAsBean());
		EntityList newList = new EntityList();
		newList.setFromBean(bean);
		assertEquals(list, newList);
		assertEquals(2, newList.size());
		assertEquals(id2, newList.get(0));
		assertEquals(id3, newList.get(1));
		assertEquals(id, newList.getIdentifier());	
	}
	
	@Test
	public void testURLReferenceScheme () throws IOException, DereferenceException {
		
		URL url1 = new URL("http://taverna.sourceforge.net/");
		File newFile = File.createTempFile("test", ".txt");
		FileUtils.writeStringToFile(newFile, "Test data\n", "utf8");
		URL fileURL = newFile.toURI().toURL();
		URLReferenceScheme urlRef = new URLReferenceScheme(fileURL);
		InputStream stream = urlRef.dereference(dManager);
		assertEquals("Test data\n", IOUtils.toString(stream, "utf8"));
		
		
		String bean = serialised(urlRef.getAsBean());
		URLReferenceScheme newUrlRef = new URLReferenceScheme();
		newUrlRef.setFromBean(bean);
		InputStream newStream = newUrlRef.dereference(dManager);
		assertEquals("Test data\n", IOUtils.toString(newStream, "utf8"));
	}
	
	/**
	 * Serialise and deserialise using {@link EntitySerialiser}
	 * 
	 * @param bean Bean to be serialised
	 * @return The deserialised bean
	 */
	@SuppressWarnings("unchecked")
	private <Bean> Bean serialised(Bean bean) {
		ClassLoader cl =  bean.getClass().getClassLoader();
		Element elem;
		try {
			elem = EntitySerialiser.toXML(bean);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return (Bean) EntitySerialiser.fromXML(elem, cl);
	}
	
}
