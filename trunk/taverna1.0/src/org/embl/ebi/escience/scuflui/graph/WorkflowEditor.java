/*
 * Created on Nov 24, 2004
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
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
import java.awt.geom.Rectangle2D;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellView;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.VertexView;

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
		graph = new JGraph(graphModel)
		{
			/*
			 * @see org.jgraph.JGraph#createVertexView(org.jgraph.JGraph, org.jgraph.graph.CellMapper, java.lang.Object)
			 */
			protected VertexView createVertexView(JGraph graph, CellMapper mapper, Object cell)
			{
				return new VertexView(cell, graph, mapper)
				{
					/*
					 * @see org.jgraph.graph.AbstractCellView#updateGroupBounds()
					 */
					protected void updateGroupBounds()
					{
						// Note: Prevent infinite recursion by removing
						// child edges that point to their parent.
						CellView[] childViews = getChildViews();
						LinkedList result = new LinkedList();
						for (int i = 0; i < childViews.length; i++)
							if (includeInGroupBounds(childViews[i]))
								result.add(childViews[i]);
						childViews = new CellView[result.size()];
						result.toArray(childViews);
						Rectangle2D r = getBounds(childViews);
						if (GraphConstants.isOpaque(getAllAttributes())) {
							Insets insets = GraphConstants.getBorder(getAllAttributes()).getBorderInsets(new JLabel());
							r.setFrame(r.getX() - insets.left, r.getY() - insets.top, r
									.getWidth()
									+ insets.left + insets.right, r.getHeight() + insets.top + insets.bottom);
						}
						groupBounds = r;
					}
					
					private boolean includeInGroupBounds(CellView view) {
						if (view instanceof EdgeView) {
							GraphModel model = graph.getModel();
							EdgeView edgeView = (EdgeView) view;
							if (edgeView.getCell() instanceof DefaultMutableTreeNode) {
								DefaultMutableTreeNode edge = (DefaultMutableTreeNode) edgeView
										.getCell();
								if (model.getSource(edge) instanceof TreeNode) {
									TreeNode source = (TreeNode) model.getSource(edge);
									if (((DefaultMutableTreeNode) source.getParent())
											.isNodeDescendant(edge)) {
										return false;
									}
								}
								if (model.getTarget(edge) instanceof TreeNode) {
									TreeNode target = (TreeNode) model.getTarget(edge);
									if (((DefaultMutableTreeNode) target.getParent())
											.isNodeDescendant(edge)) {
										return false;
									}
								}
							}
						}
						return true;
					}					
				};
			}
		};
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
				if (e.getKeyCode() == KeyEvent.VK_DELETE && graph.isEditable())
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
				if (event.isPopupTrigger() && graph.isEditable())
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
				if (dtde.isDataFlavorSupported(SpecFragmentTransferable.factorySpecFragmentFlavor) && graph.isEditable())
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
					if (dtde.isDataFlavorSupported(f) && graph.isEditable())
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

	public void setReadOnly(boolean readOnly)
	{
		graph.setEditable(!readOnly);
		graph.setEnabled(!readOnly);
	}
	
	/*
	 * @see java.awt.Component#getName()
	 */
	public String getName()
	{
		return "Workflow Editor (BETA)";
	}
	
	public void updateStatus(String status)
	{
		Element processorList;
		try
		{
			SAXBuilder builder = new SAXBuilder(false);
			Document document = builder.build(new StringReader(status));
			processorList = document.getRootElement().getChild("processorList");
			Map changes = new HashMap();
			for (Iterator i = processorList.getChildren("processor").iterator(); i.hasNext();)
			{
				Element processorElement = (Element) i.next();
				Processor processor = graphModel.getModel().locateProcessor(processorElement.getAttributeValue("name"));
				Map attributes = graphModel.getAttributes(processor);
				if(attributes != null)
				{
					// Get the first child of the processor element.
					List childElementList = processorElement.getChildren();
					if (childElementList.isEmpty() == false)
					{
						Element firstChildElement = (Element) childElementList.get(0);
						Color statusColour = getStatusColour(firstChildElement.getName());
						Color existing = GraphConstants.getBackground(attributes);
						if(statusColour != existing)
						{
							Map newColour = new HashMap();
							GraphConstants.setBackground(newColour, statusColour);
							changes.put(processor, newColour);
						}
					}
				}
			}
			if(!changes.isEmpty())
			{
				graphModel.edit(changes, null, null, null);
			}
		}
		catch (Exception e)
		{
			// TODO Handle exceptions
			e.printStackTrace();
		}
	}

	private Color getStatusColour(String status)
	{
		if (status.equals("ProcessComplete"))
		{
			return GraphColours.getColour("medium sea green", Color.GREEN);
		}
		else if (status.equals("ServiceError") || status.equals("ServiceFailure"))
		{
			return GraphColours.getColour("firebrick1", Color.RED);
		}
		else if (status.startsWith("Invoking"))
		{
			return GraphColours.getColour("medium purple", Color.MAGENTA);
		}
		return GraphColours.getColour("light gray", Color.GRAY);
	}	
}