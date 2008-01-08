package net.sf.taverna.feta.browser.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.feta.browser.util.VelocityRepresentation;

import org.openrdf.elmo.Entity;
import org.openrdf.elmo.sesame.roles.SesameEntity;
import org.openrdf.model.Resource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import uk.org.mygrid.mygridmobyservice.Operation;
import uk.org.mygrid.mygridmobyservice.OperationTask;
import uk.org.mygrid.mygridmobyservice.Parameter;
import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class ServiceResource extends AbstractResource {

	private ServiceDescription service;

	public ServiceResource(Context context, Request request, Response response) {
		super(context, request, response);
		String id = (String) request.getAttributes().get("id");
		service = serviceRegistry.getServiceDescription(id);
		if (!service.getHasOperations().isEmpty()) {
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

	}

	@Override
	public Representation getRepresentation(Variant variant) {
		Map<String, Object> model = makeModel();
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

		model.put("inputs", getParameterInfo(operation.getInputParameters()));
		model.put("outputs", getParameterInfo(operation.getOutputParameters()));

		VelocityRepresentation templateRepr = new VelocityRepresentation(
				"service.vm", model, MediaType.TEXT_HTML);

		return templateRepr;
	}

	private List<Map<String, Object>> getParameterInfo(Set<Parameter> parameters) {
		List<Map<String, Object>> paramInfos = new ArrayList<Map<String, Object>>();
		int paramNo = 0;
		for (Parameter param : parameters) {
			Map<String, Object> paramDesc = new HashMap<String, Object>();
			paramInfos.add(paramDesc);

			String paramName = utils.firstOf(param
					.getHasParameterNameTexts());
			if (paramName == null) {
				paramName = "_p" + paramNo++;
			}
			paramDesc.put("name", paramName);

			List<String> namespaces = utils.extractBioNames(serviceRegistry
					.getParamNamespaces(param));
			paramDesc.put("inNamespaces", namespaces);

			List<String> objectTypes = utils.extractBioNames(serviceRegistry
					.getParamObjectType(param));
			System.out.println(objectTypes);
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


}
