package net.sf.taverna.feta.browser.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Variant;

import uk.org.mygrid.mygridmobyservice.Operation;
import uk.org.mygrid.mygridmobyservice.Parameter;
import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class ServiceResource extends AbstractResource {

	private static Logger logger = Logger.getLogger(ServiceResource.class);

	private ServiceDescription service;

	public ServiceResource(Context context, Request request, Response response) {
		super(context, request, response);
		String id = (String) request.getAttributes().get("id");
		service = serviceRegistry.getServiceDescription(id);
		if (service != null) {
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}
	}

	@Override
	public String getPageTemplate() {
		return "service.vm";
	}

	@Override
	public String getPageTitle() {
		return "Service " + utils.firstOf(service.getHasServiceNameTexts());
	}

	private List<Map<String, Object>> getParameterInfo(Set<Parameter> parameters) {
		List<Map<String, Object>> paramInfos = new ArrayList<Map<String, Object>>();
		int paramNo = 0;
		for (Parameter param : parameters) {
			Map<String, Object> paramDesc = new HashMap<String, Object>();
			paramInfos.add(paramDesc);

			String paramName = utils.firstOf(param.getHasParameterNameTexts());
			if (paramName == null) {
				paramName = "_p" + paramNo++;
			}
			paramDesc.put("name", paramName);

			List<String> namespaces = utils.extractBioNames(serviceRegistry
					.getParamNamespaces(param));
			paramDesc.put("inNamespaces", namespaces);

			List<String> objectTypes = utils.extractBioNames(serviceRegistry
					.getParamObjectType(param));
			paramDesc.put("objectTypes", objectTypes);

			String description = utils.firstOf(param
					.getHasParameterDescriptionTexts());
			if (description == null) {
				description = "";
			}
			paramDesc.put("description", description);
		}
		Collections.sort(paramInfos, new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				return ((String) o1.get("name")).compareTo((String) o2
						.get("name"));
			}
		});
		return paramInfos;
	}

	@Override
	protected Map<String, Object> makeModel() {
		Map<String, Object> model = super.makeModel();
		model.put("service", service);
		model.put("provider", utils.firstOf(service.getProvidedBy()));

		Operation operation = utils.firstOf(service.getHasOperations());
		model.put("operation", operation);

		List<String> tasks = utils.extractBioNames(serviceRegistry
				.getTasksPerformedBy(operation));
		model.put("tasks", tasks);

		List<String> methods = utils.extractBioNames(serviceRegistry
				.getMethodsUsedBy(operation));
		model.put("methods", methods);

		List<String> resources = utils.extractBioNames(serviceRegistry
				.getResourcesUsedBy(operation));
		model.put("resources", resources);

		model.put("exampleWorkflow", findExampleWorkflow());

		model.put("inputs", getParameterInfo(operation.getInputParameters()));
		model.put("outputs", getParameterInfo(operation.getOutputParameters()));
		return model;
	}

	private URI findExampleWorkflow() {
		URI examplesRoot = URI
				.create("http://www.mygrid.org.uk/feta/mygrid/example_workflow/");
		URI descriptionsRoot = URI
				.create("http://www.mygrid.org.uk/feta/mygrid/descriptions/");
		String descriptionLocationStr = utils.firstOf(service
				.getHasServiceDescriptionLocations());
		if (descriptionLocationStr == null) {
			return null;
		}
		URI descriptionLocation;
		try {
			descriptionLocation = new URI(descriptionLocationStr);
		} catch (URISyntaxException e) {
			logger.warn("Invalid description location "
					+ descriptionLocationStr);
			return null;
		}
		
		URI parent = descriptionLocation.resolve(".");
		URI baseName = parent.relativize(descriptionLocation);
		String workflowName = baseName.getRawPath().replace(".xml", "_workflow.xml");
				
		URI relativeUri = descriptionsRoot.relativize(descriptionLocation);
		URI workflowUri =  examplesRoot.resolve(relativeUri).resolve(workflowName);

		// TODO: Check if it's actually there
		Request request = new Request(Method.GET, workflowUri.toASCIIString());
		Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);
		if (! response.getStatus().isSuccess()) {
			return null;
		}
		return workflowUri;

	}

}
