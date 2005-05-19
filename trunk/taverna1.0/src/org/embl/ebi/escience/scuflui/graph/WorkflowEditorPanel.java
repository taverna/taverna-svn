/*
 * Created on May 18, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.embl.ebi.escience.scuflui.actions.LoadWebWorkflowAction;
import org.embl.ebi.escience.scuflui.actions.LoadWorkflowAction;
import org.embl.ebi.escience.scuflui.actions.ResetAction;
import org.embl.ebi.escience.scuflui.actions.SaveWorkflowAction;

/**
 * COMMENT 
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1 $
 */
public class WorkflowEditorPanel extends JPanel implements ScuflUIComponent
{

	private WorkflowEditor editor;

	/**
	 * 
	 */
	public WorkflowEditorPanel()
	{
		super();
		// TODO Implement WorkflowEditorPanel constructor
	}

	public void attachToModel(ScuflModel model)
	{
		setLayout(new BorderLayout());
		
		// Create the tool bar
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.setMaximumSize(new Dimension(2000,30));
		toolbar.setBorderPainted(true);

		// Add options to load the workflow, import from web, save and reset
		// These options were available from the workbench file menu previously
		// but I think they're more intuitive here as buttons.
		toolbar.add(new LoadWorkflowAction(model));
		toolbar.add(new LoadWebWorkflowAction(model));
		toolbar.add(new SaveWorkflowAction(model));
		toolbar.add(Box.createHorizontalGlue());		
		toolbar.add(new ResetAction(model));
		
		editor = new WorkflowEditor();
		editor.attachToModel(model);
		
		JScrollPane scrollPane = new JScrollPane(editor); 
		scrollPane.setPreferredSize(new Dimension(0,0));
		
		add(toolbar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void detachFromModel()
	{
		editor.detachFromModel();
	}

	public ImageIcon getIcon()
	{
		return ScuflIcons.windowDiagram;
	}

	public String getName()
	{
		return "Workflow Editor (BETA)";
	}
}
