package uk.org.taverna.scufl2.api.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.org.taverna.scufl2.api.common.Visitor.VisitorWithPath;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.BlockingControlLink;
import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.dispatchstack.DispatchStack;
import uk.org.taverna.scufl2.api.dispatchstack.DispatchStackLayer;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyNode;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyStack;
import uk.org.taverna.scufl2.api.port.InputPort;
import uk.org.taverna.scufl2.api.port.OutputPort;
import uk.org.taverna.scufl2.api.port.Port;
import uk.org.taverna.scufl2.api.port.ProcessorPort;
import uk.org.taverna.scufl2.api.profiles.ProcessorPortBinding;
import uk.org.taverna.scufl2.api.property.PropertyResource;

public class URITools {

	private static final String MERGE_POSITION = "mergePosition";
	private static final String TO = "to";
	private static final String FROM = "from";
	private static final String DATALINK = "datalink";
	private static final URI DOT = URI.create(".");

	public URI relativePath(URI base, URI uri) {

		URI root = base.resolve("/");
		if (!root.equals(uri.resolve("/"))) {
			// Different protocol/host/auth
			return uri;
		}
		base = base.normalize();
		uri = uri.normalize();
		if (base.resolve("#").equals(uri.resolve("#"))) {
			// Same path, easy
			return base.relativize(uri);
		}

		if (base.isAbsolute()) {
			// Ignore hostname and protocol
			base = root.relativize(base).resolve(".");
			uri = root.relativize(uri);
		}
		// Pretend they start from /
		base = root.resolve(base).resolve(".");
		uri = root.resolve(uri);

		URI candidate = base.relativize(uri);
		URI relation = DOT;
		while (candidate.getPath().startsWith("/")
				&& !(base.getPath().isEmpty() || base.getPath().equals("/"))) {
			base = base.resolve("../");
			relation = relation.resolve("../");
			candidate = base.relativize(uri);
		}
		// Add the ../.. again
		URI resolved = relation.resolve(candidate);
		return resolved;

	}

	public URI relativeUriForBean(WorkflowBean bean, WorkflowBean relativeToBean) {
		URI rootUri = uriForBean(relativeToBean);
		URI beanUri = uriForBean(bean);
		return relativePath(rootUri, beanUri);
	}

	public WorkflowBean resolveUri(URI uri, WorkflowBundle wfBundle) {

		// Check if it's a workflow URI
		String rel = Workflow.WORKFLOW_ROOT.relativize(uri).toASCIIString();
		if (rel.matches("[0-9a-f-]+/")) {
			for (Workflow wf : wfBundle.getWorkflows()) {
				if (wf.getWorkflowIdentifier().equals(uri)) {
					return wf;
				}
			}
			return null;
		}

		// Naive, super-inefficient reverse-lookup - we could have even returned
		// early!
		final Map<URI, WorkflowBean> uriToBean = new HashMap<URI, WorkflowBean>();
		wfBundle.accept(new VisitorWithPath() {
			@Override
			public boolean visit() {
				WorkflowBean node = getCurrentNode();
				URI uri = uriForBean(node);
				WorkflowBean existing = uriToBean.put(uri, node);
				if (existing != null) {
					String msg = "Multiple nodes with same URI {0}: {1} {2}";
					throw new IllegalStateException(MessageFormat.format(msg,
							uri, existing, node));
				}
				return !(node instanceof Configuration);
			}
		});
		if (! uri.isAbsolute()) {
			// Make absolute
			uri = uriForBean(wfBundle).resolve(uri);
		}
		return uriToBean.get(uri);
	}

	public URI uriForBean(WorkflowBean bean) {
		if (bean == null) {
			throw new NullPointerException("Bean can't be null");
		}
		if (bean instanceof Root) {
			Root root = (Root) bean;
			if (root.getSameBaseAs() == null) {
				if (root instanceof WorkflowBundle) {
					root.setSameBaseAs(WorkflowBundle.generateIdentifier());
				} else {
					throw new IllegalArgumentException(
							"sameBaseAs is null for bean " + bean);
				}
			}
			return root.getSameBaseAs();
		}
		if (bean instanceof Child) {
			Child child = (Child) bean;
			WorkflowBean parent = child.getParent();
			if (parent == null) {
				throw new IllegalStateException("Bean does not have a parent: "
						+ child);
			}
			URI parentUri = uriForBean(parent);

			if (!parentUri.getPath().endsWith("/")) {
				parentUri = parentUri.resolve(parentUri.getPath() + "/");
			}
			String relation;
			if (child instanceof InputPort) {
				relation = "in/";
			} else if (child instanceof OutputPort) {
				relation = "out/";
			} else if (child instanceof IterationStrategyStack) {
				relation = "iterationstrategy/";
			} else {
				// TODO: Get relation by container annotations
				relation = child.getClass().getSimpleName() + "/";
				// Stupid fallback
			}

			URI relationURI = parentUri.resolve(relation.toLowerCase());
			if (parent instanceof List) {
				int index = ((List) parent).indexOf(child);
				return parentUri.resolve(index + "/");
			}
			if (bean instanceof Named) {
				Named named = (Named) bean;
				String name = validFilename(named.getName());
				if (!(bean instanceof Port)) {
					name = name + "/";
				}
				return relationURI.resolve(name);
			} else if (bean instanceof DataLink) {

				DataLink dataLink = (DataLink) bean;
				Workflow wf = dataLink.getParent();
				URI wfUri = uriForBean(wf);
				URI receivesFrom = relativePath(wfUri,
						uriForBean(dataLink.getReceivesFrom()));
				URI sendsTo = relativePath(wfUri,
						uriForBean(dataLink.getSendsTo()));
				String dataLinkUri = MessageFormat.format(
						"{0}?{1}={2}&{3}={4}", DATALINK, FROM, receivesFrom,
						TO, sendsTo);
				if (dataLink.getMergePosition() != null) {
					dataLinkUri += MessageFormat.format("&{0}={1}",
							MERGE_POSITION, dataLink.getMergePosition());
				}
				return wfUri.resolve(dataLinkUri);
			} else if(bean instanceof BlockingControlLink) {
				BlockingControlLink runAfterCondition = (BlockingControlLink) bean;
				Workflow wf = runAfterCondition.getParent();
				URI wfUri = uriForBean(wf);

				URI start = relativePath(wfUri,
						uriForBean(runAfterCondition.getBlock()));
				URI after = relativePath(wfUri,
						uriForBean(runAfterCondition.getUntilFinished()));
				String conditionUri = MessageFormat.format("{0}?{1}={2}&{3}={4}",
 "control", "block", start,
						"untilFinished", after);
				return wfUri.resolve(conditionUri);
			} else if (bean instanceof DispatchStack) {
				return relationURI;
			} else if (bean instanceof DispatchStackLayer) {
				DispatchStackLayer dispatchStackLayer = (DispatchStackLayer) bean;

			} else if (bean instanceof IterationStrategyStack) {
				return relationURI;
			} else if (bean instanceof IterationStrategyNode) {
				IterationStrategyNode iterationStrategyNode = (IterationStrategyNode) bean;
				parent = iterationStrategyNode.getParent();
				List<IterationStrategyNode> parentList = (List<IterationStrategyNode>) parent;
				int index = parentList.indexOf(iterationStrategyNode);
				return parentUri.resolve(index + "/");
			} else if (bean instanceof ProcessorPortBinding) {
				// Named after the processor port, extract in/blah part.
				ProcessorPortBinding<?, ?> processorPortBinding = (ProcessorPortBinding<?, ?>) bean;
				ProcessorPort procPort = processorPortBinding
						.getBoundProcessorPort();
				URI procPortUri = relativeUriForBean(procPort,
						processorPortBinding.getParent().getBoundProcessor());
				return parentUri.resolve(procPortUri);
			} else {
				throw new IllegalStateException(
						"Can't create URIs for non-named child: " + bean);
			}

		}
		if (bean instanceof PropertyResource) {
			PropertyResource propertyResource = (PropertyResource) bean;
			URI resourceURI = propertyResource.getResourceURI();
			if (resourceURI != null) {
				return resourceURI;
			}
			throw new IllegalStateException(
					"PropertyResource does not have a resourceURI");
		}
		throw new IllegalArgumentException("Unsupported type "
				+ bean.getClass() + " for bean " + bean);
	}

	public String validFilename(String name) {

		// Make a relative URI
		URI uri;
		try {
			uri = new URI(null, null, name, null, null);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid name " + name);
		}
		String ascii = uri.toASCIIString();
		// And escape / and \
		String escaped = ascii.replace("/", "%2f");
		// escaped = escaped.replace("\\", "%5c");
		escaped = escaped.replace(":", "%3a");
		escaped = escaped.replace("=", "%3d");
		return escaped;
	}

}
