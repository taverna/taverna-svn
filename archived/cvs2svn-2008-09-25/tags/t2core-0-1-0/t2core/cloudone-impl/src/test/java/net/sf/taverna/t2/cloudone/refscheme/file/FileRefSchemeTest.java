package net.sf.taverna.t2.cloudone.refscheme.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.file.FileReferenceScheme;
import net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

public class FileRefSchemeTest {

	private BeanSerialiser beanSerialiser = BeanSerialiser.getInstance();
	private InMemoryDataManager dManager;

	@Before
	public void makeDataManager() {
		dManager = new InMemoryDataManager();
	}

	@Test
	public void createFileRef() throws Exception {
		File file = File.createTempFile("test", ".tmp");
		FileReferenceScheme fileRef = new FileReferenceScheme(file);
		assertEquals("retrieved file did not match", file, fileRef.getFile());
		assertNull(fileRef.getCharset());
	}

	@Test
	public void createWithCharset() throws Exception {
		File file = File.createTempFile("test", ".tmp");
		FileReferenceScheme fileRef = new FileReferenceScheme(file, "UTF-8");
		assertEquals("retrieved file did not match", file, fileRef.getFile());
		assertEquals("UTF-8", fileRef.getCharset());
	}

	@Test
	public void fromSerialisedBean() throws IOException, InterruptedException,
			DereferenceException, JAXBException {
		File file = File.createTempFile("test", ".tmp");
		FileReferenceScheme fileRef = new FileReferenceScheme(file);
		assertEquals("retrieved file did not match", file, fileRef.getFile());
		Element xml = beanSerialiser.beanableToXMLElement(fileRef);
		FileReferenceScheme newRef = (FileReferenceScheme) beanSerialiser.beanableFromXMLElement(xml);
		assertEquals("reference was not the same after creating from bean",
				newRef, fileRef);
		assertNull(newRef.getCharset());
	}

	@Test
	public void fromSerialisedWithCharset() throws Exception {
		File file = File.createTempFile("test", ".tmp");
		FileReferenceScheme fileRef = new FileReferenceScheme(file, "UTF-8");
		Element xml = beanSerialiser.beanableToXMLElement(fileRef);
		FileReferenceScheme newRef = (FileReferenceScheme) beanSerialiser.beanableFromXMLElement(xml);
		assertEquals("retrieved file did not match", file, newRef.getFile());
		assertEquals("UTF-8", newRef.getCharset());
	}

	@Test
	public void dereference() throws IOException, DereferenceException {
		File file = File.createTempFile("test", ".tmp");
		String TEST_DATA = "The test data\n";
		FileUtils.writeStringToFile(file, TEST_DATA, "UTF-8");
		FileReferenceScheme fileRef = new FileReferenceScheme(file);
		InputStream inStream = fileRef.dereference(dManager);
		assertEquals("Dereferenced data did not match", TEST_DATA, IOUtils
				.toString(inStream, "UTF-8"));
	}

	@Test(expected = DereferenceException.class)
	public void dereferenceNonExistingFails() throws IOException,
			DereferenceException {
		File file = File.createTempFile("test", ".tmp");
		file.delete();
		FileReferenceScheme fileRef = new FileReferenceScheme(file);
		fileRef.dereference(dManager);
	}

	@Test(expected = DereferenceException.class)
	public void dereferenceDirectoryFails() throws IOException,
			DereferenceException {
		File file = File.createTempFile("test", ".tmp");
		file.delete();
		file.mkdir();
		FileReferenceScheme fileRef = new FileReferenceScheme(file);
		fileRef.dereference(dManager);
	}

}
