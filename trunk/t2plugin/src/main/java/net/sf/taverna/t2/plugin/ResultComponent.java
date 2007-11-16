package net.sf.taverna.t2.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.gui.entity.viewer.EntityViewer;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.impl.WorkflowInstanceFacadeImpl;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.impl.ContextManager;

import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class ResultComponent extends JPanel implements UIComponentSPI {

	private JTabbedPane tabbedPane;

	private Map<String, JComponent> componentMap = new HashMap<String, JComponent>();

	private ResultListener resultListener;

	public ResultComponent() {
		setLayout(new BorderLayout());
		add(new JLabel("Results"), BorderLayout.NORTH);

		tabbedPane = new JTabbedPane();
		add(tabbedPane, BorderLayout.CENTER);

		resultListener = new ResultListener() {

			public void resultTokenProduced(EntityIdentifier data, int[] index,
					String port, String owningProcess) {
				if (index.length == 0) {
					try {
						JComponent component = componentMap.get(port);
						if (component != null) {
							Component entityViewer = EntityViewer.getPanelForEntity(
									ContextManager.baseManager, data);
							component.add(entityViewer, BorderLayout.NORTH);
							component.revalidate();
						}
					} catch (RetrievalException e) {
						e.printStackTrace();
					} catch (NotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}

	public void register(WorkflowInstanceFacadeImpl facade)
			throws EditException {
		clear();
		List<? extends DataflowOutputPort> dataflowOutputPorts = facade
				.getDataflow().getOutputPorts();
		for (DataflowOutputPort dataflowOutputPort : dataflowOutputPorts) {
			JComponent outputPanel = new JPanel(new BorderLayout());
			outputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			componentMap.put(dataflowOutputPort.getName(), outputPanel);
			tabbedPane.add(dataflowOutputPort.getName(), outputPanel);
		}
		revalidate();
		facade.addResultListener(resultListener);
	}

	public void deregister(WorkflowInstanceFacadeImpl facade) {
		facade.removeResultListener(resultListener);
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

}
