package net.sf.taverna.t2.cloudone.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.HashSet;

import javax.xml.bind.JAXBException;

import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.blob.BlobReferenceSchemeImpl;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceBean;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;
import net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;

/**
 * Test {@link BeanSerialiser} by serialising simple objects.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class BeanSerialiserTest {

	// private static final String SILLY = "I'm silly";

	private BeanSerialiser beanSerialiser = BeanSerialiser.getInstance();
	
	InMemoryDataManager dManager = new InMemoryDataManager("dataNS",
			new HashSet<LocationalContext>());

	// Removed after JAXB re-factoring
	// @Test
	// public void serialiseAsFile() throws IOException, JDOMException,
	// JAXBException {
	// File file = File.createTempFile("test", ".xml");
	// file.deleteOnExit();
	// file.delete();
	// assertFalse(file.exists());
	// SillyBean silly = new SillyBean();
	// silly.setName(SILLY);
	// BeanSerialiser.getInstance().toXMLFile(silly, file);
	// assertTrue(file.exists());
	// // Should be somewhere between 50 (it's XML) and 1024 :-)
	// assertTrue("Serialised file too small", file.length() > 50);
	// assertTrue("Serialised file too big", file.length() < 1024);
	// SillyBean silly2 = (SillyBean)
	// BeanSerialiser.getInstance().fromXMLFile(file);
	// assertEquals(SILLY, silly2.getName());
	// }
	//
	// @Test
	// public void serialiseSillyBean() throws JDOMException, IOException {
	// SillyBean silly = new SillyBean();
	// silly.setName(SILLY);
	// Element elem = BeanSerialiser.toXML(silly);
	// SillyBean silly2 = (SillyBean) BeanSerialiser.fromXML(elem,
	// SillyBean.class.getClassLoader());
	// assertEquals(SILLY, silly2.getName());
	// }

	@Test
	public void serialiseURLRefScheme() throws JDOMException, IOException,
			JAXBException {
		HttpReferenceScheme urlRef = new HttpReferenceScheme();
		HttpReferenceBean bean = new HttpReferenceBean();
		bean.setUrl("http://taverna.sf.net/");
		urlRef.setFromBean(bean);

		Element elem = beanSerialiser.beanableToXMLElement(urlRef);
		HttpReferenceScheme retrievedUrlRef = (HttpReferenceScheme) beanSerialiser
				.beanableFromXMLElement(elem);
		assertEquals(urlRef.getUrl().toString(), retrievedUrlRef.getUrl()
				.toString());
	}

	@Test
	public void serialiseBlobRefSchemeWithoutCharset() throws JDOMException,
			IOException, DereferenceException, JAXBException {
		BlobReferenceSchemeImpl blobRef = new BlobReferenceSchemeImpl("ns1",
				"an-id");
		assertEquals("ns1", blobRef.getNamespace());
		assertEquals("an-id", blobRef.getId());
		assertNull(blobRef.getCharset());
		Element elem = beanSerialiser.beanableToXMLElement(blobRef);
		BlobReferenceSchemeImpl retrievedBlobRef = (BlobReferenceSchemeImpl) beanSerialiser
				.beanableFromXMLElement(elem);
		assertEquals(blobRef.getNamespace(), retrievedBlobRef.getNamespace());
		assertEquals(blobRef.getId(), retrievedBlobRef.getId());
		assertEquals(blobRef.getCharset(), retrievedBlobRef.getCharset());
	}

	@Test
	public void serialiseBlobRefSchemeWithCharset() throws JDOMException,
			IOException, DereferenceException, JAXBException {
		BlobReferenceSchemeImpl blobRef = new BlobReferenceSchemeImpl("ns1",
				"an-id", "utf-8");
		assertEquals("ns1", blobRef.getNamespace());
		assertEquals("an-id", blobRef.getId());
		assertEquals("utf-8", blobRef.getCharset());
		Element elem = beanSerialiser.beanableToXMLElement(blobRef);
		BlobReferenceSchemeImpl retrievedBlobRef = (BlobReferenceSchemeImpl) beanSerialiser
				.beanableFromXMLElement(elem);
		assertEquals(blobRef.getNamespace(), retrievedBlobRef.getNamespace());
		assertEquals(blobRef.getId(), retrievedBlobRef.getId());
		assertEquals(blobRef.getCharset(), retrievedBlobRef.getCharset());
	}

}
