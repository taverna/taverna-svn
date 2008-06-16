package net.sf.taverna.t2.ui.menu;

import java.net.URI;

/**
 * The default tool bar that will be shown by the main application window. Use
 * {@link #DEFAULT_TOOL_BAR} as the {@link #getParentId()} for items that should
 * appear in this toolbar. This toolbar can be created using
 * {@link MenuManager#createToolBar()}
 * <p>
 * Separate toolbars can be made by subclassing {@link AbstractToolBar} and
 * created by using {@link MenuManager#createToolBar(URI)}.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class DefaultToolBar extends AbstractToolBar {

	/**
	 * The URI of a tool bar item representing the default tool bar. Items who
	 * has this URI as their {@link #getParentId()} will be shown in the default
	 * toolbar of the main application window.
	 */
	public static final URI DEFAULT_TOOL_BAR = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#defaultToolBar");

	/**
	 * Construct the default toolbar.
	 * 
	 */
	public DefaultToolBar() {
		super(DEFAULT_TOOL_BAR);
	}

}
