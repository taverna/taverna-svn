package net.sf.taverna.t2.component.ui.serviceprovider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Pack;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.PackItem;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.User;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Workflow;

public class ComponentServiceProvider implements ServiceDescriptionProvider {
	
	private static final URI providerId = URI
		.create("http://example.com/2011/service-provider/component");
	
	private static Logger logger = Logger.getLogger(ComponentServiceProvider.class);
	
	MyExperimentClient myExperimentClient = new MyExperimentClient(logger);
	
	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@SuppressWarnings("unchecked")
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		// Use callback.status() for long-running searches
		// callBack.status("Resolving example services");
		
		myExperimentClient.setBaseURL("http://www.myexperiment.org");
		
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("simple_sum.t2flow");
		String content = "";
		try {
			content = IOUtils.toString(stream, "UTF-8");
		} catch (IOException e) {
			callBack.fail("Unable to read component", e);
		}


		
		if (!myExperimentClient.isLoggedIn()) {
			myExperimentClient.doLogin();
		}
		try {
		Document families;
			families = myExperimentClient.doMyExperimentGET("http://www.myexperiment.org/packs.xml?tag=component%20family").getResponseBody();
		Element root = families.getRootElement();
		for (Object packObject : root.getChildren()) {
			Element packElement = (Element) packObject;
			
				Document packDoc =
					myExperimentClient.getResource(Resource.PACK, packElement.getAttributeValue("uri"), Resource.REQUEST_ALL_DATA);
				List<ServiceDescription> results = new ArrayList<ServiceDescription>();
				Pack p = Pack.buildFromXML(packDoc, myExperimentClient, logger);
				for (PackItem pi : p.getItems()) {
					ComponentServiceDesc service = new ComponentServiceDesc();
					service.setFamilyName(p.getTitle());
					Resource memberResource = pi.getItem();
					if ((memberResource != null) && (memberResource.getItemType() == Resource.WORKFLOW)){
						Document workflowDoc = myExperimentClient.getResource(Resource.WORKFLOW, memberResource.getURI(), Resource.REQUEST_ALL_DATA);
						Workflow w = Workflow.buildFromXML(workflowDoc, logger);
						if (!w.getContentType().equals(Workflow.MIME_TYPE_TAVERNA_2)) {
							continue;
						}
						Workflow w2 = myExperimentClient.fetchWorkflowBinary(memberResource.getURI());
						service.setName(w.getTitle());
						service.setDataflowString(new String(w2.getContent(), "utf-8"));
//						service.setUrl(memberResource.getURI());
						results.add(service);
					}
				}
				callBack.partialResults(results);
	}
		} catch (Exception e) {
			callBack.fail("Unable to read component families", e);
		}

		// No more results will be coming
		callBack.finished();
	}

	/**
	 * Icon for service provider
	 */
	public Icon getIcon() {
		return ComponentServiceIcon.getIcon();
	}

	/**
	 * Name of service provider, appears in right click for 'Remove service
	 * provider'
	 */
	public String getName() {
		return "Component service";
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public String getId() {
		return providerId.toASCIIString();
	}

}
