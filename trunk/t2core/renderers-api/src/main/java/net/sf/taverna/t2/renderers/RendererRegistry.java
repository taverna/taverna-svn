package net.sf.taverna.t2.renderers;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.spi.SPIRegistry;

public class RendererRegistry extends SPIRegistry<Renderer>{
	
	public RendererRegistry() {
		super(Renderer.class);
	}
	
	public List<Renderer> getRenderersForMimeType(DataFacade dataFacade, EntityIdentifier entityIdentifier, String mimeType) {
		ArrayList<Renderer> list = new ArrayList<Renderer>();
		for (Renderer renderer: getInstances()) {
			if (renderer.canHandle(dataFacade, entityIdentifier, mimeType)) {
				list.add(renderer);
			}
		}
		return list;
	}

}
