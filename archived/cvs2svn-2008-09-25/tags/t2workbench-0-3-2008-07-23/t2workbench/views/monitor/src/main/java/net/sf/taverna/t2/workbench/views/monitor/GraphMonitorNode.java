package net.sf.taverna.t2.workbench.views.monitor;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import net.sf.taverna.t2.monitor.MonitorNode;
import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.monitor.NoSuchPropertyException;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * A <code>MonitorNode</code> that updates an <code>Graph</code> when
 * <code>MonitorableProperty</code>s change.
 * 
 * @author David Withers
 */
public class GraphMonitorNode implements MonitorNode {

	private Processor workflowObject;

	private String[] owningProcess;

	private Set<MonitorableProperty<?>> properties;

	private boolean expired = false;

	private Date creationDate = new Date();

	private GraphController graphController;

	private String processorId;

	private int queueSize = 0;

	private int sentJobs = 0;

	private int completedJobs = 0;

	private int errors = 0;

	/**
	 * Constructs a new instance of GraphMonitorNode.
	 *
	 * @param workflowObject
	 * @param owningProcess
	 * @param properties
	 * @param graphController
	 */
	public GraphMonitorNode(Processor workflowObject,
			String[] owningProcess, Set<MonitorableProperty<?>> properties,
			GraphController graphController) {
		this.properties = properties;
		this.workflowObject = workflowObject;
		this.owningProcess = owningProcess;
		this.graphController = graphController;
		processorId = GraphMonitor.getProcessorId(owningProcess);
	}

	public void addMonitorableProperty(MonitorableProperty<?> newProperty) {
		properties.add(newProperty);
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String[] getOwningProcess() {
		return owningProcess;
	}

	public Set<? extends MonitorableProperty<?>> getProperties() {
		return Collections.unmodifiableSet(properties);
	}

	public Processor getWorkflowObject() {
		return workflowObject;
	}

	public boolean hasExpired() {
		return expired;
	}

	public void expire() {
		expired = true;
	}

	/**
	 * Updates the <code>Graph</code> when changes to
	 * <code>MonitorableProperty</code>s are detected.
	 * 
	 */
	public synchronized void update() {
		boolean queueSizeChanged = false;
		boolean sentJobsChanged = false;
		boolean completedJobsChanged = false;
		boolean errorsChanged = false;

		for (MonitorableProperty<?> property : getProperties()) {
			String[] name = property.getName();
			if (name.length == 3) {
				if (name[0].equals("dispatch")) {
					if (name[1].equals("parallelize")) {
						if (name[2].equals("queuesize")) {
							try {
								int newQueueSize = (Integer) property
										.getValue();
								newQueueSize = newQueueSize == -1 ? 0
										: newQueueSize;
								if (queueSize != newQueueSize) {
									queueSize = newQueueSize;
									queueSizeChanged = true;
								}
							} catch (NoSuchPropertyException e) {
							}
						} else if (name[2].equals("sentjobs")) {
							try {
								int newSentJobs = (Integer) property.getValue();
								if (sentJobs != newSentJobs) {
									sentJobs = newSentJobs;
									sentJobsChanged = true;
								}
							} catch (NoSuchPropertyException e) {
							}
						} else if (name[2].equals("completedjobs")) {
							try {
								int newCompletedJobs = (Integer) property
										.getValue();
								if (completedJobs != newCompletedJobs) {
									completedJobs = newCompletedJobs;
									completedJobsChanged = true;
								}
							} catch (NoSuchPropertyException e) {
							}
						}
					} else if (name[1].equals("errorbounce")) {
						if (name[2].equals("translated")) {
							try {
								int newErrors = (Integer) property
										.getValue();
								if (errors != newErrors) {
									errors = newErrors;
									errorsChanged = true;
								}
							} catch (NoSuchPropertyException e) {
							}
						}
					}
				}
			}
		}

		if (queueSizeChanged || sentJobsChanged || completedJobsChanged || errorsChanged) {
			if (completedJobsChanged) {
//				graphController.setIteration(processorId, completedJobs);
			}
			if (completedJobs > 0) {
				int totalJobs = sentJobs + queueSize;
				graphController.setNodeCompleted(processorId,
						((float) (completedJobs)) / (float) totalJobs);
			}
			if (sentJobsChanged) {
				graphController.setEdgeActive(processorId, true);
			}
			if (errorsChanged && errors > 0) {
//				graphController.setErrors(processorId, errors);				
			}
		}
	}

}
