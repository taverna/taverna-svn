package net.sf.taverna.t2.renderers;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.EmptyListException;
import net.sf.taverna.t2.cloudone.datamanager.MalformedListException;
import net.sf.taverna.t2.cloudone.datamanager.UnsupportedObjectTypeException;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.renderers.RendererRegistry;

import org.junit.Before;
import org.junit.Test;


public class TestRendererSPI {
	
	private static final String TEST_NS = "testNS";
	private DataManager dManager;
	private DataFacade facade;
	
	@Test
	public void getAllRenderers() {
		RendererRegistry rendererRegistry = new RendererRegistry();
		assertEquals(rendererRegistry.getInstances().size(), 7);
	}
	
	@Test
	public void checkTextHtmlMimeType() throws EmptyListException, MalformedListException, UnsupportedObjectTypeException {
		String mimeType = "text/html";
		String html = "<HTML><HEAD></HEAD><BODY>hello</BODY></HTML>";
		EntityIdentifier entityIdentifier = facade.register(html, "utf-8");
		RendererRegistry rendererRegistry = new RendererRegistry();
		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(facade, entityIdentifier, mimeType);
		assertEquals(renderersForMimeType.size(),2);
		assertEquals(renderersForMimeType.get(0).getClass().getSimpleName(), "TextRenderer");
		assertEquals(renderersForMimeType.get(1).getClass().getSimpleName(), "TextHtmlRenderer");
		assertTrue(renderersForMimeType.get(0).canHandle("text/html"));
	}
	
	@Test
	public void checkURLMimeType() throws EmptyListException, MalformedListException, UnsupportedObjectTypeException {
		String mimeType = "text/x-taverna-web-url.text";
		String url = "http://google.com";
		EntityIdentifier entityIdentifier = facade.register(url);
		RendererRegistry rendererRegistry = new RendererRegistry();
		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(facade, entityIdentifier, mimeType);
		assertEquals(renderersForMimeType.size(),2);
		assertEquals(renderersForMimeType.get(0).getClass().getSimpleName(), "TextRenderer");
		assertEquals(renderersForMimeType.get(1).getClass().getSimpleName(), "TextTavernaWebUrlRenderer");
		assertTrue(renderersForMimeType.get(1).canHandle("text/x-taverna-web-url.text"));
	}
	
	@Before
	public void setDataManager() {
		// dManager = new FileDataManager("testNS",
		// new HashSet<LocationalContext>(), new File("/tmp/fish"));
		dManager = new InMemoryDataManager(TEST_NS,
				new HashSet<LocationalContext>());
		facade = new DataFacade(dManager);
	}

}
