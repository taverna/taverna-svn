/*
 * Created on Aug 31, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
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
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

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
		ResultThing thing = (ResultThing)resultTable.getValueAt(resultTable.getSelectedRow(),resultTable.getSelectedColumn());
		if(thing != null)
		{
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			
			JPanel textPanel = new JPanel();
			textPanel.setLayout(new GridLayout(2,1));
			
			JLabel otherLabel = new JLabel("This is the output of");
			JLabel outputLabel = new JLabel(thing.source.toString());
			outputLabel.setIcon(ProcessorHelper.getPreferredIcon(thing.source.getProcessor()));
			
			textPanel.add(otherLabel);
			textPanel.add(outputLabel);
			
			panel.add(textPanel, BorderLayout.NORTH);
			
			RendererSPI renderer = RendererRegistry.instance().getRenderer(thing.getDataThing());
			try
			{
				JScrollPane scroll = new JScrollPane(renderer.getComponent(RendererRegistry.instance(), thing.getDataThing()));
				scroll.setPreferredSize(new Dimension(0,0));
				panel.add(scroll, BorderLayout.CENTER);
			}
			catch (RendererException e1)
			{
				// TODO Handle RendererException
				e1.printStackTrace();
			}
			split.setRightComponent(panel);			
		}
		else
		{
			split.setRightComponent(null);
		}
	}
}