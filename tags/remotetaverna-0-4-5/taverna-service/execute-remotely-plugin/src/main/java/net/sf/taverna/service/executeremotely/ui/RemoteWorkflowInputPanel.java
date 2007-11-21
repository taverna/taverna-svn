package net.sf.taverna.service.executeremotely.ui;

import java.util.Date;
import java.util.Map;

import javax.swing.Action;

import net.sf.taverna.service.executeremotely.RESTService;
import net.sf.taverna.service.executeremotely.UILogger;
import net.sf.taverna.service.rest.client.DataREST;
import net.sf.taverna.service.rest.client.JobREST;
import net.sf.taverna.service.rest.client.NotSuccessException;
import net.sf.taverna.service.rest.client.WorkflowREST;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.shared.UIUtils;

public class RemoteWorkflowInputPanel extends WorkflowInputPanel {

	private static Logger logger =
		Logger.getLogger(RemoteWorkflowInputPanel.class);

	private RESTService service;

	private UILogger uiLog;

	public RemoteWorkflowInputPanel(ScuflModel model, RESTService service, UILogger uiLog) {
		super(model);
		this.service = service;
		this.uiLog = uiLog;
	}

	public void init() {
		// Change the name of the "Run" action
		runAction.putValue(Action.NAME, "Run remotely");
		super.init();
	}

	@Override
	public void runWorkflow(ScuflModel model, Map<String, DataThing> inputs) {
		WorkflowREST wf;
		try {
			wf = service.uploadWorkflow(model);
		} catch (NotSuccessException e) {
			uiLog.log("Could not upload workflow " + model);
			logger.warn("Could not upload workflow " + model, e);
			return;
		}
		DataREST baclava;
		try {
			baclava = service.uploadData(inputs);
		} catch (NotSuccessException e) {
			uiLog.log("Could not upload data for workflow " + model);
			logger.warn("Could not upload data for workflow", e);
			return;
		}
		JobREST job;
		try {
			job = service.addJob(wf, baclava);
		} catch (NotSuccessException e) {
			uiLog.log("Could not add job for " + wf);
			logger.warn("Could not add job for workflow " + wf, e);
			return;
		}
		try {
			service.setJobTitle(job, model);
		} catch (NotSuccessException e) {
			uiLog.log("Could not set title for " + job);
			logger.warn("Could not set title for " + job, e);
		}
		
		uiLog.log("Added " + job);
	}

	public static void run(ScuflModel model, RESTService service, UILogger uiLog) {
		if (model.getWorkflowSourcePorts().length > 0) {
			RemoteWorkflowInputPanel panel =
				new RemoteWorkflowInputPanel(model, service, uiLog);
			UIUtils.createFrame(panel, 50, 50, 600, 600);
		} else {
			// Add the job right away as we don't need any inputs
			WorkflowREST wf;
			try {
				wf = service.uploadWorkflow(model);
			} catch (NotSuccessException e) {
				uiLog.log("Could not upload workflow " + model);
				logger.warn("Could not upload workflow " + model, e);
				return;
			}
			JobREST job;
			try {
				job = service.addJob(wf);
			} catch (NotSuccessException e) {
				uiLog.log("Could not add job for " + wf);
				logger.warn("Could not add job for " + wf, e);
				return;
			}
			uiLog.log("Added " + job);
			try {
				service.setJobTitle(job, model);
			} catch (NotSuccessException e) {
				uiLog.log("Could not set title for " + job);
				logger.warn("Could not set title for " + job, e);
			}
		}
	}

}
