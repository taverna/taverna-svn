package net.sf.taverna.t2.renderers;

import javax.swing.JComponent;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

public interface Renderer {

	public boolean canHandle(String mimeType);

	public boolean canHandle(DataFacade facade,
			EntityIdentifier entityIdentifier, String mimeType) throws RendererException;

	public JComponent getComponent(EntityIdentifier entityIdentifier,
			DataFacade dataFacade) throws RendererException;
	
	public String getType();

}
