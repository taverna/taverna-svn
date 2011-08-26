/*
 * Created on Aug 31, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scuflui.renderers.RendererException;
import org.embl.ebi.escience.scuflui.renderers.RendererRegistry;
import org.embl.ebi.escience.scuflui.renderers.RendererSPI;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class ResultTablePanel extends JPanel implements ListSelectionListener
{
	ResultTable resultTable;
	private JSplitPane split;
	
	public ResultTablePanel(ScuflModel model, WorkflowInstance workflowInstance)
	{
		super();
		initComponents(model, workflowInstance);
	}
	
	private void initComponents(ScuflModel model, WorkflowInstance workflowInstance)
	{
		setLayout(new BorderLayout());
		resultTable = new ResultTable(model, workflowInstance);
		resultTable.getSelectionModel().addListSelectionListener(this);
		
		JScrollPane pane = new JScrollPane();
		pane.setViewportView(resultTable);
		pane.setPreferredSize(new Dimension(0, 0));
		
		split = new JSplitPane();
		split.setLeftComponent(pane);
		split.setRightComponent(null);
		split.setResizeWeight(0.8);
		
		add(split, BorderLayout.CENTER);		
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e)
	{
		System.out.println("Cell: " + resultTable.getSelectedColumn() + "," + resultTable.getSelectedRow());
		ResultTableCell cell = (ResultTableCell)resultTable.getValueAt(resultTable.getSelectedRow(),resultTable.getSelectedColumn());
		if(cell != null)
		{
			RendererSPI renderer = RendererRegistry.instance().getRenderer(cell.getDataThing());
			try
			{
				JScrollPane scroll = new JScrollPane(renderer.getComponent(RendererRegistry.instance(), cell.getDataThing()));
				scroll.setPreferredSize(new Dimension(0,0));
				split.setRightComponent(scroll);
			}
			catch (RendererException e1)
			{
				// TODO Handle RendererException
				e1.printStackTrace();
			}
		}
		else
		{
			split.setRightComponent(null);
		}
	}
}