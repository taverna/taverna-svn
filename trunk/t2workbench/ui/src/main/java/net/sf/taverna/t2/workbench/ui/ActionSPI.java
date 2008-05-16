package net.sf.taverna.t2.workbench.ui;

import java.util.List;

import javax.swing.Action;

/**
 * 
 * 
 * @author David Withers
 */
public interface ActionSPI {

	public List<Action> getActions();
	
	public String getMenu();
	
	public int getMenuPosition();
	
	public int getToolBarPosition();
	
	public boolean isToggleAction();
	
}
