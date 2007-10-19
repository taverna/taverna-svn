package net.sf.taverna.t2.cloudone.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DataPeer;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceScheme;

import org.junit.Before;
import org.junit.Test;

public class TranslatorRegistrySPITest {

	public class MyReferenceScheme implements ReferenceScheme {
		public InputStream dereference(DataManager manager)
				throws DereferenceException {
			throw new DereferenceException();
		}

		public Date getExpiry() {
			return null;
		}

		public boolean isImmediate() {
			return true;
		}

		public boolean validInContext(Set<LocationalContext> contextSet,
				DataPeer currentLocation) {
			return true;
		}
	}

	URLReferenceScheme urlRef;

	@Before
	public void createUrlRef() throws MalformedURLException {
		urlRef = new URLReferenceScheme(new URL("http://localhost/test.txt"));
	}

	TranslatorRegistry registry = TranslatorRegistry.getInstance();

	@Test
	public void getInstances() {
		List<Translator> instances = registry.getInstances();
		assertEquals(2, instances.size());
		assertTrue(instances.get(0) instanceof AnyToBlobTranslator);
		assertTrue(instances.get(1) instanceof AnyToFileURLTranslator);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getBlobTranslators() {
		List<Translator<BlobReferenceScheme>> blobTranslators = registry
				.getTranslators(urlRef, BlobReferenceScheme.class);
		assertEquals(1, blobTranslators.size());
		assertTrue(blobTranslators.get(0) instanceof AnyToBlobTranslator);
	}

	@Test
	public void getURLTranslators() {
		List<Translator<URLReferenceScheme>> urlTranslators = registry
				.getTranslators(urlRef, URLReferenceScheme.class);
		assertEquals(1, urlTranslators.size());
		assertTrue(urlTranslators.get(0) instanceof AnyToFileURLTranslator);
	}

	@Test
	public void getNoTranslators() {
		List<Translator<MyReferenceScheme>> urlTranslators = registry
				.getTranslators(urlRef, MyReferenceScheme.class);
		assertEquals(0, urlTranslators.size());
	}

}
