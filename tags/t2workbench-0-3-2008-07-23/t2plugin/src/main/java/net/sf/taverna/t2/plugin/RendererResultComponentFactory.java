package net.sf.taverna.t2.plugin;

import javax.swing.JPanel;

/**
 * Get an instance of this factory and the use {@link #getRendererComponent()}
 * to fill the actual renderer with a new result
 * 
 * @author Ian Dunlop
 * 
 */
public class RendererResultComponentFactory {

	private static RendererResultComponentFactory instance;
	private RenderedResultComponent component;
	
	private RendererResultComponentFactory() {
		
	}

	/**
	 * Get a {@link RenderedResultComponent}
	 * 
	 * @return a {@link JPanel} to put a rendered workflow result inside
	 */
	public RenderedResultComponent getRendererComponent() {
		if (component == null) {
			component = new RenderedResultComponent();
		}
		return component;
	}
/**
 * Get an instance of this factory
 * @return
 */
	public static RendererResultComponentFactory getInstance() {
		if (instance == null) {
			instance = new RendererResultComponentFactory();
		}
		return instance;
	}

}
