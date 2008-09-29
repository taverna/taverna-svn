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

import javax.xml.bind.JAXBException;

import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.entity.impl.DataDocumentImpl;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
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

	@SuppressWarnings("unchecked")
	@Test
	public void testDataDocument() throws JDOMException, IOException,
			JAXBException {
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

		assertEquals(urn, doc.getIdentifier().getAsURI());
		assertEquals(3, doc.getReferenceSchemes().size());

		DataDocument newDoc = serialisedBeanable(doc);
		assertEquals(urn, newDoc.getIdentifier().getAsURI());
		final Set<ReferenceScheme> retrievedRefs = newDoc.getReferenceSchemes();
		assertNotSame("Did not reconstruct set", retrievedRefs, refSchemes);
		// Fails due to lacking blob support
		assertEquals(3, retrievedRefs.size());
		assertTrue(retrievedRefs.containsAll(refSchemes));
	}


	@Test
	public void testEntityList() throws JAXBException {
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

		EntityList newList = serialisedBeanable(list);
		assertEquals(list, newList);
		assertEquals(2, newList.size());
		assertEquals(id2, newList.get(0));
		assertEquals(id3, newList.get(1));
		assertEquals(id, newList.getIdentifier());
	}


	@Test
	public void testErrorDocument() throws JDOMException, IOException,
			JAXBException {
		ErrorDocumentIdentifier id = new ErrorDocumentIdentifier(
				"urn:t2data:error://fish/error1/3/2");
		Throwable throwable = new Throwable("failure", new Exception(
				"total failure"));
		ErrorDocument doc = new ErrorDocument(id, "did not work", throwable);
		assertEquals("urn:t2data:error://fish/error1/3/2", doc.getIdentifier()
				.getAsURI());
		assertEquals(throwable, doc.getCause());
		assertEquals("did not work", doc.getMessage());

		ErrorDocument newDoc = serialisedBeanable(doc);
		assertEquals("urn:t2data:error://fish/error1/3/2", newDoc
				.getIdentifier().getAsURI());
		// null because we can't serialise a Throwable
		assertNull(newDoc.getCause());
		assertEquals("did not work", newDoc.getMessage());
	}


	@Test
	public void testLiteral() throws MalformedIdentifierException,
			UnsupportedEncodingException, JAXBException {
		Literal id = new Literal(
				"urn:t2data:literal://string.literal/Some%20funky%2Fcharacters");
		assertEquals("string.literal", id.getNamespace());
		assertEquals(0, id.getDepth());
		assertEquals(IDType.Literal, id.getType());
		assertEquals("Some%20funky%2Fcharacters", id.getName());
		
		Literal newId = (Literal) serialisedBeanable(id);
		assertEquals("string.literal", newId.getNamespace());
		assertEquals(0, newId.getDepth());
		assertEquals(IDType.Literal, newId.getType());
		assertEquals("Some%20funky%2Fcharacters", newId.getName());
		assertEquals("Some funky/characters", newId.getValue());
	}

	@Test
	public void testLiteralFloat() throws MalformedIdentifierException,
			UnsupportedEncodingException, JAXBException {
		Literal id = new Literal("urn:t2data:literal://float.literal/-15.87");
		assertEquals("float.literal", id.getNamespace());
		assertEquals(0, id.getDepth());
		assertEquals(IDType.Literal, id.getType());
		assertEquals("-15.87", id.getName());
		
		Literal newId = (Literal) serialisedBeanable(id);
		assertEquals("float.literal", newId.getNamespace());
		assertEquals(0, newId.getDepth());
		assertEquals(IDType.Literal, newId.getType());
		assertEquals("-15.87", newId.getName());
	}

	@Test
	public void testLiteralInfinity() throws MalformedIdentifierException,
			UnsupportedEncodingException, JAXBException {
		Literal id = Literal.buildLiteral(Double.NEGATIVE_INFINITY);
		assertEquals("double.literal", id.getNamespace());
		assertEquals(0, id.getDepth());
		assertEquals(IDType.Literal, id.getType());
		assertEquals("-Infinity", id.getName());
		
		Literal newId = (Literal) serialisedBeanable(id);
		assertEquals("double.literal", newId.getNamespace());
		assertEquals(0, newId.getDepth());
		assertEquals(IDType.Literal, newId.getType());
		assertEquals("-Infinity", newId.getName());
		assertEquals(Double.NEGATIVE_INFINITY, newId.getValue());
	}

	@Test
	public void testLiteralMaxDouble() throws MalformedIdentifierException,
			UnsupportedEncodingException, JAXBException {
		Literal id = Literal.buildLiteral(Double.MAX_VALUE);
		assertEquals("double.literal", id.getNamespace());
		assertEquals(0, id.getDepth());
		assertEquals(IDType.Literal, id.getType());
		assertEquals("1.7976931348623157E308", id.getName());
		
		Literal newId = (Literal) serialisedBeanable(id);
		assertEquals("double.literal", newId.getNamespace());
		assertEquals(0, newId.getDepth());
		assertEquals(IDType.Literal, newId.getType());
		assertEquals("1.7976931348623157E308", newId.getName());
		assertEquals(Double.MAX_VALUE, newId.getValue());
	}

	@Test
	public void testURLReferenceScheme() throws IOException,
			DereferenceException, JAXBException {
		File newFile = File.createTempFile("test", ".txt");
		FileUtils.writeStringToFile(newFile, "Test data\n", "utf8");
		URL fileURL = newFile.toURI().toURL();
		HttpReferenceScheme urlRef = new HttpReferenceScheme(fileURL);
		InputStream stream = urlRef.dereference(dManager);
		assertEquals("Test data\n", IOUtils.toString(stream, "utf8"));

		HttpReferenceScheme newUrlRef = serialisedBeanable(urlRef);
		InputStream newStream = newUrlRef.dereference(dManager);
		assertEquals("Test data\n", IOUtils.toString(newStream, "utf8"));
	}

	/**
	 * Serialise and deserialise using {@link BeanSerialiser}.
	 * 
	 * @param bean
	 *            Bean to be serialised
	 * @return The deserialised bean
	 * @throws JAXBException
	 */
	@SuppressWarnings("unchecked")
	private <BeanableType extends Beanable> BeanableType serialisedBeanable(
			BeanableType bean) throws JAXBException {
		BeanSerialiser beanSerialiser = BeanSerialiser.getInstance();
		Element elem = beanSerialiser.beanableToXMLElement(bean);
		return (BeanableType) beanSerialiser.beanableFromXMLElement(elem);
	}


}
