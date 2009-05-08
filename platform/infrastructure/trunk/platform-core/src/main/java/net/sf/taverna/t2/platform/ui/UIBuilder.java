package net.sf.taverna.t2.platform.ui;

import javax.swing.JPanel;

/**
 * Static methods to build bean editor UIs through reflection and (minimal)
 * configuration.
 * <p>
 * <strong>Renamed</strong> to
 * {@link net.sf.taverna.t2.lang.uibuilder.UIBuilder}
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * @see net.sf.taverna.t2.lang.uibuilder.UIBuilder
 * 
 */
@Deprecated
public abstract class UIBuilder {

	/**
	 * @see net.sf.taverna.t2.lang.uibuilder.UIBuilder#buildEditor(Object,
	 *      String)
	 */
	@Deprecated
	public static JPanel buildEditor(Object target, String configuration)
			throws UIConstructionException {
		try {
			return net.sf.taverna.t2.lang.uibuilder.UIBuilder.buildEditor(
					target, configuration);
		} catch (net.sf.taverna.t2.lang.uibuilder.UIConstructionException ex) {
			throw new UIConstructionException(ex);
		}
	}

	/**
	 * 
	 * @see net.sf.taverna.t2.lang.uibuilder.UIBuilder#buildEditor(Object,
	 *      String[])
	 */
	@Deprecated
	public static JPanel buildEditor(Object target, String[] fields)
			throws UIConstructionException {
		try {

			return net.sf.taverna.t2.lang.uibuilder.UIBuilder.buildEditor(
					target, fields);
		} catch (net.sf.taverna.t2.lang.uibuilder.UIConstructionException ex) {
			throw new UIConstructionException(ex);
		}
	}
}
