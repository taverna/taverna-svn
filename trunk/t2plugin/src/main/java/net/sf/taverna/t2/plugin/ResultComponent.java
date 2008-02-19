package net.sf.taverna.t2.plugin;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.impl.WorkflowInstanceFacadeImpl;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class ResultComponent extends JPanel implements UIComponentSPI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2829637398174474092L;

	private JTabbedPane tabbedPane;

	private Map<String, JComponent> componentMap = new HashMap<String, JComponent>();

	List<ResultListener> resultListeners = new ArrayList<ResultListener>();

	private Map<String, String> mimeTypes;

	private InvocationContext context;
	
	public ResultComponent() {
		setLayout(new BorderLayout());
		add(new JLabel("Results"), BorderLayout.NORTH);

		tabbedPane = new JTabbedPane();
		add(tabbedPane, BorderLayout.CENTER);
	}

	public void register(WorkflowInstanceFacadeImpl facade)
			throws EditException {
		clear();
		List<? extends DataflowOutputPort> dataflowOutputPorts = facade
				.getDataflow().getOutputPorts();
		for (DataflowOutputPort dataflowOutputPort : dataflowOutputPorts) {
			String portName=dataflowOutputPort.getName();
			JComponent outputPanel = new JPanel(new BorderLayout());
			outputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			componentMap.put(portName, outputPanel);
			tabbedPane.add(portName, outputPanel);
//			final JPanel resultRenderer = new JPanel();
//			outputPanel.add(resultRenderer);
			ResultTreeModel resultModel = new ResultTreeModel(portName,dataflowOutputPort.getDepth(), mimeTypes);
			resultListeners.add(resultModel);
			final JTree tree = new JTree(resultModel);
			tree.addTreeSelectionListener(new TreeSelectionListener() {

				public void valueChanged(TreeSelectionEvent e) {
					Object lastSelectedPathComponent = tree.getLastSelectedPathComponent();
					if (lastSelectedPathComponent instanceof ResultTreeNode) {
						String mimeType = ((ResultTreeNode) lastSelectedPathComponent).getMimeType();
						RendererRegistry rendererRegistry = new RendererRegistry();
						System.out.println("Mime type for translation: " + mimeType);
						EntityIdentifier token = ((ResultTreeNode)lastSelectedPathComponent).getToken();
						DataFacade dataFacade = new DataFacade(context.getDataManager());
						List<Renderer> renderersForMimeType = rendererRegistry.getRenderersForMimeType(dataFacade, token, mimeType);
						if (renderersForMimeType.isEmpty()) {
							JOptionPane.showMessageDialog(ResultComponent.this,
									"Unable to display for mime type " + mimeType, "Unable to render",
									JOptionPane.WARNING_MESSAGE);
						} else {
							//pop up the renderer in a frame
//							JOptionPane.showMessageDialog(ResultComponent.this,
//									"Here is some stuff for " + mimeType, "Rendering",
//									JOptionPane.WARNING_MESSAGE);
							System.out.println("result time for " + token);
//							resultRenderer.removeAll();
//							resultRenderer.add(renderersForMimeType.get(0).getComponent(token, dataFacade));
//							resultRenderer.setVisible(true);
							JDialog resultDialog = new JDialog();
							JPanel rendererPanel = new JPanel();
							resultDialog.add(rendererPanel);
							//cheat and use first renderer!! Maybe loop through and find first one that works?
							rendererPanel.add(renderersForMimeType.get(0).getComponent(token, dataFacade));
							resultDialog.setSize(300, 100);
							resultDialog.setName(mimeType);
							resultDialog.setVisible(true);
						}
					}
				}
				
			});
			tree.setExpandsSelectedPaths(true);
			tree.setLargeModel(true);
			resultModel.addTreeModelListener(new TreeModelListener() {

				public void treeNodesChanged(TreeModelEvent e) {
					tree.expandPath(e.getTreePath());
					tree.scrollPathToVisible(e.getTreePath());
					//tree.setSelectionPath(e.getTreePath());
				}

				public void treeNodesInserted(TreeModelEvent e) {
					// TODO Auto-generated method stub
					
				}

				public void treeNodesRemoved(TreeModelEvent e) {
					// TODO Auto-generated method stub
					
				}

				public void treeStructureChanged(TreeModelEvent e) {
					// TODO Auto-generated method stub
					
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
		// TODO Auto-generated method stub
		return null;
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

	public void clear() {
		tabbedPane.removeAll();
		componentMap.clear();
	}
	
	public void setOutputMimeTypes(Map<String, String> mimeTypes) {
		this.mimeTypes = mimeTypes;
	}

	public void setContext(InvocationContext context) {
		this.context = context;
	}

}
