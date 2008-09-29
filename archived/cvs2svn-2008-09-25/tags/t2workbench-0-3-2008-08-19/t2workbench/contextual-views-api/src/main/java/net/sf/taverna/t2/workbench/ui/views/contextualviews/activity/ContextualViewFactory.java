package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

/**
 * Defines a factory class that when associated with a selected object creates a
 * {@link ContextualView} for that selection.
 * <p>
 * This factory acts as an SPI to find a {@link ContextualView}s for a given
 * Activity and other workflow components.
 * </p>
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * @author Stian Soiland-Reyes
 * 
 * 
 * @param <SelectionType>
 *            - the selection type this factory is associated with
 * 
 * @see ContextualView
 * @see ContextualViewFactoryRegistry
 */
public interface ContextualViewFactory<SelectionType> {

	/**
	 * @param selection
	 *            - the object for which a ContextualView needs to be generated
	 * @return an instance of an {@link ContextualView}
	 */
	public ContextualView getView(SelectionType selection);

	/**
	 * Used by the SPI system to find the correct factory that can handle the
	 * given object type. 
	 * 
	 * @param selection
	 * @return true if this factory relates to the given selection type
	 * @see ContextualViewFactoryRegistry
	 */
	public boolean canHandle(Object selection);

}