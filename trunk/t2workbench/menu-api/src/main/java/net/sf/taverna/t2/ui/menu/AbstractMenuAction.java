package net.sf.taverna.t2.ui.menu;

import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * A {@link MenuComponent} of the type
 * {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#action}.
 * <p>
 * Subclass to create an SPI implementation for the {@link MenuManager} of an
 * action. An action is an item within a menu or toolbar that can be
 * clicked/selected to invoke some action.
 * </p>
 * <p>
 * This action can have as an parent a {@link AbstractMenu menu} or
 * {@link AbstractToolBar toolbar}, or grouped within an
 * {@link AbstractMenuSection section} or
 * {@link AbstractMenuOptionGroup option group}.
 * </p>
 * <p>
 * To define the {@link Action}, implement {@link #createAction()}. The action
 * should provide both the label/icon (representation) and
 * {@link java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)}.
 * </p>
 * <p>
 * You need to list the {@link Class#getName() fully qualified class name} (for
 * example <code>com.example.t2plugin.menu.MyMenuAction</code>) of the menu
 * action implementation in the SPI description resource file
 * <code>/META-INF/services/net.sf.taverna.t2.ui.menu.MenuComponent</code> so
 * that it can be discovered by the {@link MenuManager}. This requirement also
 * applies to parent menu components (except {@link DefaultToolBar} and
 * {@link DefaultMenuBar}, but ensure they are only listed once.
 * </p>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class AbstractMenuAction extends AbstractMenuItem {

	/**
	 * Construct a menu action to appear within the specified menu component.
	 * 
	 * @param parentId
	 *            The {@link URI} of the parent menu component. The component
	 *            should be a {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#isParentType() parent type} and
	 *            must have been registered separately as an SPI.
	 * @param positionHint
	 *            The position hint to determine the position of this action
	 *            among its siblings in the parent menu, section or toolbar. For
	 *            extensibility, use BASIC style numbering such as 10, 20, etc.
	 *            (Note that position hints are local to each parent, so each
	 *            {@link AbstractMenuSection section} have their own position
	 *            hint scheme.)
	 */
	public AbstractMenuAction(URI parentId, int positionHint) {
		this(parentId, positionHint, null);
	}

	/**
	 * Construct a menu action to appear within the specified menu component.
	 * 
	 * @param parentId
	 *            The {@link URI} of the parent menu component. The component
	 *            should be a {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#isParentType() parent type} and
	 *            must have been registered separately as an SPI.
	 * @param positionHint
	 *            The position hint to determine the position of this action
	 *            among its siblings in the parent menu, section or toolbar. For
	 *            extensibility, use BASIC style numbering such as 10, 20, etc.
	 *            (Note that position hints are local to each parent, so each
	 *            {@link AbstractMenuSection section} have their own position
	 *            hint scheme.)
	 * @param id
	 *            The {@link URI} to identify this action. Although no
	 *            components can have an action as their parent, this URI can be
	 *            used to retrieve the realisation of this component using
	 *            {@link MenuManager#getComponentByURI(URI)}. This ID might
	 *            also be registered as a help identifier with the help system.
	 */
	public AbstractMenuAction(URI parentId, int positionHint, URI id) {
		super(MenuType.action, parentId, id);
		this.positionHint = positionHint;
	}

	/**
	 * Call {@link #createAction()} on first call, after that return cached
	 * action.
	 * 
	 * @see #createAction()
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Action getAction() {
		if (action == null) {
			action = createAction();
		}
		return action;
	}

	/**
	 * Create the {@link Action} that labels this menu action, in addition to
	 * performing the desired action on
	 * {@link java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)}.
	 * <p>
	 * Implementations might use {@link AbstractAction} as a superclass for menu
	 * actions. It is recommended to make the action a top level class so that
	 * it can be used both within an {@link AbstractMenuAction} of a menu bar
	 * and within an {@link AbstractMenuAction} of a tool bar.
	 * </p>
	 * 
	 * @return A configured {@link Action} that should at least have a label or
	 *         icon.
	 */
	protected abstract Action createAction();
}
