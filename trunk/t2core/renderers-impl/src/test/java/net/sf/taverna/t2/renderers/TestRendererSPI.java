package net.sf.taverna.t2.renderers;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.List;

import net.sf.taverna.t2.renderers.RendererRegistry;

import org.junit.Test;


public class TestRendererSPI {
	
	@Test
	public void getAllRenderers() {
		RendererRegistry rendererRegistry = new RendererRegistry();
		assertEquals(rendererRegistry.getInstances().size(), 7);
	}
	
	@Test
	public void checkTextHtmlMimeType() {
		String mimeType = "text/html";
		RendererRegistry rendererRegistry = new RendererRegistry();
		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(mimeType);
		assertEquals(renderersForMimeType.size(),2);
		assertEquals(renderersForMimeType.get(0).getClass().getSimpleName(), "TextRenderer");
		assertEquals(renderersForMimeType.get(1).getClass().getSimpleName(), "TextHtmlRenderer");
		assertTrue(renderersForMimeType.get(0).canHandle("text/html"));
	}
	
	@Test
	public void checkURLMimeType() {
		String mimeType = "text/x-taverna-web-url.text";
		RendererRegistry rendererRegistry = new RendererRegistry();
		List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType("text/x-taverna-web-url.text");
		assertEquals(renderersForMimeType.size(),2);
		assertEquals(renderersForMimeType.get(0).getClass().getSimpleName(), "TextRenderer");
		assertEquals(renderersForMimeType.get(1).getClass().getSimpleName(), "TextTavernaWebUrlRenderer");
		assertTrue(renderersForMimeType.get(1).canHandle("text/x-taverna-web-url.text"));
	}

}
