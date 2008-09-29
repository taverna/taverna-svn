package net.sf.taverna.t2.renderers;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.spi.SPIRegistry;

import org.apache.log4j.Logger;

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
	 * @param context.getReferenceService()
	 * @param entityIdentifier
	 * @param mimeType
	 * @return
	 */
	public List<Renderer> getRenderersForMimeType(InvocationContext context,
			T2Reference reference, String mimeType) {
		ArrayList<Renderer> list = new ArrayList<Renderer>();
		for (Renderer renderer : getInstances()) {
			try {
				if (renderer.canHandle(context.getReferenceService(), reference, mimeType)) {
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
