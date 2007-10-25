package net.sf.taverna.t2.cloudone.util;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import net.sf.taverna.t2.cloudone.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.bean.SillyBean;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.impl.BlobReferenceSchemeImpl;
import net.sf.taverna.t2.cloudone.impl.http.HttpReferenceScheme;
import net.sf.taverna.t2.cloudone.impl.http.HttpReferenceBean;

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

	private static final String SILLY = "I'm silly";

	private ClassLoader classLoader = BeanSerialiserTest.class.getClassLoader();

	InMemoryDataManager dManager = new InMemoryDataManager("dataNS",
			new HashSet<LocationalContext>());

	@Test
	public void serialiseAsFile() throws IOException, JDOMException {
		File file = File.createTempFile("test", ".xml");
		file.deleteOnExit();
		file.delete();
		assertFalse(file.exists());
		SillyBean silly = new SillyBean();
		silly.setName(SILLY);
		BeanSerialiser.toXMLFile(silly, file);
		assertTrue(file.exists());
		// Should be somewhere between 50 (it's XML) and 1024 :-)
		assertTrue("Serialised file too small", file.length() > 50);
		assertTrue("Serialised file too big", file.length() < 1024);
		SillyBean silly2 = (SillyBean) BeanSerialiser.fromXMLFile(file,
				getClass().getClassLoader());
		assertEquals(SILLY, silly2.getName());
	}

	@Test
	public void serialiseSillyBean() throws JDOMException, IOException {
		SillyBean silly = new SillyBean();
		silly.setName(SILLY);
		Element elem = BeanSerialiser.toXML(silly);
		SillyBean silly2 = (SillyBean) BeanSerialiser.fromXML(elem,
				SillyBean.class.getClassLoader());
		assertEquals(SILLY, silly2.getName());
	}

	@Test
	public void serialiseURLRefScheme() throws JDOMException, IOException {
		HttpReferenceScheme urlRef = new HttpReferenceScheme();
		HttpReferenceBean bean = new HttpReferenceBean();
		bean.setUrl("http://taverna.sf.net/");
		urlRef.setFromBean(bean);

		Element elem = BeanSerialiser.beanableToXML(urlRef);
		HttpReferenceScheme retrievedUrlRef = (HttpReferenceScheme) BeanSerialiser
				.beanableFromXML(elem);
		assertEquals(urlRef.getUrl().toString(), retrievedUrlRef.getUrl().toString());
	}
	
	@Test
	public void serialiseBlobRefSchemeWithoutCharset() throws JDOMException, IOException, DereferenceException {
		BlobReferenceSchemeImpl blobRef = new BlobReferenceSchemeImpl("ns1", "an-id");
		assertEquals("ns1", blobRef.getNamespace());
		assertEquals("an-id", blobRef.getId());
		assertNull(blobRef.getCharset());
		Element elem = BeanSerialiser.beanableToXML(blobRef);
		BlobReferenceSchemeImpl retrievedBlobRef = (BlobReferenceSchemeImpl) BeanSerialiser
				.beanableFromXML(elem);
		assertEquals(blobRef.getNamespace(), retrievedBlobRef.getNamespace());
		assertEquals(blobRef.getId(), retrievedBlobRef.getId());
		assertEquals(blobRef.getCharset(), retrievedBlobRef.getCharset());
	}

	@Test
	public void serialiseBlobRefSchemeWithCharset() throws JDOMException, IOException, DereferenceException {
		BlobReferenceSchemeImpl blobRef = new BlobReferenceSchemeImpl("ns1", "an-id", "utf-8");
		assertEquals("ns1", blobRef.getNamespace());
		assertEquals("an-id", blobRef.getId());
		assertEquals("utf-8", blobRef.getCharset());
		Element elem = BeanSerialiser.beanableToXML(blobRef);
		BlobReferenceSchemeImpl retrievedBlobRef = (BlobReferenceSchemeImpl) BeanSerialiser
				.beanableFromXML(elem);
		assertEquals(blobRef.getNamespace(), retrievedBlobRef.getNamespace());
		assertEquals(blobRef.getId(), retrievedBlobRef.getId());
		assertEquals(blobRef.getCharset(), retrievedBlobRef.getCharset());
	}


}
