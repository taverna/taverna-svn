/*
 * Created on Nov 24, 2004
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.NoContextMenuFoundException;
import org.embl.ebi.escience.scuflui.ScuflContextMenuFactory;
import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.embl.ebi.escience.scuflui.ShadedLabel;
import org.embl.ebi.escience.scuflui.dnd.FactorySpecFragment;
import org.embl.ebi.escience.scuflui.dnd.SpecFragmentTransferable;
import org.embl.ebi.escience.scuflui.graph.model.LayoutManager;
import org.embl.ebi.escience.scuflui.graph.model.ScuflGraphModel;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.jdom.Element;
import org.jgraph.JGraph;
import org.jgraph.graph.GraphConstants;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 */
public class WorkflowEditor extends JPanel implements ScuflUIComponent, DropTargetListener
{
	private ScuflGraphModel graphModel;
	JGraph graph;
	ScuflModel model;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#attachToModel(org.embl.ebi.escience.scufl.ScuflModel)
	 */
	public void attachToModel(ScuflModel model)
	{
		setLayout(new BorderLayout());
		graphModel = new ScuflGraphModel(model);
		new LayoutManager(graphModel);
		graph = new JGraph(graphModel);
		graph.setAntiAliased(true);
		graph.setBendable(true);
		graph.setHighlightColor(Color.BLUE);
		graph.setMoveable(false);
		graph.setSizeable(false);
		// graph.setDragEnabled(false);
		// graph.setPortsVisible(true);
		graph.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_DELETE)
				{
					Object[] selected = graph.getSelectionCells();
					graph.getModel().remove(selected);
				}
			}
		});
		graph.addMouseMotionListener(new MouseMotionAdapter()
		{
			public void mouseMoved(MouseEvent e)
			{
				// TODO Auto-generated method stub

			}
		});
		graph.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				showPopup(e);
			}

			public void mouseReleased(MouseEvent e)
			{
				showPopup(e);
			}

			private void showPopup(MouseEvent event)
			{
				if (event.isPopupTrigger())
				{
					try
					{
						Object scuflObject = graph.getSelectionCell();
						if (scuflObject != null)
						{
							try
							{
								JPopupMenu menu = ScuflContextMenuFactory.getMenuForObject(null,
											scuflObject, WorkflowEditor.this.model);
								if (scuflObject instanceof Processor)
								{
									Processor processor = (Processor) scuflObject;
									Port[] ports = processor.getPorts();
									if (ports.length > 0)
									{
										if (ports.length == 1)
										{
											String text;
											Icon icon;
											if(ports[0] instanceof InputPort)
											{
												text = "Add Link to " + ports[0].getName();
												icon = ScuflIcons.inputPortIcon;
											}
											else
											{
												text = "Add Link from " + ports[0].getName();
												icon = ScuflIcons.outputPortIcon;
											}
											JMenuItem linkItem = new JMenuItem(text, icon);
											menu.add(linkItem);
										}
										else
										{
											JMenu linkMenu = new JMenu("Add Link to...");
											ports = processor.getInputPorts();
											if (ports.length > 0)
											{
												linkMenu.add(new ShadedLabel("Inputs",
														ShadedLabel.TAVERNA_GREEN));
												linkMenu.addSeparator();
												for (int index = 0; index < ports.length; index++)
												{
													JMenuItem inputItem = new JMenuItem(
															ports[index].getName(), ScuflIcons.inputPortIcon);
													linkMenu.add(inputItem);
												}
												linkMenu.addSeparator();
											}
											ports = processor.getOutputPorts();
											if (ports.length > 0)
											{
												linkMenu.add(new ShadedLabel("Outputs",
														ShadedLabel.TAVERNA_ORANGE));
												linkMenu.addSeparator();
												for (int index = 0; index < ports.length; index++)
												{
													JMenuItem inputItem = new JMenuItem(
															ports[index].getName(), ScuflIcons.outputPortIcon);
													linkMenu.add(inputItem);
												}
											}
											menu.add(linkMenu);											
										}
									}
								}
								menu.show(WorkflowEditor.this, event.getX(), event.getY());
							}
							catch (NoContextMenuFoundException ncmfe)
							{
								// just means that there wasn't a suitable menu
								// for the selected node.
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					// TODO If adding edge
				}
			}
		});
		GraphConstants.SELECTION_STROKE = new BasicStroke(1);
		this.model = model;

		new DropTarget(graph, this);

		add(graph, BorderLayout.CENTER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#detachFromModel()
	 */
	public void detachFromModel()
	{
		// graph.removeAll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#getIcon()
	 */
	public ImageIcon getIcon()
	{
		return ScuflIcons.windowDiagram;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Component#getName()
	 */
	public String getName()
	{
		return "Workflow Editor (BETA)";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
	 */
	public void dragEnter(DropTargetDragEvent dtde)
	{
		int action = dtde.getDropAction();
		if (dtde.isDataFlavorSupported(SpecFragmentTransferable.factorySpecFragmentFlavor))
		{
			dtde.acceptDrag(action);
		}
		else
		{
			dtde.rejectDrag();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
	 */
	public void dragOver(DropTargetDragEvent dtde)
	{
		// Do nothing?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
	 */
	public void dropActionChanged(DropTargetDragEvent dtde)
	{
		// Do nothing?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 */
	public void drop(DropTargetDropEvent dtde)
	{
		try
		{
			DataFlavor f = SpecFragmentTransferable.factorySpecFragmentFlavor;
			Transferable t = dtde.getTransferable();
			if (dtde.isDataFlavorSupported(f))
			{
				// Have something of type factorySpecFragmentFlavor;
				FactorySpecFragment fsf = (FactorySpecFragment) t.getTransferData(f);
				String validName = model.getValidProcessorName(fsf.getFactoryNodeName());
				Element wrapperElement = new Element("wrapper");
				wrapperElement.addContent(fsf.getElement());

				Processor newProcessor = ProcessorHelper.loadProcessorFromXML(wrapperElement,
						model, validName);
				model.addProcessor(newProcessor);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
	 */
	public void dragExit(DropTargetEvent dte)
	{
		// Do nothing?
	}
}