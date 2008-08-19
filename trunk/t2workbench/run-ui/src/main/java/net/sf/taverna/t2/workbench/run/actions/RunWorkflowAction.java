package net.sf.taverna.t2.workbench.run.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.ui.WorkflowLaunchPanel;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.reference.config.ReferenceConfiguration;
import net.sf.taverna.t2.workbench.run.DataflowRunsComponent;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;

public class RunWorkflowAction extends AbstractAction {

	private final class InvocationContextImplementation implements
			InvocationContext {
		private final ReferenceService referenceService;

		private InvocationContextImplementation(
				ReferenceService referenceService) {
			this.referenceService = referenceService;
		}

		public ReferenceService getReferenceService() {
			return referenceService;
		}

		public <T> List<? extends T> getEntities(Class<T> entityType) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RunWorkflowAction.class);

	private DataflowRunsComponent runComponent;

	private PerspectiveSPI resultsPerspective;

	public RunWorkflowAction() {
		runComponent = DataflowRunsComponent.getInstance();
		putValue(SMALL_ICON, WorkbenchIcons.runIcon);
		putValue(NAME, "Run workflow...");
		putValue(SHORT_DESCRIPTION, "Run the current workflow");

	}

	public void actionPerformed(ActionEvent e) {
		Object model = ModelMap.getInstance().getModel(
				ModelMapConstants.CURRENT_DATAFLOW);
		if (model instanceof Dataflow) {
			Dataflow dataflow = (Dataflow) model;
			XMLSerializer serialiser = new XMLSerializerImpl();
			XMLDeserializer deserialiser = new XMLDeserializerImpl();
			Dataflow dataflowCopy = null;
			try {
				dataflowCopy = deserialiser.deserializeDataflow(serialiser
						.serializeDataflow(dataflow));
			} catch (SerializationException e1) {
				logger.error("Unable to copy dataflow", e1);
			} catch (DeserializationException e1) {
				logger.error("Unable to copy dataflow", e1);
			} catch (EditException e1) {
				logger.error("Unable to copy dataflow", e1);
			}

			if (dataflowCopy != null) {
				String context = ReferenceConfiguration
						.getInstance()
						.getProperty(
								ReferenceConfiguration.REFERENCE_SERVICE_CONTEXT);
				ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
						context);
				final ReferenceService referenceService = (ReferenceService) appContext
						.getBean("t2reference.service.referenceService");
				ReferenceContext referenceContext = null;

				WorkflowInstanceFacade facade;
				try {
					facade = new EditsImpl().createWorkflowInstanceFacade(
							dataflow, new InvocationContextImplementation(
									referenceService), "");
				} catch (InvalidDataflowException ex) {
					invalidDataflow(ex.getDataflowValidationReport());
					return;
				}

				List<? extends DataflowInputPort> inputPorts = dataflowCopy
						.getInputPorts();
				if (!inputPorts.isEmpty()) {
					showInputDialog(facade, referenceContext);
				} else {
					switchToResultsPerspective();
					runComponent.runDataflow(facade, (Map) null);
				}

			} else {
				showErrorDialog("Unable to make a copy of the workflow to run",
						"Workflow copy failed");
			}
		}

	}

	private void invalidDataflow(DataflowValidationReport report) {
		StringBuilder sb = new StringBuilder();
		sb.append("Unable to validate workflow due to:");
		List<? extends TokenProcessingEntity> unsatisfiedEntities = report
				.getUnsatisfiedEntities();
		if (unsatisfiedEntities.size() > 0) {
			sb.append("\n Missing inputs or cyclic dependencies:");
			for (TokenProcessingEntity entity : unsatisfiedEntities) {
				sb.append("\n  " + entity.getLocalName());
			}
		}
		List<? extends DataflowOutputPort> unresolvedOutputs = report
				.getUnresolvedOutputs();
		if (unresolvedOutputs.size() > 0) {
			sb.append("\n Invalid or unconnected outputs:");
			for (DataflowOutputPort dataflowOutputPort : unresolvedOutputs) {
				sb.append("\n  " + dataflowOutputPort.getName());
			}
		}
		List<? extends TokenProcessingEntity> failedEntities = report
				.getFailedEntities();
		if (failedEntities.size() > 0) {
			sb.append("\n Type check failure:");
			for (TokenProcessingEntity entity : failedEntities) {
				sb.append("\n  " + entity.getLocalName());
			}
		}
		showErrorDialog(sb.toString(), "Workflow validation failed");

	}

	private void switchToResultsPerspective() {
		if (resultsPerspective == null) {
			for (PerspectiveSPI perspective : Workbench.getInstance()
					.getPerspectives().getPerspectives()) {
				if (perspective.getText().equalsIgnoreCase("results")) {
					resultsPerspective = perspective;
					break;
				}
			}
		}
		if (resultsPerspective != null) {
			ModelMap.getInstance().setModel(
					ModelMapConstants.CURRENT_PERSPECTIVE, resultsPerspective);
		}
	}

	private void showInputDialog(final WorkflowInstanceFacade facade, ReferenceContext refContext) {
		// Create and set up the window.
		JFrame frame = new JFrame("Workflow input builder");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		WorkflowLaunchPanel wlp = new WorkflowLaunchPanel(facade.getContext()
				.getReferenceService(), refContext) {
			@Override
			public void handleLaunch(Map<String, T2Reference> workflowInputs) {
				switchToResultsPerspective();
				runComponent.runDataflow(facade, workflowInputs);
			
			}
		};
		wlp.setOpaque(true); // content panes must be opaque

		for (DataflowInputPort input : facade.getDataflow().getInputPorts()) {
			wlp.addInputTab(input.getName(), input.getDepth());
		}

		frame.setContentPane(wlp);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	private void showErrorDialog(String message, String title) {
		JOptionPane.showMessageDialog(runComponent, message, title,
				JOptionPane.ERROR_MESSAGE);
	}

}
