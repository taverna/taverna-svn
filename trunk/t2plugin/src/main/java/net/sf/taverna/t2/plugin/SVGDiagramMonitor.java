package net.sf.taverna.t2.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.monitor.MonitorManager.AddPropertiesMessage;
import net.sf.taverna.t2.monitor.MonitorManager.DeregisterNodeMessage;
import net.sf.taverna.t2.monitor.MonitorManager.MonitorMessage;
import net.sf.taverna.t2.monitor.MonitorManager.RegisterNodeMessage;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.log4j.Logger;

/**
 * An implementation of the Monitor interface that updates a SVGDiagram when
 * MonitorableProperties change.
 * 
 * @author David Withers
 */
public class SVGDiagramMonitor implements Observer<MonitorMessage> {

	private static Logger logger = Logger.getLogger(SVGDiagramMonitor.class);

	private static long deregisterDelay = 1000;

	private static long monitorRate = 200;

	private SVGDiagram svgDiagram;

	private Map<String, Object> workflowObjects = new HashMap<String, Object>();

	private Map<String, SVGDiagramMonitorNode> processors = new HashMap<String, SVGDiagramMonitorNode>();

	private Map<String, ResultListener> resultListeners = new HashMap<String, ResultListener>();

	private Timer updateTimer = new Timer(true);

	private TimerTask updateTask;

	private String filter;

	public SVGDiagramMonitor() {
	}

	public void addPropertiesToNode(String[] owningProcess,
			Set<MonitorableProperty<?>> newProperties) {
		if (owningProcess[0].equals(filter)) {
			SVGDiagramMonitorNode monitorNode = processors
					.get(getProcessorId(owningProcess));
			if (monitorNode != null) {
				for (MonitorableProperty<?> property : newProperties) {
					monitorNode.addMonitorableProperty(property);
				}
			}
		}
	}

	public void deregisterNode(String[] owningProcess) {
		if (owningProcess[0].equals(filter)) {
			final String owningProcessId = getOwningProcessId(owningProcess);
			Object workflowObject = workflowObjects.remove(owningProcessId);
			if (workflowObject instanceof Processor) {
				// processors.get(getProcessorId(owningProcess)).update();
			} else if (workflowObject instanceof Dataflow) {
				if (owningProcess.length == 2) {
					// outermost dataflow finished so schedule a task to cancel
					// the update task
					if (updateTask != null) {
						updateTimer.schedule(new TimerTask() {
							public void run() {
								updateTask.cancel();
							}
						}, deregisterDelay);
					}
				}

			} else if (workflowObject instanceof WorkflowInstanceFacade) {
				final WorkflowInstanceFacade facade = (WorkflowInstanceFacade) workflowObject;
				updateTimer.schedule(new TimerTask() {
					public void run() {
						facade.removeResultListener(resultListeners
								.remove(owningProcessId));
					}
				}, deregisterDelay);
			}
		}
	}

	public void registerNode(Object workflowObject, String[] owningProcess,
			Set<MonitorableProperty<?>> properties) {
		if (filter == null && owningProcess.length == 1) {
			filter = owningProcess[0];
		}
		if (owningProcess[0].equals(filter)) {
			String owningProcessId = getOwningProcessId(owningProcess);
			workflowObjects.put(owningProcessId, workflowObject);
			if (workflowObject instanceof Processor) {
				Processor processor = (Processor) workflowObject;
				SVGDiagramMonitorNode monitorNode = new SVGDiagramMonitorNode(
						processor, owningProcess, properties, svgDiagram);
				processors.put(getProcessorId(owningProcess), monitorNode);
			} else if (workflowObject instanceof Dataflow) {
				if (owningProcess.length == 2) {
					updateTask = new TimerTask() {
						public void run() {
							for (SVGDiagramMonitorNode node : processors
									.values()) {
								node.update();
							}
						}
					};
					updateTimer.schedule(updateTask, monitorRate, monitorRate);
				}
			} else if (workflowObject instanceof WorkflowInstanceFacade) {
				WorkflowInstanceFacade facade = (WorkflowInstanceFacade) workflowObject;
				ResultListener resultListener = new MonitorResultListener(
						getProcessorId(owningProcess));
				facade.addResultListener(resultListener);
				resultListeners.put(owningProcessId, resultListener);
			}
		}
	}

	public void setDiagram(SVGDiagram diagram) {
		svgDiagram = diagram;
	}

	/**
	 * Calculates the id that will identify the box on the diagram that
	 * represents the processor.
	 * 
	 * @param owningProcess
	 *            the owning process id for a processor
	 * @return the id that will identify the box on the diagram that represents
	 *         the processor
	 */
	public static String getProcessorId(String[] owningProcess) {
		StringBuffer sb = new StringBuffer();
		for (String process : owningProcess) {
			if (!process.startsWith("facade")
					&& !process.startsWith("dataflow")
					&& !process.startsWith("invocation")) {
				sb.append(process);
			}
		}
		return sb.toString();
	}

	/**
	 * Converts the owning process array to a string.
	 * 
	 * @param owningProcess
	 *            the owning process id
	 * @return the owning process as a string
	 */
	private String getOwningProcessId(String[] owningProcess) {
		StringBuffer sb = new StringBuffer();
		for (String string : owningProcess) {
			sb.append(string);
		}
		return sb.toString();
	}

	class MonitorResultListener implements ResultListener {

		private String context;

		public MonitorResultListener(String context) {
			this.context = context;
		}

		public void resultTokenProduced(WorkflowDataToken token, String portName) {
			svgDiagram.fireDatalink(context + "WORKFLOWINTERNALSINK_"
					+ portName);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void notify(Observable<MonitorMessage> sender, MonitorMessage message)
			throws Exception {
		if (message instanceof RegisterNodeMessage) {
			RegisterNodeMessage regMessage = (RegisterNodeMessage) message;
			registerNode(regMessage.getWorkflowObject(), regMessage
					.getOwningProcess(), regMessage.getProperties());
		} else if (message instanceof DeregisterNodeMessage) {
			deregisterNode(message.getOwningProcess());
		} else if (message instanceof AddPropertiesMessage) {
			AddPropertiesMessage addMessage = (AddPropertiesMessage) message;
			addPropertiesToNode(addMessage.getOwningProcess(), addMessage
					.getNewProperties());
		} else {
			logger.warn("Unknown message " + message + " from " + sender);
		}
	}

}
