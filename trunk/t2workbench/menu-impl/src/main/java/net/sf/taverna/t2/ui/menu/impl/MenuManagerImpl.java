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

import javax.help.CSH;
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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.spi.SPIRegistry.SPIRegistryEvent;
import net.sf.taverna.t2.ui.menu.DefaultMenuBar;
import net.sf.taverna.t2.ui.menu.DefaultToolBar;
import net.sf.taverna.t2.ui.menu.MenuComponent;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.ui.menu.MenuComponent.MenuType;

import org.apache.log4j.Logger;

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
public class MenuManagerImpl extends MenuManager {

	private static Logger logger = Logger.getLogger(MenuManagerImpl.class);

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

	public MenuManagerImpl() {
		menuRegistry.addObserver(new MenuRegistryObserver());
		multiCaster = new MultiCaster<MenuManagerEvent>(this);
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.ui.menu.impl.MenuManager#addObserver(net.sf.taverna.t2.lang.observer.Observer)
	 */
	public void addObserver(Observer<MenuManagerEvent> observer) {
		multiCaster.addObserver(observer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.ui.menu.impl.MenuManager#createMenuBar()
	 */
	@Override
	public JMenuBar createMenuBar() {
		return createMenuBar(DefaultMenuBar.DEFAULT_MENU_BAR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.ui.menu.impl.MenuManager#createMenuBar(java.net.URI)
	 */
	@Override
	public JMenuBar createMenuBar(URI id) {
		JMenuBar menuBar = new JMenuBar();
		populateMenuBar(menuBar, id);
		registerComponent(id, menuBar, true);
		return menuBar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.ui.menu.impl.MenuManager#createToolBar()
	 */
	@Override
	public JToolBar createToolBar() {
		return createToolBar(DefaultToolBar.DEFAULT_TOOL_BAR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.ui.menu.impl.MenuManager#createToolBar(java.net.URI)
	 */
	@Override
	public JToolBar createToolBar(URI id) {
		JToolBar toolbar = new JToolBar();
		populateToolBar(toolbar, id);
		registerComponent(id, toolbar, true);
		return toolbar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.ui.menu.impl.MenuManager#getComponentByURI(java.net.URI)
	 */
	@Override
	public synchronized Component getComponentByURI(URI id) {
		WeakReference<Component> componentRef = uriToComponent.get(id);
		if (componentRef == null) {
			return null;
		}
		// Might also be null it reference has gone dead
		return componentRef.get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.ui.menu.impl.MenuManager#getObservers()
	 */
	public List<Observer<MenuManagerEvent>> getObservers() {
		return multiCaster.getObservers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.ui.menu.impl.MenuManager#getURIByComponent(java.awt.Component)
	 */
	@Override
	public synchronized URI getURIByComponent(Component component) {
		return componentToUri.get(component);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.ui.menu.impl.MenuManager#removeObserver(net.sf.taverna.t2.lang.observer.Observer)
	 */
	public void removeObserver(Observer<MenuManagerEvent> observer) {
		multiCaster.removeObserver(observer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.ui.menu.impl.MenuManager#update()
	 */
	@Override
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
		List<Component> subComponents = makeComponents(menuId, false, false);
		if (subComponents.isEmpty()) {
			logger.warn("No sub components found for menu " + menuId);
			return;
		}

		JMenu menu = new JMenu(menuComponent.getAction());
		for (Component menuItem : subComponents) {
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

	private void addOptionGroup(List<Component> components, URI optionGroupId,
			boolean isToolbar) {
		List<Component> buttons = makeComponents(optionGroupId, isToolbar, true);
		addNullSeparator(components);
		if (buttons.isEmpty()) {
			logger.warn("No sub components found for option group "
					+ optionGroupId);
			return;
		}
		ButtonGroup buttonGroup = new ButtonGroup();

		for (Component button : buttons) {
			if (button instanceof AbstractButton) {
				buttonGroup.add((AbstractButton) button);
			} else {
				logger.warn("Component of button group " + optionGroupId
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

	private void addSection(List<Component> components, URI sectionId,
			boolean isToolbar, boolean isOptionGroup) {
		List<Component> childComponents = makeComponents(sectionId, isToolbar,
				isOptionGroup);
		addNullSeparator(components);
		if (childComponents.isEmpty()) {
			logger.warn("No sub components found for section " + sectionId);
			return;
		}
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
						toolbarizeButton((AbstractButton) actionComponent);
					} else {
						actionComponent = new JRadioButtonMenuItem(action);
					}
				} else {
					if (isToolbar) {
						actionComponent = new JButton(action);
						toolbarizeButton((AbstractButton) actionComponent);

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

	protected void toolbarizeButton(AbstractButton actionButton) {
		Action action = actionButton.getAction();
		if (action.getValue(Action.SHORT_DESCRIPTION) == null) {
			action.putValue(Action.SHORT_DESCRIPTION, action
					.getValue(Action.NAME));
		}
		actionButton.setBorder(new EmptyBorder(0, 2, 0, 2));
		// actionButton.setHorizontalTextPosition(JButton.CENTER);
		// actionButton.setVerticalTextPosition(JButton.BOTTOM);
		if (action.getValue(Action.SMALL_ICON) != null) {
			// Don't show the text
			actionButton.putClientProperty("hideActionText", Boolean.TRUE);
			// Since hideActionText seems to be broken in Java 5
			// and/or OS X
			actionButton.setText(null);
		}
	}

	protected void setHelpStringForComponent(Component component,
			URI componentId) {
		if (componentId != null) {
			String helpId = componentId.toASCIIString();
			CSH.setHelpIDString(component, helpId);
		}
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
				JButton toolbarButton = (JButton) component;
				toolbarButton.putClientProperty("hideActionText", true);
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

		setHelpStringForComponent(component, id);
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
