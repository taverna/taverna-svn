package net.sf.taverna.t2.workbench.views.results;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.impl.WorkflowInstanceFacadeImpl;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

public class ResultViewComponent extends JSplitPane implements UIComponentSPI {

	List<ResultListener> resultListeners = new ArrayList<ResultListener>();
	private RenderedResultComponent renderedResultComponent;
//	private Map<String, JComponent> componentMap = new HashMap<String, JComponent>();
	private JTabbedPane tabbedPane;
//	private InvocationContext context;
	private Map<String, List<String>> mimeTypes = new HashMap<String, List<String>>();

	public ResultViewComponent() {
		super(JSplitPane.HORIZONTAL_SPLIT);
		init();
	}

	private void init() {
		renderedResultComponent = new RenderedResultComponent();
//		add(new JLabel("Results"), BorderLayout.NORTH);
		tabbedPane = new JTabbedPane();
		setTopComponent(tabbedPane);
		setBottomComponent(renderedResultComponent);
		setDividerLocation(400);
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Results View Component";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

	public void register(WorkflowInstanceFacade facade)
			throws EditException {
		clear();
//		// FIXME do using annotations on the data references
//		setMimeTypes(facade.getDataflow().getProcessors());

		List<? extends DataflowOutputPort> dataflowOutputPorts = facade
				.getDataflow().getOutputPorts();
		for (DataflowOutputPort dataflowOutputPort : dataflowOutputPorts) {
			String portName = dataflowOutputPort.getName();
			JComponent outputPanel = new JPanel(new BorderLayout());
			outputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
//			componentMap.put(portName, outputPanel);
			tabbedPane.add(portName, outputPanel);
			ResultTreeModel resultModel = new ResultTreeModel(portName,
					dataflowOutputPort.getDepth(), mimeTypes);
			resultListeners.add(resultModel);
			final JTree tree = new JTree(resultModel);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					Object selectedObject = e.getPath().getLastPathComponent();
					if (selectedObject instanceof ResultTreeNode) {
						renderedResultComponent.setResult((ResultTreeNode) selectedObject);
					}
				}		
			});
			tree.setExpandsSelectedPaths(true);
			tree.setLargeModel(true);
			resultModel.addTreeModelListener(new TreeModelListener() {

				public void treeNodesChanged(TreeModelEvent e) {
					tree.expandPath(e.getTreePath());
					tree.scrollPathToVisible(e.getTreePath());
				}

				public void treeNodesInserted(TreeModelEvent e) {
				}

				public void treeNodesRemoved(TreeModelEvent e) {
				}

				public void treeStructureChanged(TreeModelEvent e) {
				}
			});
			outputPanel.add(new JScrollPane(tree));
			facade.addResultListener(resultModel);
		}
		revalidate();
	}

//	public void setContext(InvocationContext context) {
//		this.context = context;
//	}

	public void clear() {
		tabbedPane.removeAll();
//		componentMap.clear();
	}

//	/**
//	 * Set the mime types for each processor in a dataflow. Get the output port
//	 * name and if it is an {@link ActivityOutputPortDefinitionBean} grab the
//	 * list of mime types. Otherwise set it to be null for the moment (the
//	 * {@link RendererPopup} will work some mime magic to get the mime type for
//	 * these ones)
//	 * 
//	 * @param list
//	 */
//	private void setMimeTypes(List<? extends Processor> list) {
//		for (Processor processor : list) {
//			List<? extends ProcessorOutputPort> outputPorts = processor
//					.getOutputPorts();
//			for (ProcessorOutputPort outputPort : outputPorts) {
//				String name2 = outputPort.getName();
//				if (outputPort.getClass().getSimpleName().equalsIgnoreCase(
//						"ActivityOutputPortDefinitionBean")) {
//					System.out
//							.println("its an activity output port bean for port "
//									+ name2);
//					List<String> mimeTypes2 = ((ActivityOutputPortDefinitionBean) outputPort)
//							.getMimeTypes();
//					mimeTypes.put(name2, mimeTypes2);
//				} else {
//					// we don't know what kind of mime type so add null for the
//					// moment
//					mimeTypes.put(name2, null);
//				}
//			}
//		}
//	}

}
