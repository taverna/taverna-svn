package net.sf.taverna.t2.workbench.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import net.sf.taverna.raven.launcher.Launchable;


/**
 * 
 * 
 * @author David Withers
 */
public class Workbench extends JFrame implements Launchable {

	private static final long serialVersionUID = 1L;

	private ActionManager actionManager;
	
	public Workbench() {
		setLayout(new BorderLayout());
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JSplitPane toolBarPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		add(toolBarPanel, BorderLayout.NORTH);
		
		JToolBar toolBar = new JToolBar();
		toolBarPanel.add(toolBar);
		
		actionManager = new ActionManager(menuBar, toolBar);
		
		setSize(new Dimension(500, 500));
		
	}
	
	public static void main(String[] args) {
		System.setProperty("raven.eclipse", "true");
		Workbench workbench = new Workbench();
		workbench.setVisible(true);
	}

	public int launch(String[] args) throws Exception {
		Workbench workbench = new Workbench();
		workbench.setVisible(true);
		return 0;
	}

}
