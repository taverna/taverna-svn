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
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
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
import org.embl.ebi.escience.scuflui.actions.AddInputAction;
import org.embl.ebi.escience.scuflui.actions.AddOutputAction;
import org.embl.ebi.escience.scuflui.actions.RemoveAction;
import org.embl.ebi.escience.scuflui.actions.ScuflModelAction;
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
public class WorkflowEditor extends JPanel implements ScuflUIComponent
{
	private ScuflGraphModel graphModel;
	JGraph graph;

	private class StartLinkAction extends ScuflModelAction
	{
		private Port port;

		/**
		 * @param model
		 * @param port
		 */
		protected StartLinkAction(ScuflModel model, Port port)
		{
			super(model);
			this.port = port;
			putValue(NAME, port.getName());
			if (port instanceof InputPort)
			{
				putValue(SMALL_ICON, ScuflIcons.inputPortIcon);
			}
			else
			{
				putValue(SMALL_ICON, ScuflIcons.outputPortIcon);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e)
		{
			// TODO Start adding link
			port.getName();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#attachToModel(org.embl.ebi.escience.scufl.ScuflModel)
	 */
	public void attachToModel(final ScuflModel model)
	{
		setLayout(new BorderLayout());
		graphModel = new ScuflGraphModel();
		graph = new JGraph(graphModel);
		graph.setAntiAliased(true);
		graph.setAutoSizeOnValueChange(true);
		graph.setBendable(true);
		graph.setMoveable(false);
		graph.setSizeable(false);
		graph.setGridColor(Color.WHITE);
		graph.setLockedHandleColor(Color.BLUE);
		graph.setHighlightColor(Color.BLUE);
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
				// TODO Update the edited edge, if any

			}
		});
		graph.addMouseWheelListener(new MouseWheelListener()
		{
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				// Zoom zoom
				graph.setScale(graph.getScale() + (e.getWheelRotation() / 10.0));
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
					Object[] selected = graph.getSelectionCells();
					if (selected != null)
					{
						if (selected.length == 1)
						{
							Object scuflObject = selected[0];
							try
							{
								JPopupMenu menu = ScuflContextMenuFactory.getMenuForObject(null,
										scuflObject, model);
								if (scuflObject instanceof Processor)
								{
									if (scuflObject == model.getWorkflowSinkProcessor())
									{
										menu = new JPopupMenu();
										menu.add(new ShadedLabel("Workflow Outputs",
												ShadedLabel.TAVERNA_GREEN));
										menu.add(new AddOutputAction(model));
									}
									else if (scuflObject == model.getWorkflowSourceProcessor())
									{
										menu = new JPopupMenu();
										menu.add(new ShadedLabel("Workflow Inputs",
												ShadedLabel.TAVERNA_GREEN));
										menu.add(new AddInputAction(model));
									}
									else
									{
										Processor processor = (Processor) scuflObject;
										Port[] ports = processor.getPorts();
										if (ports.length > 0)
										{
											if (ports.length == 1)
											{
												// TODO Change text
												menu.add(new StartLinkAction(model, ports[0]));
											}
											else
											{
												JMenu linkMenu = new JMenu("Add Link to...");
												linkMenu.setIcon(ScuflIcons.dataLinkIcon);
												ports = processor.getInputPorts();
												if (ports.length > 0)
												{
													linkMenu.add(new ShadedLabel("Inputs",
															ShadedLabel.TAVERNA_GREEN));
													linkMenu.addSeparator();
													for (int index = 0; index < ports.length; index++)
													{
														linkMenu.add(new StartLinkAction(model,
																ports[index]));
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
														linkMenu.add(new StartLinkAction(model,
																ports[index]));
													}
												}
												menu.add(linkMenu);
											}
										}
									}
								}
								else if (scuflObject instanceof Port)
								{
									menu.add(new StartLinkAction(model, (Port) scuflObject));
								}
								menu.show(WorkflowEditor.this, event.getX(), event.getY());
							}
							catch (NoContextMenuFoundException ncmfe)
							{
								// just means that there wasn't a suitable menu
								// for the selected node.
							}
						}
						else if (selected.length > 1)
						{
							JPopupMenu menu = new JPopupMenu();
							menu.add(new RemoveAction(model, selected));
							menu.show(WorkflowEditor.this, event.getX(), event.getY());
						}
						else
						{
							JPopupMenu menu = new JPopupMenu();
							menu.add(new ShadedLabel("Workflow", ShadedLabel.TAVERNA_GREEN));
							menu.addSeparator();
							menu.add(new AddInputAction(model));
							menu.add(new AddOutputAction(model));
							menu.show(WorkflowEditor.this, event.getX(), event.getY());
						}
					}
				}
				else
				{
					// TODO If adding edge
				}
			}
		});
		GraphConstants.SELECTION_STROKE = new BasicStroke(2, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND, 1, new float[] { 4, 6 }, 0);

		new DropTarget(graph, new DropTargetAdapter()
		{
			/*
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

						Processor newProcessor = ProcessorHelper.loadProcessorFromXML(
								wrapperElement, model, validName);
						model.addProcessor(newProcessor);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		add(graph, BorderLayout.CENTER);
		new LayoutManager(graphModel);
		graphModel.attachToModel(model);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#detachFromModel()
	 */
	public void detachFromModel()
	{
		graphModel.detachFromModel();
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#getIcon()
	 */
	public ImageIcon getIcon()
	{
		return ScuflIcons.windowDiagram;
	}

	/*
	 * @see java.awt.Component#getName()
	 */
	public String getName()
	{
		return "Workflow Editor (BETA)";
	}
}