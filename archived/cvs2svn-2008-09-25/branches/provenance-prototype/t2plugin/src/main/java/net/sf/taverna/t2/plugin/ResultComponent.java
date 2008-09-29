package net.sf.taverna.t2.plugin;

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
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.impl.WorkflowInstanceFacadeImpl;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

/**
 * Display workflow results in a tree format and provide capability to render
 * using a {@link Renderer} appropriate to the MIME type
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * @author David Withers
 * @author Stuart Owen
 * 
 */
public class ResultComponent extends JPanel implements UIComponentSPI {

	private static final long serialVersionUID = 2829637398174474092L;

	private JTabbedPane tabbedPane;

	private Map<String, JComponent> componentMap = new HashMap<String, JComponent>();

	List<ResultListener> resultListeners = new ArrayList<ResultListener>();

	private Map<String, List<String>> mimeTypes;

	private InvocationContext context;

	public ResultComponent() {
		setLayout(new BorderLayout());
		add(new JLabel("Results"), BorderLayout.NORTH);

		tabbedPane = new JTabbedPane();
		add(tabbedPane, BorderLayout.CENTER);

	}

	/**
	 * Registers the {@link WorkflowInstanceFacade} which will be used to get
	 * the {@link DataflowOutputPort} for this workflow. Sets up the
	 * {@link ResultTreeModel} which will be displayed and the
	 * {@link RendererPopup} which the user can get from right click to render
	 * the results
	 * 
	 * @param facade
	 * @throws EditException
	 */
	public void register(WorkflowInstanceFacadeImpl facade)
			throws EditException {
		clear();
		List<? extends DataflowOutputPort> dataflowOutputPorts = facade
				.getDataflow().getOutputPorts();
		for (DataflowOutputPort dataflowOutputPort : dataflowOutputPorts) {
			String portName = dataflowOutputPort.getName();
			JComponent outputPanel = new JPanel(new BorderLayout());
			outputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			componentMap.put(portName, outputPanel);
			tabbedPane.add(portName, outputPanel);
			ResultTreeModel resultModel = new ResultTreeModel(portName,
					dataflowOutputPort.getDepth(), mimeTypes);
			resultListeners.add(resultModel);
			final JTree tree = new JTree(resultModel);
			// add the renderer popup
			tree.addMouseListener(new RendererPopup(tree, new DataFacade(
					context.getDataManager())));
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

	public void deregister(WorkflowInstanceFacadeImpl facade) {
		for (ResultListener listener : resultListeners) {
			facade.removeResultListener(listener);
		}
	}

	public ImageIcon getIcon() {
		return null;
	}

	public void onDisplay() {
	}

	public void onDispose() {
	}

	public void clear() {
		tabbedPane.removeAll();
		componentMap.clear();
	}

	public void setOutputMimeTypes(Map<String, List<String>> mimeTypeMap) {
		this.mimeTypes = mimeTypeMap;
	}

	public void setContext(InvocationContext context) {
		this.context = context;
	}

}
