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
import net.sf.taverna.t2.ui.menu.DefaultMenuBar;
import net.sf.taverna.t2.ui.menu.DefaultToolBar;
import net.sf.taverna.t2.ui.menu.MenuComponent;
import net.sf.taverna.t2.ui.menu.MenuComponent.MenuType;
import net.sf.taverna.t2.ui.menu.impl.MenuManager.MenuManagerEvent;

public class MenuManager implements Observable<MenuManagerEvent> {

	private static MenuManager instance;

	private static Logger logger = Logger.getLogger(MenuManager.class);

	public static synchronized MenuManager getInstance() {
		if (instance == null) {
			instance = new MenuManager();
		}
		return instance;
	}

	private WeakHashMap<Component, URI> componentToUri;

	private MenuElementComparator menuElementComparator = new MenuElementComparator();

	private HashMap<URI, List<MenuComponent>> menuElementTree;

	private SPIRegistry<MenuComponent> menuRegistry = new SPIRegistry<MenuComponent>(
			MenuComponent.class);

	private MultiCaster<MenuManagerEvent> multiCaster;

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

	public void addObserver(Observer<MenuManagerEvent> observer) {
		multiCaster.addObserver(observer);
	}

	public JMenuBar createMenuBar() {
		return createMenuBar(DefaultMenuBar.DEFAULT_MENU_BAR);
	}

	public JMenuBar createMenuBar(URI id) {
		JMenuBar menuBar = new JMenuBar();
		populateMenuBar(menuBar, id);
		registerComponent(id, menuBar, true);
		return menuBar;
	}

	public JToolBar createToolBar() {
		return createToolBar(DefaultToolBar.DEFAULT_TOOL_BAR);
	}

	public JToolBar createToolBar(URI id) {
		JToolBar toolbar = new JToolBar();
		populateToolBar(toolbar, id);
		registerComponent(id, toolbar, true);
		return toolbar;
	}

	public synchronized Component getComponentByURI(URI id) {
		WeakReference<Component> componentRef = uriToComponent.get(id);
		if (componentRef == null) {
			return null;
		}
		// Might also be null
		return componentRef.get();
	}

	public List<Observer<MenuManagerEvent>> getObservers() {
		return multiCaster.getObservers();
	}

	public synchronized URI getURIByComponent(Component component) {
		return componentToUri.get(component);
	}

	public void removeObserver(Observer<MenuManagerEvent> observer) {
		multiCaster.removeObserver(observer);
	}

	public void update() {
		synchronized (this) {
			if (updating) {
				return;
			}
			updating = true;
		}
		try {
			doUpdate();
		} finally {
			synchronized (this) {
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
			logger.debug("Found menu element " + menuElement.getId() + " " + menuElement);
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
		if (! components.isEmpty()) {
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

	protected void resetCollections() {
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

	public static class MenuManagerEvent {
	}

	public static class UpdatedMenuManagerEvent extends MenuManagerEvent {
	}

	protected static class MenuElementComparator implements
			Comparator<MenuComponent> {
		public int compare(MenuComponent a, MenuComponent b) {
			return a.getPositionHint() - b.getPositionHint();
		}
	}

	protected class MenuRegistryObserver implements Observer<SPIRegistryEvent> {
		public void notify(Observable<SPIRegistryEvent> sender,
				SPIRegistryEvent message) throws Exception {
			if (message.equals(SPIRegistry.UPDATED)) {
				update();
			}
		}
	}

}
