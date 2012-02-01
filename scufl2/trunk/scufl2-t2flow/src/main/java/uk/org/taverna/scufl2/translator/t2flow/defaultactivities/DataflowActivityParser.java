package uk.org.taverna.scufl2.translator.t2flow.defaultactivities;

import java.net.URI;

import uk.org.taverna.scufl2.api.common.URITools;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.io.ReaderException;
import uk.org.taverna.scufl2.translator.t2flow.ParserState;
import uk.org.taverna.scufl2.translator.t2flow.T2FlowParser;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.ConfigBean;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.DataflowConfig;

public class DataflowActivityParser extends AbstractActivityParser {

	private URITools uriTools = new URITools();
	
	private static URI activityRavenURI = T2FlowParser.ravenURI
			.resolve("net.sf.taverna.t2.activities/dataflow-activity/");

	private static String activityClassName = "net.sf.taverna.t2.activities.dataflow.DataflowActivity";

	public static URI nestedUri = URI
			.create("http://ns.taverna.org.uk/2010/activity/nested-workflow");

	@Override
	public boolean canHandlePlugin(URI activityURI) {
		String activityUriStr = activityURI.toASCIIString();
		return activityUriStr.startsWith(activityRavenURI.toASCIIString())
				&& activityUriStr.endsWith(activityClassName);
	}

	@Override
	public URI mapT2flowRavenIdToScufl2URI(URI t2flowActivity) {
		return nestedUri;
	}

	@Override
	public Configuration parseConfiguration(T2FlowParser t2FlowParser,
			ConfigBean configBean, ParserState parserState) throws ReaderException {
		DataflowConfig dataflowConfig = unmarshallConfig(t2FlowParser,
				configBean, "dataflow", DataflowConfig.class);
		Configuration configuration = new Configuration();
		configuration.setConfigurableType(nestedUri.resolve("#Config"));		

		String wfId = dataflowConfig.getRef();
		URI wfUri = Workflow.WORKFLOW_ROOT.resolve(wfId + "/");
		Workflow wf = (Workflow) getUriTools().resolveUri(wfUri, parserState.getCurrentWorkflowBundle());		
		URI uri = getUriTools().relativeUriForBean(wf, parserState.getCurrentProfile());

		configuration.getPropertyResource().addPropertyReference(nestedUri.resolve("#workflow"), uri);		
		return configuration;
	}

	public void setUriTools(URITools uriTools) {
		this.uriTools = uriTools;
	}

	public URITools getUriTools() {
		return uriTools;
	}

}
