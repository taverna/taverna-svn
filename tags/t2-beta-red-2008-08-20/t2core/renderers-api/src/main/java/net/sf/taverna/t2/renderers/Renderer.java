package net.sf.taverna.t2.renderers;

import javax.swing.JComponent;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

public interface Renderer {

	public boolean canHandle(String mimeType);

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException;

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException;
	
	public String getType();

}
