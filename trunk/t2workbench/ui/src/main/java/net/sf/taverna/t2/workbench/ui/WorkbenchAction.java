package net.sf.taverna.t2.workbench.ui;

import java.util.List;

import javax.swing.Action;

/**
 * SPI for specifying Actions and the Menus and Tool Bars that they will
 * be added to.
 * 
 * @author David Withers
 */
public interface WorkbenchAction {

	/**
	 * The actions to add to the workbench.
	 * 
	 * If there is more than one action they will be treated as a group; only
	 * one action of a group can be selected at one time.
	 * 
	 * @return
	 */
	public List<Action> getActions();

	/**
	 * Specifies the menu that the action should be placed on. Menus are
	 * specified as a dot separated list of menus, each menu consists of
	 * id|name|position. e.g. a NewFileAction may specify its menu as
	 * "file|File|1.new|New|5".
	 * 
	 * @return
	 */
	public String getMenu();

	/**
	 * Specifies to the position that the action would like to be placed on the
	 * menu.
	 * 
	 * Actions (and sub menus) on a menu are grouped into sets of 10. The first
	 * position of a group is 0, 10, 20 etc, the second is 1, 11, 22 and so on.
	 * 
	 * If more than one action specifies the same position then the ordering is
	 * unspecified.
	 * 
	 * A value of less than zero means that the action will not be placed on a
	 * menu.
	 * 
	 * @return the position that the action would like to be placed on the menu
	 */
	public int getMenuPosition();

	/**
	 * Specifies to the position that the action would like to be placed on the
	 * tool bar.
	 * 
	 * Actions on a tool bar are grouped into sets of 10. The first position of
	 * a group is 0, 10, 20 etc, the second is 1, 11, 22 and so on.
	 * 
	 * If more than one action specifies the same position then the ordering is
	 * unspecified.
	 * 
	 * A value of less than zero means that the action will not be placed on the
	 * tool bar.
	 * 
	 * @return the position that the action would like to be placed on the tool
	 *         bar
	 */
	public int getToolBarPosition();

	/**
	 * Specifies if this is a toggle action.
	 * 
	 * If true the action will be a check box on a menu or tool bar. This value
	 * is ignored if there is more than one action.
	 * 
	 * @return
	 */
	public boolean isToggleAction();

}
