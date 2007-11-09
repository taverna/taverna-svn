package net.sf.taverna.t2.cloudone.datamanager.refscheme.file;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import net.sf.taverna.t2.cloudone.refscheme.file.FileReferenceScheme;
import net.sf.taverna.t2.cloudone.util.BeanSerialiser;

import org.jdom.Element;
import org.junit.Test;

public class FileRefSchemeTest {

	@Test
	public void createFileRef() throws Exception {
		File file = File.createTempFile("test", ".tmp");
		FileReferenceScheme fileRef = new FileReferenceScheme(file);
		assertEquals("retrieved file did not match", file, fileRef.getFile());
	}

	@Test
	public void fromSerialisedBean() throws IOException, InterruptedException {
		File file = File.createTempFile("test", ".tmp");
		FileReferenceScheme fileRef = new FileReferenceScheme(file);
		assertEquals("retrieved file did not match", file, fileRef.getFile());
		Element xml = BeanSerialiser.beanableToXML(fileRef);
		FileReferenceScheme newRef = (FileReferenceScheme) BeanSerialiser
				.beanableFromXML(xml);
		assertEquals("reference was not the same after creating from bean",
				newRef, fileRef);
	}

}
