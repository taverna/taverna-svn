package net.sf.taverna.t2.renderers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.spi.SPIRegistry;

public class RendererRegistry extends SPIRegistry<Renderer> {

	private static Logger logger = Logger.getLogger(RendererRegistry.class);

	public RendererRegistry() {
		super(Renderer.class);
	}

	/**
	 * Get all of the available renderers for a specific MIME type. If there is
	 * a problem with one then catch the exception and log the problem but carry
	 * on since there is probably more than one way to render the data
	 * 
	 * @param dataFacade
	 * @param entityIdentifier
	 * @param mimeType
	 * @return
	 */
	public List<Renderer> getRenderersForMimeType(DataFacade dataFacade,
			EntityIdentifier entityIdentifier, String mimeType) {
		ArrayList<Renderer> list = new ArrayList<Renderer>();
		for (Renderer renderer : getInstances()) {
			try {
				if (renderer.canHandle(dataFacade, entityIdentifier, mimeType)) {
					list.add(renderer);
				}
			} catch (RendererException e) {
				logger.warn("Problem with renderer for " + renderer.getType(),
						e);
			}
		}
		return list;
	}

}
