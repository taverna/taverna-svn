package net.sf.taverna.t2.ui.menu.impl;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.spi.SPIRegistry.SPIRegistryEvent;
import net.sf.taverna.t2.ui.menu.AbstractMenu;
import net.sf.taverna.t2.ui.menu.AbstractToolBar;
import net.sf.taverna.t2.ui.menu.DefaultMenuBar;
import net.sf.taverna.t2.ui.menu.DefaultToolBar;
import net.sf.taverna.t2.ui.menu.MenuComponent;
import net.sf.taverna.t2.ui.menu.MenuComponent.MenuType;
import net.sf.taverna.t2.ui.menu.impl.MenuManager.MenuManagerEvent;

/**
 * Create {@link JMenuBar}s and {@link JToolBar}s based on SPI instances of
 * {@link MenuComponent}.
 * <p>
 * Elements of menus are discovered automatically using an {@link SPIRegistry}.
 * The elements specify their internal relationship through
 * {@link MenuComponent#getParentId()} and
 * {@link MenuComponent#getPositionHint()}. {@link MenuComponent#getType()}
 * specifies how the component is to be rendered or grouped.
 * </p>
 * <p>
 * The menu manager is {@link Observable}, you can
 * {@link #addObserver(Observer) add an observer} to be notified when the menus
 * have changed, i.e. when {@link #update()} has been called, for instance when
 * the {@link SPIRegistry} (which the menu manager observes) has been updated
 * due to a plugin installation.
 * </p>
 * <p>
 * {@link #createMenuBar()} creates the default menu bar, ie. the menu bar
 * containing all the items with {@link DefaultMenuBar#DEFAULT_MENU_BAR} as
 * their parent. Alternate menu bars can be created using
 * {@link #createMenuBar(URI)}.
 * </p>
 * <p>
 * Similary {@link #createToolBar()} creates the default tool bar, containing
 * the items that has {@link DefaultToolBar#DEFAULT_TOOL_BAR} as their parent.
 * Alternate toolbars can be created using {@link #createToolBar(URI)}.
 * </p>
 * <p>
 * The menu manager keeps weak references to the created (published) menu bars
 * and tool bars, and will attempt to update them when {@link #update()} is
 * called.
 * </p>
 * <p>
 * See the package level documentation for {@link net.sf.taverna.t2.ui.menu}
 * more information about how to specify menu elements.
 * </p>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class MenuManager implements Observable<MenuManagerEvent> {

	private static MenuManager instance;

	private static Logger logger = Logger.getLogger(MenuManager.class);

	/**
	 * Get {@link MenuManager} singleton.
	 * 
	 * @return {@link MenuManager} singleton
	 */
	public static synchronized MenuManager getInstance() {
		if (instance == null) {
			instance = new MenuManager();
		}
		return instance;
	}

	private final Object updateLock = new Object();
	
	private WeakHashMap<Component, URI> componentToUri;

	private MenuElementComparator menuElementComparator = new MenuElementComparator();

	private HashMap<URI, List<MenuComponent>> menuElementTree;

	private SPIRegistry<MenuComponent> menuRegistry = new SPIRegistry<MenuComponent>(
			MenuComponent.class);

	private MultiCaster<MenuManagerEvent> multiCaster;

	/**
	 * True if {@link #doUpdate()} is running, subsequents call to
	 * {@link #update()} will return immediately.
	 */
	private boolean updating;

	private Map<URI, WeakReference<Component>> uriToComponent;

	private Map<URI, MenuComponent> uriToMenuElement;

	// Note: Not reset by #resetCollections()
	private Map<URI, List<WeakReference<Component>>> uriToPublishedComponents = new HashMap<URI, List<WeakReference<Component>>>();

	/**
	 * Protected constructor, use singleton method {@link #getInstance()}
	 * instead.
	 * 
	 */
	protected MenuManager() {
		menuRegistry.addObserver(new MenuRegistryObserver());
		multiCaster = new MultiCaster<MenuManagerEvent>(this);
		update();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addObserver(Observer<MenuManagerEvent> observer) {
		multiCaster.addObserver(observer);
	}

	/**
	 * Create the {@link JMenuBar} containing menu elements defining
	 * {@link DefaultMenuBar#DEFAULT_MENU_BAR} as their
	 * {@link MenuComponent#getParentId() parent}.
	 * <p>
	 * A {@link WeakReference weak reference} is kept in the menu manager to
	 * {@link #populateMenuBar(JMenuBar, URI) update the menubar} if
	 * {@link #update()} is called (manually or automatically when the SPI is
	 * updated).
	 * </p>
	 * 
	 * @return A {@link JMenuBar} populated with the items belonging to the
	 *         default menu bar
	 */
	public JMenuBar createMenuBar() {
		return createMenuBar(DefaultMenuBar.DEFAULT_MENU_BAR);
	}

	/**
	 * Create the {@link JMenuBar} containing menu elements defining the given
	 * <code>id</code> as their {@link MenuComponent#getParentId() parent}.
	 * <p>
	 * Note that the parent itself also needs to exist as a registered SPI
	 * instance og {@link MenuComponent#getType()} equal to
	 * {@link MenuType#menu}, for instance by subclassing {@link AbstractMenu}.
	 * </p>
	 * <p>
	 * A {@link WeakReference weak reference} is kept in the menu manager to
	 * {@link #populateMenuBar(JMenuBar, URI) update the menubar} if
	 * {@link #update()} is called (manually or automatically when the SPI is
	 * updated).
	 * </p>
	 * 
	 * @param id
	 *            The {@link URI} identifying the menu bar
	 * @return A {@link JMenuBar} populated with the items belonging to the
	 *         given parent id.
	 */
	public JMenuBar createMenuBar(URI id) {
		JMenuBar menuBar = new JMenuBar();
		populateMenuBar(menuBar, id);
		registerComponent(id, menuBar, true);
		return menuBar;
	}

	/**
	 * Create the {@link JToolBar} containing elements defining
	 * {@link DefaultToolBar#DEFAULT_TOOL_BAR} as their
	 * {@link MenuComponent#getParentId() parent}.
	 * <p>
	 * A {@link WeakReference weak reference} is kept in the menu manager to
	 * {@link #populateToolBar(JToolBar, URI) update the toolbar} if
	 * {@link #update()} is called (manually or automatically when the SPI is
	 * updated).
	 * </p>
	 * 
	 * @return A {@link JToolBar} populated with the items belonging to the
	 *         default tool bar
	 */
	public JToolBar createToolBar() {
		return createToolBar(DefaultToolBar.DEFAULT_TOOL_BAR);
	}

	/**
	 * Create the {@link JToolBar} containing menu elements defining the given
	 * <code>id</code> as their {@link MenuComponent#getParentId() parent}.
	 * <p>
	 * Note that the parent itself also needs to exist as a registered SPI
	 * instance og {@link MenuComponent#getType()} equal to
	 * {@link MenuType#toolBar}, for instance by subclassing
	 * {@link AbstractToolBar}.
	 * </p>
	 * <p>
	 * A {@link WeakReference weak reference} is kept in the menu manager to
	 * {@link #populateToolBar(JToolBar, URI) update the toolbar} if
	 * {@link #update()} is called (manually or automatically when the SPI is
	 * updated).
	 * </p>
	 * 
	 * @param id
	 *            The {@link URI} identifying the tool bar
	 * @return A {@link JToolBar} populated with the items belonging to the
	 *         given parent id.
	 */
	public JToolBar createToolBar(URI id) {
		JToolBar toolbar = new JToolBar();
		populateToolBar(toolbar, id);
		registerComponent(id, toolbar, true);
		return toolbar;
	}

	/**
	 * Get a menu item identified by the given URI.
	 * <p>
	 * Return the UI {@link Component} last created for a {@link MenuComponent},
	 * through {@link #createMenuBar()}, {@link #createMenuBar(URI)},
	 * {@link #createToolBar()} or {@link #createToolBar(URI)}.
	 * </p>
	 * <p>
	 * For instance, if {@link #createMenuBar()} created a menu bar containing a
	 * "File" menu with {@link MenuComponent#getId()} ==
	 * <code>http://example.com/menu#file</code>, calling:
	 * </p>
	 * 
	 * <pre>
	 * Component fileMenu = getComponentByURI(URI
	 * 		.create(&quot;http://example.com/menu#file&quot;));
	 * </pre>
	 * 
	 * <p>
	 * would return the {@link JMenu} last created for "File". Note that "last
	 * created" could mean both the last call to {@link #createMenuBar()} and
	 * last call to {@link #update()} - which could have happened because the
	 * SPI registry was updated. To be notified when
	 * {@link #getComponentByURI(URI)} might return a new Component because the
	 * menues have been reconstructed,
	 * {@link #addObserver(Observer) add an observer} to the MenuManager.
	 * </p>
	 * <p>
	 * If the URI is unknown, has not yet been rendered as a {@link Component},
	 * or the Component is no longer in use outside the menu manager's
	 * {@link WeakReference weak references}, <code>null</code> is returned
	 * instead.
	 * </p>
	 * 
	 * @see #getURIByComponent(Component)
	 * @param id
	 *            {@link URI} of menu item as returned by
	 *            {@link MenuComponent#getId()}
	 * @return {@link Component} as previously generated by
	 *         {@link #createMenuBar()}/{@link #createToolBar()}, or
	 *         <code>null</code> if the URI is unknown, or if the
	 *         {@link Component} no longer exists.
	 */
	public synchronized Component getComponentByURI(URI id) {
		WeakReference<Component> componentRef = uriToComponent.get(id);
		if (componentRef == null) {
			return null;
		}
		// Might also be null it reference has gone dead
		return componentRef.get();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Observer<MenuManagerEvent>> getObservers() {
		return multiCaster.getObservers();
	}

	/**
	 * Get the URI of the {@link MenuComponent} this menu/toolbar
	 * {@link Component} was created from.
	 * <p>
	 * If the component was created by the MenuManager, through
	 * {@link #createMenuBar()}, {@link #createMenuBar(URI)},
	 * {@link #createToolBar()} or {@link #createToolBar(URI)}, the URI
	 * identifying the defining {@link MenuComponent} is returned. This will be
	 * the same URI as returned by {@link MenuComponent#getId()}.
	 * </p>
	 * <p>
	 * Note that if {@link #update()} has been invoked, the {@link MenuManager}
	 * might have rebuilt the menu structure and replaced the components since
	 * the given <code>component</code> was created. The newest
	 * {@link Component} for the given URI can be retrieved using
	 * {@link #getComponentByURI(URI)}.
	 * </p>
	 * <p>
	 * If the component is unknown, <code>null</code> is returned instead.
	 * </p>
	 * 
	 * @see #getComponentByURI(URI)
	 * @param component
	 *            {@link Component} that was previously created by the
	 *            {@link MenuManager}
	 * @return {@link URI} identifying the menu component, as returned by
	 *         {@link MenuComponent#getId()}, or <code>null</code> if the
	 *         component is unknown.
	 */
	public synchronized URI getURIByComponent(Component component) {
		return componentToUri.get(component);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeObserver(Observer<MenuManagerEvent> observer) {
		multiCaster.removeObserver(observer);
	}

	/**
	 * Update and rebuild the menu structure.
	 * <p>
	 * Rebuild menu structure as defined by the {@link MenuComponent}s
	 * retrieved from the MenuComponent {@link SPIRegistry}.
	 * </p>
	 * <p>
	 * Rebuilds previously published menubars and toolbars created with
	 * {@link #createMenuBar()}, {@link #createMenuBar(URI)},
	 * {@link #createToolBar()} and {@link #createToolBar(URI)}. Note that the
	 * rebuild will do a removeAll() on the menubar/toolbar, so all components
	 * will be reconstructed. You can use {@link #getComponentByURI(URI)} to
	 * look up individual components within the menu and toolbars.
	 * </p>
	 * <p>
	 * Note that the menu manager is observing the {@link SPIRegistry}, so if a
	 * plugin gets installed and the SPI registry is updated, this update method
	 * will be called by the SPI registry observer {@link MenuRegistryObserver}.
	 * </p>
	 * <p>
	 * If there are several concurrent calls to {@link #update()}, the calls
	 * from the other thread will return immediately, while the first thread to
	 * get the synchronization lock on the menu manager will do the actual
	 * update. If you want to ensure that {@link #update()} does not return
	 * before the update has been performed fully, synchronize on the menu
	 * manager:
	 * </p>
	 * 
	 * <pre>
	 * MenuManager menuManager = MenuManager.getInstance();
	 * synchronized (menuManager) {
	 * 	menuManager.update();
	 * }
	 * doSomethingAfterUpdateFinished();
	 * </pre>
	 */
	public void update() {
		synchronized (updateLock) {
			if (updating) {
				return;
			}
			updating = true;
		}
		try {
			doUpdate();
		} finally {
			synchronized (updateLock) {
				updating = false;
			}
		}
	}

	private void addMenu(List<Component> components,
			MenuComponent menuComponent, boolean isToolbar) {
		URI menuId = menuComponent.getId();
		if (isToolbar) {
			logger.warn("Can't have menu " + menuComponent
					+ " within toolBar element");
			return;
		}
		JMenu menu = new JMenu(menuComponent.getAction());
		for (Component menuItem : makeComponents(menuId, false, false)) {
			if (menuItem == null) {
				menu.addSeparator();
			} else {
				menu.add(menuItem);
			}
		}
		registerComponent(menuId, menu);
		components.add(menu);
	}

	private void addNullSeparator(List<Component> components) {
		if (components.isEmpty()) {
			// Don't start with a separator
			return;
		}
		if (components.get(components.size() - 1) == null) {
			// Already a separator in last position
			return;
		}
		components.add(null);
	}

	private void addOptionGroup(List<Component> components, URI childId,
			boolean isToolbar) {
		ButtonGroup buttonGroup = new ButtonGroup();
		List<Component> buttons = makeComponents(childId, isToolbar, true);
		addNullSeparator(components);
		for (Component button : buttons) {
			if (button instanceof AbstractButton) {
				buttonGroup.add((AbstractButton) button);
			} else {
				logger.warn("Component of button group " + childId
						+ " is not an AbstractButton: " + button);
			}
			if (button == null) {
				logger.warn("Separator found within button group");
				addNullSeparator(components);
			} else {
				components.add(button);
			}
		}
		addNullSeparator(components);
	}

	private void addSection(List<Component> components, URI childId,
			boolean isToolbar, boolean isOptionGroup) {
		List<Component> childComponents = makeComponents(childId, isToolbar,
				isOptionGroup);
		addNullSeparator(components);
		for (Component childComponent : childComponents) {
			if (childComponent == null) {
				logger.warn("Separator found within section");
				addNullSeparator(components);
			} else {
				components.add(childComponent);
			}
		}
		addNullSeparator(components);
	}

	protected synchronized void doUpdate() {
		resetCollections();
		findChildren();
		updatePublishedComponents();
		multiCaster.notify(new UpdatedMenuManagerEvent());
	}

	protected void findChildren() {
		for (MenuComponent menuElement : menuRegistry.getInstances()) {
			uriToMenuElement.put(menuElement.getId(), menuElement);
			logger.debug("Found menu element " + menuElement.getId() + " "
					+ menuElement);
			if (menuElement.getParentId() == null) {
				continue;
			}
			List<MenuComponent> siblings = menuElementTree.get(menuElement
					.getParentId());
			if (siblings == null) {
				siblings = new ArrayList<MenuComponent>();
				menuElementTree.put(menuElement.getParentId(), siblings);
			}
			siblings.add(menuElement);
		}
		if (uriToMenuElement.isEmpty()) {
			logger.error("No menu elements found, check classpath/Raven/SPI");
		}
	}

	protected List<MenuComponent> getChildren(URI id) {
		List<MenuComponent> children = menuElementTree.get(id);
		if (children == null) {
			return Collections.<MenuComponent> emptyList();
		}
		Collections.sort(children, menuElementComparator);
		return children;
	}

	protected List<Component> makeComponents(URI id, boolean isToolbar,
			boolean isOptionGroup) {
		List<Component> components = new ArrayList<Component>();
		for (MenuComponent childElement : getChildren(id)) {
			MenuType type = childElement.getType();
			Action action = childElement.getAction();
			URI childId = childElement.getId();
			if (type.equals(MenuType.action)) {
				if (action == null) {
					logger.warn("Skipping invalid action " + childId + " for "
							+ id);
					continue;
				}
				Component actionComponent;
				if (isOptionGroup) {
					if (isToolbar) {
						actionComponent = new JToggleButton(action);
					} else {
						actionComponent = new JRadioButtonMenuItem(action);
					}
				} else {
					if (isToolbar) {
						actionComponent = new JButton(action);
					} else {
						actionComponent = new JMenuItem(action);
					}
				}
				registerComponent(childId, actionComponent);
				components.add(actionComponent);
			} else if (type.equals(MenuType.toggle)) {
				if (action == null) {
					logger.warn("Skipping invalid toggle " + childId + " for "
							+ id);
					continue;
				}
				Component toggleComponent;
				if (isToolbar) {
					toggleComponent = new JToggleButton(action);
				} else {
					toggleComponent = new JCheckBoxMenuItem(action);
				}
				registerComponent(childId, toggleComponent);
				components.add(toggleComponent);
			} else if (type.equals(MenuType.custom)) {
				Component customComponent = childElement.getCustomComponent();
				if (customComponent == null) {
					logger.warn("Skipping null custom component " + childId
							+ " for " + id);
					continue;
				}
				registerComponent(childId, customComponent);
				components.add(customComponent);
			} else if (type.equals(MenuType.optionGroup)) {
				addOptionGroup(components, childId, isToolbar);
			} else if (type.equals(MenuType.section)) {
				addSection(components, childId, isToolbar, isOptionGroup);
			} else if (type.equals(MenuType.menu)) {
				addMenu(components, childElement, isToolbar);
			} else {
				logger.warn("Skipping invalid/unknown type " + type + " for "
						+ id);
				continue;
			}
		}
		stripTrailingNullSeparator(components);
		return components;
	}

	private void stripTrailingNullSeparator(List<Component> components) {
		if (!components.isEmpty()) {
			int lastIndex = components.size() - 1;
			if (components.get(lastIndex) == null) {
				components.remove(lastIndex);
			}
		}
	}

	protected void populateMenuBar(JMenuBar menuBar, URI id) {
		menuBar.removeAll();
		MenuComponent menuDef = uriToMenuElement.get(id);
		if (menuDef == null) {
			throw new IllegalArgumentException("Unknown menuBar " + id);
		}
		if (!menuDef.getType().equals(MenuType.menu)) {
			throw new IllegalArgumentException("Element " + id
					+ " is not a menu, but a " + menuDef.getType());
		}
		for (Component component : makeComponents(id, false, false)) {
			if (component == null) {
				logger.warn("Ignoring separator in menu bar " + id);
			} else {
				menuBar.add(component);
			}
		}
	}

	protected void populateToolBar(JToolBar toolbar, URI id) {
		toolbar.removeAll();
		MenuComponent toolbarDef = uriToMenuElement.get(id);
		if (toolbarDef == null) {
			throw new IllegalArgumentException("Unknown toolBar " + id);
		}
		if (!toolbarDef.getType().equals(MenuType.toolBar)) {
			throw new IllegalArgumentException("Element " + id
					+ " is not a toolBar, but a " + toolbarDef.getType());
		}
		if (toolbarDef.getAction() != null) {
			String name = (String) toolbarDef.getAction().getValue(Action.NAME);
			toolbar.setName(name);
		} else {
			toolbar.setName("");
		}
		for (Component component : makeComponents(id, true, false)) {
			if (component == null) {
				toolbar.addSeparator();
			} else {
				toolbar.add(component);
			}
		}
	}

	protected synchronized void registerComponent(URI id, Component component) {
		registerComponent(id, component, false);
	}

	protected synchronized void registerComponent(URI id, Component component,
			boolean published) {
		uriToComponent.put(id, new WeakReference<Component>(component));
		componentToUri.put(component, id);
		if (published) {
			List<WeakReference<Component>> publishedComponents = uriToPublishedComponents
					.get(id);
			if (publishedComponents == null) {
				publishedComponents = new ArrayList<WeakReference<Component>>();
				uriToPublishedComponents.put(id, publishedComponents);
			}
			publishedComponents.add(new WeakReference<Component>(component));

		}
	}

	protected synchronized void resetCollections() {
		menuElementTree = new HashMap<URI, List<MenuComponent>>();
		componentToUri = new WeakHashMap<Component, URI>();
		uriToMenuElement = new HashMap<URI, MenuComponent>();
		uriToComponent = new HashMap<URI, WeakReference<Component>>();
	}

	protected void updatePublishedComponents() {
		for (Entry<URI, List<WeakReference<Component>>> entry : uriToPublishedComponents
				.entrySet()) {
			URI id = entry.getKey();
			for (WeakReference<Component> reference : entry.getValue()) {
				Component component = reference.get();
				if (component == null) {
					continue;
				}
				if (component instanceof JToolBar) {
					populateToolBar((JToolBar) component, id);
				} else if (component instanceof JMenuBar) {
					populateMenuBar((JMenuBar) component, id);
				} else {
					logger.warn("Could not update published component " + id
							+ ": " + component.getClass());
				}
			}
		}
	}

	public static abstract class MenuManagerEvent {
	}

	/**
	 * Event sent to observers registered by {@link MenuManager#addObserver(Observer)} when the menus have been updated, i.e. when {@link MenuManager#update()} has been called.
	 */
	public static class UpdatedMenuManagerEvent extends MenuManagerEvent {
	}

	/**
	 * {@link Comparator} that can order {@link MenuComponent}s by their
	 * {@link MenuComponent#getPositionHint()}.
	 * 
	 */
	protected static class MenuElementComparator implements
			Comparator<MenuComponent> {
		public int compare(MenuComponent a, MenuComponent b) {
			return a.getPositionHint() - b.getPositionHint();
		}
	}

	/**
	 * Update menus when {@link SPIRegistry} is updated.
	 * 
	 */
	protected class MenuRegistryObserver implements Observer<SPIRegistryEvent> {
		public void notify(Observable<SPIRegistryEvent> sender,
				SPIRegistryEvent message) throws Exception {
			if (message.equals(SPIRegistry.UPDATED)) {
				update();
			}
		}
	}

}
