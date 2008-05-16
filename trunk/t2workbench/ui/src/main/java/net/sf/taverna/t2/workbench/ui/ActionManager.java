package net.sf.taverna.t2.workbench.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;

import net.sf.taverna.t2.spi.SPIRegistry;

public class ActionManager {
	
	private static final int GROUP_SIZE = 10;

	private SPIRegistry<WorkbenchAction> actionRegistry = new SPIRegistry<WorkbenchAction>(WorkbenchAction.class);

	private JMenuBar menuBar;
	
	private JToolBar toolBar;
	
	private Menu root = new Menu("root", "Root", 0);
	
	private List<WorkbenchAction> toolBarActions = new ArrayList<WorkbenchAction>();
	
	/**
	 * Constructs a new instance of ActionManager.
	 *
	 * @param menuBar
	 * @param toolBar
	 */
	public ActionManager(JMenuBar menuBar, JToolBar toolBar) {
		this.menuBar = menuBar;
		this.toolBar = toolBar;		
		update();		
	}

	private void update() {
		for (WorkbenchAction actionSPI : actionRegistry.getInstances()) {
			if (actionSPI.getMenuPosition() >= 0) {
				Menu menu = new Menu(actionSPI);
				mergeMenus(root, getRootMenu(menu));
			}
			if (actionSPI.getToolBarPosition() >= 0) {
				toolBarActions.add(actionSPI);
			}
		}		

		menuBar.removeAll();
		layoutMenuBar();
		
		toolBar.removeAll();
		layoutToolBar();
	}
	
	private void layoutToolBar() {
		Collections.sort(toolBarActions, new Comparator<WorkbenchAction>() {
			public int compare(WorkbenchAction a, WorkbenchAction b) {
				return a.getToolBarPosition() - b.getToolBarPosition();
			}
		});
		boolean firstTool = true;
		int currentPosition = 0;
		for (WorkbenchAction action : toolBarActions) {
			if (firstTool) {
				firstTool = false;
			} else {
				if ((currentPosition / GROUP_SIZE) < (action.getToolBarPosition() / GROUP_SIZE)) {
					toolBar.addSeparator();
				}
			}
			currentPosition = action.getToolBarPosition();
			List<Action> actions = action.getActions();
			if (actions.size() == 1) {
				toolBar.add(actions.get(0));
			} else {
				//TODO
			}
		}
	}

	private void layoutMenuBar() {
		List<Menu> menuList = new ArrayList<Menu>(root.getMenus());
		Collections.sort(menuList);
		for (Menu childMenu : menuList) {
			String menuName = childMenu.getName();
			if (menuName == null) {
				menuName = childMenu.getId();
			}
			JMenu jmenu = new JMenu(menuName);
			menuBar.add(jmenu);

			layoutMenus(jmenu, childMenu);
		}
	}
	
	private void layoutMenus(JMenu rootMenu, Menu menu) {
		List<Menu> menuList = menu.getMenus();
		Collections.sort(menuList);
		boolean firstMenu = true;
		int currentPosition = 0;
		for (Menu childMenu : menuList) {
			if (firstMenu) {
				firstMenu = false;
			} else {
				if ((currentPosition / GROUP_SIZE) < (childMenu.getPosition() / GROUP_SIZE)) {
					rootMenu.addSeparator();
				}
			}
			currentPosition = childMenu.getPosition();
			if (childMenu.isLeaf()) {
				List<Action> actions = childMenu.getActions();
				if (actions.size() == 1) {
					Action action = actions.get(0);
					if (childMenu.isToggle()) {
						rootMenu.add(new JCheckBoxMenuItem(action));
					} else {
						rootMenu.add(new JMenuItem(action));
					}
				} else if (actions.size() > 1) {
					ButtonGroup buttonGroup = new ButtonGroup();
					for (Action action : actions) {
						JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(action);
						buttonGroup.add(menuItem);
						rootMenu.add(menuItem);
					}
				}
			} else {
				String menuName = childMenu.getName();
				if (menuName == null) {
					menuName = childMenu.getId();
				}
				JMenu jmenu = new JMenu(menuName);
				rootMenu.add(jmenu);
				layoutMenus(jmenu, childMenu);				
			}
		}
	}
	
	private void mergeMenus(Menu root, Menu menu) {
		if (root.containsMenu(menu)) {
			Menu existingMenu = root.getMenu(menu);
			if (existingMenu.getName() == null) {
				existingMenu.setName(menu.getName());
			}
			if (existingMenu.getPosition() == 0) {
				existingMenu.setPosition(menu.getPosition());
			}
			for (Menu childMenu : menu.getMenus()) {
				mergeMenus(existingMenu, childMenu);
			}
		} else {
			root.addMenu(menu);
		}
	}
	
	private Menu getRootMenu(Menu menu) {
		Menu parentMenu = menu.getParent();
		if (parentMenu == null) {
			return menu;
		} else {
			return getRootMenu(parentMenu);
		}
	}
	
}

class Menu implements Comparable<Menu> {

	private String id;
	
	private String name;
	
	private int position;
	
	private boolean toggle;
	
	private Menu parent;
	
	private Map<String, Menu> children = new HashMap<String, Menu>();
	
	private List<Action> actions = new ArrayList<Action>();
	
	public Menu(String id, String name, int position) {
		this.id = id;
		this.name = name;
		this.position = position;
	}

	public Menu(WorkbenchAction action) {
		id = (String) action.getActions().get(0).getValue(Action.NAME);
		position = action.getMenuPosition();
		toggle = action.isToggleAction();
		actions = action.getActions();
		String menu = action.getMenu();
		createMenu(menu).addMenu(this);
	}
	
	private Menu createMenu(String menuString) {
		String[] menus = menuString.split("\\.");
		String[] leafMenu = menus[menus.length - 1].split("\\|");
		Menu menu = null;
		if (leafMenu.length == 1) {
			menu = new Menu(leafMenu[0], null, 0);
		} else if (leafMenu.length == 2) {
			menu = new Menu(leafMenu[0], leafMenu[1], 0);
		} else if (leafMenu.length == 3) {
			menu = new Menu(leafMenu[0], leafMenu[1], Integer.parseInt(leafMenu[2]));
		}
		if (menus.length > 1) {
			createMenu(menuString.substring(0, menuString.lastIndexOf('.'))).addMenu(menu);
		}
		return menu;		
	}

	/**
	 * Returns the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the position.
	 *
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Sets the position.
	 *
	 * @param position the new position
	 */
	public void setPosition(int position) {
		this.position = position;
	}
	
	/**
	 * Returns the parent.
	 *
	 * @return the parent
	 */
	public Menu getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent the new parent
	 */
	private void setParent(Menu parent) {
		this.parent = parent;
	}

	public List<Menu> getMenus() {
		return new ArrayList<Menu>(children.values());
	}
	
	public Menu getMenu(Menu menu) {
		return children.get(menu.getId());
	}
	
	public void addMenu(Menu menu) {
		menu.setParent(this);
		children.put(menu.getId(), menu);
	}
	
	public boolean containsMenu(Menu menu) {
		return children.containsKey(menu.getId());
	}

	public int compareTo(Menu o) {
		return getPosition() - o.getPosition(); 
	}

	/**
	 * Returns the actions.
	 *
	 * @return the actions
	 */
	public List<Action> getActions() {
		return actions;
	}
	
	public void addAction(Action action) {
		actions.add(action);
	}
	
	public boolean isLeaf() {
		return children.size() == 0;
	}
	
	public boolean isToggle() {
		return toggle;
	}
	
	public String toString() {
		return toString("");
	}
	
	public String toString(String indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent + id + "[" + name + "]" + position + "\n");
		for (Menu child : children.values()) {
			sb.append(child.toString(indent + " "));
		}
		return sb.toString();
	}
	
}

