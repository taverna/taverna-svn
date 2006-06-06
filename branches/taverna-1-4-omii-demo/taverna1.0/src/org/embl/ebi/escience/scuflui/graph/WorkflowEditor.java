/*
 * Created on Nov 24, 2004
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

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
import org.embl.ebi.escience.scuflui.graph.model.ScuflGraphModel;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jgraph.JGraph;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.graph.CellHandle;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphContext;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;
import org.jgraph.plaf.basic.BasicGraphUI;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 */
public class WorkflowEditor extends JGraph implements ScuflUIComponent {
	private class StartLinkAction extends ScuflModelAction {
		private Port port;

		/**
		 * @param model
		 * @param port
		 */
		protected StartLinkAction(ScuflModel model, Port port) {
			super(model);
			this.port = port;
			putValue(NAME, port.getName());
			if (port instanceof InputPort) {
				putValue(SMALL_ICON, ScuflIcons.inputPortIcon);
			} else {
				putValue(SMALL_ICON, ScuflIcons.outputPortIcon);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			((MarqueeHandler) getMarqueeHandler()).startEdge(WorkflowEditor.this, port);
		}
	}

	public class ShowBoringModel extends JToggleButton.ToggleButtonModel {
		public boolean isSelected() {
			return getScuflGraphModel().isShowingBoring();
		}

		public void setSelected(boolean b) {
			getScuflGraphModel().setShowBoring(b);

			// Send ChangeEvent
			fireStateChanged();

			// Send ItemEvent
			fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this,
					this.isSelected() ? ItemEvent.SELECTED : ItemEvent.DESELECTED));
		}
	}

	RowLayout layoutManager;

	/**
	 * 
	 */
	public WorkflowEditor() {
		super();
		setModel(new ScuflGraphModel());
	}

	public ScuflGraphModel getScuflGraphModel() {
		return (ScuflGraphModel) graphModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#attachToModel(org.embl.ebi.escience.scufl.ScuflModel)
	 */
	public void attachToModel(final ScuflModel model) {
		GraphLayoutCache layoutCache = getGraphLayoutCache();
		layoutManager = new RowLayout(graphModel, layoutCache);
		layoutCache.setAutoSizeOnValueChange(true);
		layoutCache.setSelectsLocalInsertedCells(false);
		layoutCache.setSelectsAllInsertedCells(false);
		layoutCache.setFactory(new DefaultCellViewFactory() {
			protected EdgeView createEdgeView(Object cell) {
				return new EdgeView(cell) {
					protected Point2D getPointLocation(int index) {
						Object obj = points.get(index);
						if (obj instanceof Point2D) {
							return (Point2D) obj;
						} else if (obj instanceof VirtualNode) {
							return ((VirtualNode) obj).getPosition();
						} else if (obj instanceof PortView) {
							VertexView vertex = (VertexView) ((CellView) obj).getParentView();
							if (vertex != null) {
								return vertex.getCenterPoint();
							}
						}
						return null;
					}

					public Point2D getPoint(int index) {
						Object obj = points.get(index);
						if (obj instanceof PortView) {
							return ((PortView) obj).getLocation(this, getPointLocation((index == 0) ? 1
									: points.size() - 2));
						} else if (obj instanceof VirtualNode) {
							return ((VirtualNode) obj).getPosition();
						} else if (obj instanceof CellView) {
							System.err.println("CellView!");
							Rectangle2D r = ((CellView) obj).getBounds();
							return getAttributes().createPoint(r.getX(), r.getY());
						} else if (obj instanceof Point2D)
							// Regular Point
							return (Point2D) obj;
						return null;

					}

					public CellHandle getHandle(GraphContext context) {
						return new EdgeHandle(this, context) {
							public void paint(Graphics g) {
								invalidate();
								return;
							}
						};
					}
				};
			}

			/*
			 * @see org.jgraph.JGraph#createVertexView(org.jgraph.JGraph,
			 *      org.jgraph.graph.CellMapper, java.lang.Object)
			 */
			protected VertexView createVertexView(Object cell) {
				return new VertexView(cell) {
					/*
					 * @see org.jgraph.graph.AbstractCellView#updateGroupBounds()
					 */
					protected void updateGroupBounds() {
						Rectangle2D r = getBounds(getChildViews());
						Insets insets = GraphConstants.getBorder(getAllAttributes()).getBorderInsets(
								WorkflowEditor.this);
						r.setFrame(r.getX() - insets.left, r.getY() - insets.top, r.getWidth() + insets.left
								+ insets.right, r.getHeight() + insets.top + insets.bottom);
						groupBounds = r;
					}
				};
			}
		});
		WorkflowEdgeRenderer edgeRenderer = new WorkflowEdgeRenderer();
		edgeRenderer.setTension((float) 0.6);
		EdgeView.renderer = edgeRenderer;
		VertexView.renderer = new VertexRenderer() {
			// Change to non 1 value to show only part of the progress bar and
			// leave
			// the remaining space coloured by processor type.
			int progressBarDivide = 1;

			int progress = -2;

			int workers = 0;

			Color background2 = Color.WHITE;

			Color background3 = Color.WHITE;

			Color background4 = Color.WHITE;

			public void paint(Graphics g) {
				setOpaque(false);
				Graphics2D g2d = (Graphics2D) g;
				Paint oldPaint = g2d.getPaint();

				if (!DefaultGraphModel.isGroup(getScuflGraphModel(), view.getCell())) {
					g2d.setPaint(new GradientPaint(0, 0, getBackground(), getWidth(), getHeight(), ShadedLabel
							.halfShade(getBackground())));
					g2d.fillRect(0, 0, getWidth(), getHeight());

					if (progress > -1 && progress < 101) {
						// progress is integer 0-100 where 0 is started, 100
						// completed
						int newWidth = ((getWidth() * progress) / 100);
						int completedWidth = (getWidth() * (progress - workers)) / 100;
						int remainingWidth = getWidth() - newWidth;
						g2d.setPaint(new GradientPaint(0, 0, background3, getWidth(), getHeight(), ShadedLabel
								.halfShade(background3)));
						g2d.fillRect(0, 0, newWidth, getHeight() / progressBarDivide);
						g2d.setPaint(new GradientPaint(0, 0, background2, getWidth(), getHeight(), ShadedLabel
								.halfShade(background2)));
						g2d.fillRect(newWidth, 0, remainingWidth, getHeight() / progressBarDivide);
						if (completedWidth > 0) {
							g2d.setPaint(new GradientPaint(0, 0, background4, getWidth(), getHeight(), ShadedLabel
									.halfShade(background4)));
							g2d.fillRect(0, 0, completedWidth, getHeight() / progressBarDivide);
						}
					} else if (progress == -1) {
						g2d.setPaint(new GradientPaint(0, 0, background3, getWidth(), getHeight(),
								org.embl.ebi.escience.scuflui.ShadedLabel.halfShade(background3)));
						g2d.fillRect(0, 0, getWidth(), getHeight() / progressBarDivide);
					}
				}

				if (selected) {
					Color hl = getHighlightColor();
					g2d.setPaint(new Color(hl.getRed(), hl.getGreen(), hl.getBlue(), 100));
					g2d.fillRect(0, 0, getWidth(), getHeight());
				}

				g2d.setPaint(oldPaint);
				super.paint(g);
			}

			protected void installAttributes(CellView view) {
				super.installAttributes(view);
				Map map = view.getAllAttributes();
				if (map.containsKey("progress")) {
					String progressStringValue = (String) map.get("progress");
					progress = Integer.parseInt(progressStringValue);
				} else {
					progress = -2;
				}
				if (map.containsKey("workers")) {
					String wstring = (String) map.get("workers");
					workers = Integer.parseInt(wstring);
				} else {
					workers = 0;
				}
				if (map.containsKey("statuscolour1")) {
					background3 = (Color) map.get("statuscolour1");
				} else {
					background3 = Color.WHITE;
				}
				if (map.containsKey("statuscolour2")) {
					background2 = (Color) map.get("statuscolour2");
				} else {
					background2 = Color.WHITE;
				}
				if (map.containsKey("statuscolour3")) {
					background4 = (Color) map.get("statuscolour3");
				} else {
					background4 = Color.WHITE;
				}
			}
		};
		setUI(new BasicGraphUI() {
			protected GraphModelListener createGraphModelListener() {
				return new GraphModelHandler() {
					public void graphChanged(GraphModelEvent e) {
						super.graphChanged(e);
						layoutManager.layout(e.getChange());
					}
				};
			}
		});
		setMarqueeHandler(new MarqueeHandler());
		setAntiAliased(true);
		setBendable(true);
		setMoveable(false);
		setSizeable(false);
		setGridVisible(false);
		setHighlightColor(UIManager.getColor("Tree.selectionBackground"));
		GraphConstants.SELECTION_STROKE = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE && isEditable()) {
					Object[] selected = getSelectionCells();
					getModel().remove(selected);
				}
			}
		});
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				// Zoom zoom
				setScale(getScale() + (e.getWheelRotation() / 10.0));
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				showPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				showPopup(e);
			}

			private void showPopup(MouseEvent event) {
				if (event.isPopupTrigger() && isEditable()) {
					Object[] selected = getSelectionCells();
					if (selected != null) {
						if (selected.length == 1) {
							Object scuflObject = selected[0];
							try {
								JPopupMenu menu = ScuflContextMenuFactory.getMenuForObject(null, scuflObject, model);
								if (scuflObject instanceof Processor) {
									if (scuflObject == model.getWorkflowSinkProcessor()) {
										menu = new JPopupMenu();
										menu.add(new ShadedLabel("Workflow Outputs", ShadedLabel.TAVERNA_GREEN));
										menu.add(new AddOutputAction(model));
									} else if (scuflObject == model.getWorkflowSourceProcessor()) {
										menu = new JPopupMenu();
										menu.add(new ShadedLabel("Workflow Inputs", ShadedLabel.TAVERNA_GREEN));
										menu.add(new AddInputAction(model));
									} else {
										Processor processor = (Processor) scuflObject;
										Port[] ports = processor.getPorts();
										if (ports.length > 0) {
											JMenu linkMenu = new JMenu("Start link from...");
											linkMenu.setIcon(ScuflIcons.dataLinkIcon);
											ports = processor.getInputPorts();
											if (ports.length > 0) {
												linkMenu.add(new ShadedLabel("Inputs", ShadedLabel.TAVERNA_GREEN));
												linkMenu.addSeparator();
												for (int index = 0; index < ports.length; index++) {
													linkMenu.add(new StartLinkAction(model, ports[index]));
												}
												linkMenu.addSeparator();
											}
											ports = processor.getOutputPorts();
											if (ports.length > 0) {
												linkMenu.add(new ShadedLabel("Outputs", ShadedLabel.TAVERNA_ORANGE));
												linkMenu.addSeparator();
												for (int index = 0; index < ports.length; index++) {
													linkMenu.add(new StartLinkAction(model, ports[index]));
												}
											}
											menu.add(linkMenu);
										}
									}
								} else if (scuflObject instanceof Port) {
									menu.add(new StartLinkAction(model, (Port) scuflObject));
								}
								menu.show(WorkflowEditor.this, event.getX(), event.getY());
							} catch (NoContextMenuFoundException ncmfe) {
								// just means that there wasn't a suitable menu
								// for the selected node.
							}
						} else if (selected.length > 1) {
							JPopupMenu menu = new JPopupMenu();
							menu.add(new RemoveAction(model, selected));
							menu.show(WorkflowEditor.this, event.getX(), event.getY());
						} else {
							JPopupMenu menu = new JPopupMenu();
							menu.add(new ShadedLabel("Workflow", ShadedLabel.TAVERNA_GREEN));
							menu.addSeparator();
							if (isEditable()) {
								menu.add(new AddInputAction(model));
								menu.add(new AddOutputAction(model));
							}
							JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Show Boring");
							menuItem.setModel(new ShowBoringModel());

							menu.add(menuItem);
							menu.show(WorkflowEditor.this, event.getX(), event.getY());
						}
					}
				} else {
					// TODO If adding edge
				}
			}
		});
		new DropTarget(this, new DropTargetAdapter() {
			/*
			 * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
			 */
			public void dragEnter(DropTargetDragEvent dtde) {
				int action = dtde.getDropAction();
				if (dtde.isDataFlavorSupported(SpecFragmentTransferable.factorySpecFragmentFlavor) && isEditable()) {
					dtde.acceptDrag(action);
				} else {
					dtde.rejectDrag();
				}
			}

			/*
			 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
			 */
			public void drop(DropTargetDropEvent dtde) {
				try {
					DataFlavor f = SpecFragmentTransferable.factorySpecFragmentFlavor;
					Transferable t = dtde.getTransferable();
					if (dtde.isDataFlavorSupported(f) && isEditable()) {
						// Have something of type factorySpecFragmentFlavor;
						FactorySpecFragment fsf = (FactorySpecFragment) t.getTransferData(f);
						String validName = model.getValidProcessorName(fsf.getFactoryNodeName());
						Element wrapperElement = new Element("wrapper");
						wrapperElement.addContent(fsf.getElement());

						Processor newProcessor = ProcessorHelper.loadProcessorFromXML(wrapperElement, model, validName);
						model.addProcessor(newProcessor);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		getScuflGraphModel().attachToModel(model);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#detachFromModel()
	 */
	public void detachFromModel() {
		((ScuflGraphModel) getModel()).detachFromModel();
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#getIcon()
	 */
	public ImageIcon getIcon() {
		return ScuflIcons.windowDiagram;
	}

	/*
	 * @see java.awt.Component#getName()
	 */
	public String getName() {
		return "Workflow Editor (BETA)";
	}

	public void updateAutoSize(CellView view) {
		if (!view.isLeaf()) {
			CellView[] children = view.getChildViews();
			Rectangle2D rect = GraphConstants.getBounds(children[0].getAllAttributes());
			double x = rect.getMaxX() + 10;
			double y = rect.getY();
			for (int index = 1; index < children.length; index++) {
				Rectangle2D childRect = GraphConstants.getBounds(children[index].getAllAttributes());
				childRect.setFrame(x, y, childRect.getWidth(), childRect.getHeight());
				x += childRect.getWidth() + 10;
				children[index].update();
			}
		} else {
			super.updateAutoSize(view);
		}
	}

	public void updateStatus(String status) {
		Element processorList;
		try {
			SAXBuilder builder = new SAXBuilder(false);
			Document document = builder.build(new StringReader(status));
			processorList = document.getRootElement().getChild("processorList");
			Map changes = new HashMap();
			for (Iterator i = processorList.getChildren("processor").iterator(); i.hasNext();) {
				Element processorElement = (Element) i.next();
				Processor processor = ((ScuflGraphModel) getModel()).getModel().locateProcessor(
						processorElement.getAttributeValue("name"));
				Map attributes = graphModel.getAttributes(processor);
				if (attributes != null) {
					// Get the first child of the processor element.
					List childElementList = processorElement.getChildren();
					if (childElementList.isEmpty() == false) {
						Element firstChildElement = (Element) childElementList.get(0);
						Color statusColour = getStatusColour(firstChildElement.getName());
						Color existing = GraphConstants.getBackground(attributes);
						boolean iterating = (firstChildElement.getName().equalsIgnoreCase("invokingwithiteration"));
						if (statusColour != existing || iterating) {
							if (!iterating) {
								Map newColour = new HashMap();
								// Insert progress cancellation - should fix
								// partial colour bars after iterations
								newColour.put("progress", "-1");
								newColour.put("statuscolour1", statusColour);
								// GraphConstants.setBackground(newColour,
								// statusColour);
								changes.put(processor, newColour);
							} else {
								int iterationNumber = Integer.parseInt(firstChildElement
										.getAttributeValue("IterationNumber"));
								int iterationTotal = Integer.parseInt(firstChildElement
										.getAttributeValue("IterationTotal"));
								int activeWorkers = Integer.parseInt(firstChildElement
										.getAttributeValue("ActiveWorkers"));
								int progress = (100 * iterationNumber) / iterationTotal;
								int running = (100 * activeWorkers) / iterationTotal;

								// System.out.println("Progress : " + progress);
								Map newStuff = new HashMap();
								newStuff.put("progress", progress + "");
								newStuff.put("workers", running + "");
								// Iterations launched
								newStuff.put("statuscolour1", GraphColours.getColour("gold", Color.GREEN));
								// Iterations still to go
								newStuff.put("statuscolour2", GraphColours.getColour("medium purple", Color.MAGENTA));
								// Iterations completed
								newStuff.put("statuscolour3", GraphColours.getColour("medium sea green", Color.GREEN));
								// GraphConstants.setBackground(newStuff,GraphColours.getColour("gold",
								// Color.GREEN));
								changes.put(processor, newStuff);
							}
						}
					}
				}
			}
			if (!changes.isEmpty()) {
				graphModel.edit(changes, null, null, null);
			}
		} catch (Exception e) {
			// TODO Handle exceptions
			e.printStackTrace();
		}
	}

	private Color getStatusColour(String status) {
		if (status.equals("ProcessComplete")) {
			return GraphColours.getColour("medium sea green", Color.GREEN);
		} else if (status.equals("ServiceError") || status.equals("ServiceFailure")) {
			return GraphColours.getColour("firebrick1", Color.RED);
		} else if (status.startsWith("Invoking")) {
			return GraphColours.getColour("medium purple", Color.MAGENTA);
		}
		return GraphColours.getColour("light gray", Color.GRAY);
	}
}
