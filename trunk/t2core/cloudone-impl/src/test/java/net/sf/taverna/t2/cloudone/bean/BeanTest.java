package net.sf.taverna.t2.cloudone.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceBean;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;
import net.sf.taverna.t2.util.beanable.Beanable;
import net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

/**
 * Test bean serialisation of {@link Beanable}s using {@link BeanSerialiser}.
 *
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public class BeanTest {

	InMemoryDataManager dManager = new InMemoryDataManager("dataNS",
			new HashSet<LocationalContext>());

	XMLOutputter xo = new XMLOutputter();

	@Test
	public void testDataDocument() throws JDOMException, IOException {
		String urn = "urn:t2data:ddoc://dataNS/data0";

		// Generate a set of two reference schemes
		DataDocumentIdentifier id = new DataDocumentIdentifier(urn);
		HashSet<ReferenceScheme> refSchemes = new HashSet<ReferenceScheme>();
		URL url1 = new URL("http://taverna.sourceforge.net/");
		refSchemes.add(new HttpReferenceScheme(url1));
		URL url2 = new URL("http://taverna.sourceforge.net/");
		refSchemes.add(new HttpReferenceScheme(url2)); // Duplicate to be
		// ignored
		URL url3 = new URL("http://www.mygrid.org.uk/");
		refSchemes.add(new HttpReferenceScheme(url3));
		URL url4 = new URL("http://mygrid.org.uk/");

		// Disabled because Java looks up in DNS
		// assertEquals("Java not buggy", url3, url4);
		// should DIFFER from url3 although Javas URL thing it equals
		refSchemes.add(new HttpReferenceScheme(url4));
		DataDocument doc = new DataDocumentImpl(id, refSchemes);

		assertEquals(urn, doc.getIdentifier().getAsBean());
		assertEquals(3, doc.getReferenceSchemes().size());

		DataDocumentBean bean = serialised(doc.getAsBean());
		List<ReferenceBean> refs = bean.getReferences();
		
		DataDocument newDoc = new DataDocumentImpl();
		newDoc.setFromBean(bean);
		assertEquals(urn, newDoc.getIdentifier().getAsBean());
		final Set<ReferenceScheme> retrievedRefs = newDoc.getReferenceSchemes();
		assertNotSame("Did not reconstruct set", retrievedRefs, refSchemes);
		// Fails due to lacking blob support
		assertEquals(3, retrievedRefs.size());
		System.out.println(retrievedRefs.iterator().next());
		assertTrue(retrievedRefs.containsAll(refSchemes));
	}

	@Test
	public void testDataDocumentIdentifier() {
		DataDocumentIdentifier id = new DataDocumentIdentifier(
				"urn:t2data:ddoc://dataNS/data0");
		assertEquals("dataNS", id.getNamespace());
		assertEquals(0, id.getDepth());
		assertEquals(IDType.Data, id.getType());
		assertEquals("data0", id.getName());
		String bean = serialised(id.getAsBean());

		DataDocumentIdentifier newId = (DataDocumentIdentifier) EntityIdentifiers
				.parse(bean);
		assertEquals("dataNS", newId.getNamespace());
		assertEquals(0, newId.getDepth());
		assertEquals(IDType.Data, newId.getType());
		assertEquals("data0", newId.getName());
		assertEquals(bean, newId.getAsBean());

	}

	@Test
	public void testEntityList() {
		EntityListIdentifier id = new EntityListIdentifier(
				"urn:t2data:list://myNS/list1/2");
		EntityListIdentifier id2 = new EntityListIdentifier(
				"urn:t2data:list://myNS/list2/4");
		EntityListIdentifier id3 = new EntityListIdentifier(
				"urn:t2data:list://myNS/list3/3");
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
	public void testEntityListIdentifier() {
		EntityListIdentifier id = new EntityListIdentifier(
				"urn:t2data:list://fish/list5311/2");
		assertEquals("fish", id.getNamespace());
		assertEquals(2, id.getDepth());
		assertEquals(IDType.List, id.getType());
		assertEquals("list5311", id.getName());
		String bean = serialised(id.getAsBean());

		EntityListIdentifier newId = (EntityListIdentifier) EntityIdentifiers
				.parse(bean);
		assertEquals("fish", newId.getNamespace());
		assertEquals(2, newId.getDepth());
		assertEquals(IDType.List, newId.getType());
		assertEquals("list5311", newId.getName());
		assertEquals(bean, newId.getAsBean());
	}

	@Test
	public void testErrorDocument() throws JDOMException, IOException {
		ErrorDocumentIdentifier id = new ErrorDocumentIdentifier(
				"urn:t2data:error://fish/error1/3/2");
		Throwable throwable = new Throwable("failure", new Exception(
				"total failure"));
		ErrorDocument doc = new ErrorDocument(id, "did not work", throwable);
		assertEquals("urn:t2data:error://fish/error1/3/2", doc.getIdentifier()
				.getAsBean());
		assertEquals(throwable, doc.getCause());
		assertEquals("did not work", doc.getMessage());

		ErrorDocumentBean bean = serialised(doc.getAsBean());
		ErrorDocument newDoc = new ErrorDocument();
		newDoc.setFromBean(bean);
		assertEquals("urn:t2data:error://fish/error1/3/2", newDoc
				.getIdentifier().getAsBean());
		// null because we can't serialise a Throwable
		assertNull(newDoc.getCause());
		assertEquals("did not work", newDoc.getMessage());
	}

	@Test
	public void testErrorDocumentIdentifier() {
		ErrorDocumentIdentifier id = new ErrorDocumentIdentifier(
				"urn:t2data:error://fish/error1/3/2");
		assertEquals("fish", id.getNamespace());
		assertEquals(3, id.getDepth());
		assertEquals(2, id.getImplicitDepth());
		assertEquals(IDType.Error, id.getType());
		assertEquals("error1", id.getName());
		String bean = serialised(id.getAsBean());

		ErrorDocumentIdentifier newId = (ErrorDocumentIdentifier) EntityIdentifiers
				.parse(bean);
		assertEquals("fish", newId.getNamespace());
		assertEquals(3, newId.getDepth());
		assertEquals(2, newId.getImplicitDepth());
		assertEquals(IDType.Error, newId.getType());
		assertEquals("error1", newId.getName());
		assertEquals(bean, newId.getAsBean());
	}

	@Test
	public void testLiteral() throws MalformedIdentifierException,
			UnsupportedEncodingException {
		Literal id = new Literal(
				"urn:t2data:literal://string.literal/Some%20funky%2Fcharacters");
		assertEquals("string.literal", id.getNamespace());
		assertEquals(0, id.getDepth());
		assertEquals(IDType.Literal, id.getType());
		assertEquals("Some%20funky%2Fcharacters", id.getName());
		String bean = serialised(id.getAsBean());

		Literal newId = (Literal) EntityIdentifiers.parse(bean);
		assertEquals("string.literal", newId.getNamespace());
		assertEquals(0, newId.getDepth());
		assertEquals(IDType.Literal, newId.getType());
		assertEquals("Some%20funky%2Fcharacters", newId.getName());
		assertEquals(bean, newId.getAsBean());
		assertEquals("Some funky/characters", newId.getValue());
	}

	@Test
	public void testLiteralFloat() throws MalformedIdentifierException,
			UnsupportedEncodingException {
		Literal id = new Literal("urn:t2data:literal://float.literal/-15.87");
		assertEquals("float.literal", id.getNamespace());
		assertEquals(0, id.getDepth());
		assertEquals(IDType.Literal, id.getType());
		assertEquals("-15.87", id.getName());
		String bean = serialised(id.getAsBean());

		Literal newId = (Literal) EntityIdentifiers.parse(bean);
		assertEquals("float.literal", newId.getNamespace());
		assertEquals(0, newId.getDepth());
		assertEquals(IDType.Literal, newId.getType());
		assertEquals("-15.87", newId.getName());
		assertEquals(bean, newId.getAsBean());
	}

	@Test
	public void testLiteralInfinity() throws MalformedIdentifierException,
			UnsupportedEncodingException {
		Literal id = Literal.buildLiteral(Double.NEGATIVE_INFINITY);
		assertEquals("double.literal", id.getNamespace());
		assertEquals(0, id.getDepth());
		assertEquals(IDType.Literal, id.getType());
		assertEquals("-Infinity", id.getName());
		String bean = serialised(id.getAsBean());

		Literal newId = (Literal) EntityIdentifiers.parse(bean);
		assertEquals("double.literal", newId.getNamespace());
		assertEquals(0, newId.getDepth());
		assertEquals(IDType.Literal, newId.getType());
		assertEquals("-Infinity", newId.getName());
		assertEquals(bean, newId.getAsBean());
		assertEquals(Double.NEGATIVE_INFINITY, newId.getValue());
	}

	@Test
	public void testLiteralMaxDouble() throws MalformedIdentifierException,
			UnsupportedEncodingException {
		Literal id = Literal.buildLiteral(Double.MAX_VALUE);
		assertEquals("double.literal", id.getNamespace());
		assertEquals(0, id.getDepth());
		assertEquals(IDType.Literal, id.getType());
		assertEquals("1.7976931348623157E308", id.getName());
		String bean = serialised(id.getAsBean());

		Literal newId = (Literal) EntityIdentifiers.parse(bean);
		assertEquals("double.literal", newId.getNamespace());
		assertEquals(0, newId.getDepth());
		assertEquals(IDType.Literal, newId.getType());
		assertEquals("1.7976931348623157E308", newId.getName());
		assertEquals(bean, newId.getAsBean());
		assertEquals(Double.MAX_VALUE, newId.getValue());
	}

	@Test
	public void testURLReferenceScheme() throws IOException,
			DereferenceException {
		File newFile = File.createTempFile("test", ".txt");
		FileUtils.writeStringToFile(newFile, "Test data\n", "utf8");
		URL fileURL = newFile.toURI().toURL();
		HttpReferenceScheme urlRef = new HttpReferenceScheme(fileURL);
		InputStream stream = urlRef.dereference(dManager);
		assertEquals("Test data\n", IOUtils.toString(stream, "utf8"));

		HttpReferenceBean bean = serialised(urlRef.getAsBean());
		HttpReferenceScheme newUrlRef = new HttpReferenceScheme();
		newUrlRef.setFromBean(bean);
		InputStream newStream = newUrlRef.dereference(dManager);
		assertEquals("Test data\n", IOUtils.toString(newStream, "utf8"));
	}

	/**
	 * Serialise and deserialise using {@link BeanSerialiser}.
	 *
	 * @param bean
	 *            Bean to be serialised
	 * @return The deserialised bean
	 */
	@SuppressWarnings("unchecked")
	private <Bean> Bean serialised(Bean bean) {
		ClassLoader cl = bean.getClass().getClassLoader();
		Element elem;
		elem = BeanSerialiser.toXML(bean);
		return (Bean) BeanSerialiser.fromXML(elem, cl);
	}

}
