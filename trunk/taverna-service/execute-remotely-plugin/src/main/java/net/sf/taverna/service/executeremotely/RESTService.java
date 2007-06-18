package net.sf.taverna.service.executeremotely;

import java.util.Map;

import net.sf.taverna.service.rest.client.DataREST;
import net.sf.taverna.service.rest.client.JobREST;
import net.sf.taverna.service.rest.client.NotSuccessException;
import net.sf.taverna.service.rest.client.RESTContext;
import net.sf.taverna.service.rest.client.WorkflowREST;
import net.sf.taverna.service.rest.client.WorkflowsREST;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class RESTService {
	private static Logger logger = Logger.getLogger(RESTService.class);

	private static XMLOutputter xmlOutputter =
		new XMLOutputter(Format.getCompactFormat());

	private RESTContext context;

	public RESTService(RESTContext context) {
		this.context = context;
	}

	public WorkflowREST uploadWorkflow(ScuflModel model)
		throws NotSuccessException {
		String scufl = XScuflView.getXMLText(model);
		WorkflowsREST userWfs = context.getUser().getWorkflows();
		WorkflowREST wf = userWfs.add(scufl);
		logger.info("Created new workflow " + wf);
		return wf;
	}

	public RESTContext getContext() {
		return context;
	}

	public DataREST uploadData(Map<String, DataThing> inputs)
		throws NotSuccessException {
		// From XMLUtils.makeDataDocument()
		org.jdom.Document doc = DataThingXMLFactory.getDataDocument(inputs);

		// Upload data
		String baclava = xmlOutputter.outputString(doc);
		return context.getUser().getDatas().add(baclava);

	}

	public JobREST addJob(WorkflowREST wf, DataREST inputDoc)
		throws NotSuccessException {
		return context.getUser().getJobs().add(wf, inputDoc);
	}

	public JobREST addJob(WorkflowREST wf) throws NotSuccessException {
		return context.getUser().getJobs().add(wf);
	}

}
