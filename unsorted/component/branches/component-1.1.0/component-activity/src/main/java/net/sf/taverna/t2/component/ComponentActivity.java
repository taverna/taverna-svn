package net.sf.taverna.t2.component;

import static java.lang.Thread.currentThread;
import static net.sf.taverna.t2.component.registry.ComponentDataflowCache.getDataflow;
import static org.apache.log4j.Logger.getLogger;

import java.util.Map;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.profile.ExceptionHandling;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.impl.InvocationContextImpl;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;

import org.apache.log4j.Logger;

public class ComponentActivity extends
		AbstractAsynchronousActivity<ComponentActivityConfigurationBean>
		implements AsynchronousActivity<ComponentActivityConfigurationBean>,
		NestedDataflow {
	private static final Logger logger = getLogger(ComponentActivity.class);
	private static final EditManager em = EditManager.getInstance();
	private static final Edits EDITS = em.getEdits();
	private static final AnnotationTools aTools = new AnnotationTools();

	private volatile DataflowActivity componentRealization = new DataflowActivity();
	private ComponentActivityConfigurationBean configBean;
	private DataflowImpl skeletonDataflow = null;

	@Override
	public void configure(ComponentActivityConfigurationBean configBean)
			throws ActivityConfigurationException {
		this.configBean = configBean;

		try {
			configurePorts(configBean.getPorts());
		} catch (RegistryException e) {
			throw new ActivityConfigurationException(
					"failed to get component realization", e);
		}

		skeletonDataflow = (DataflowImpl) EDITS.createDataflow();
		skeletonDataflow.setLocalName(configBean.getComponentName());
		for (ActivityInputPort aip : getInputPorts())
			try {
				DataflowInputPort dip = EDITS.createDataflowInputPort(
						aip.getName(), aip.getDepth(), aip.getDepth(),
						skeletonDataflow);
				EDITS.getAddDataflowInputPortEdit(skeletonDataflow, dip)
						.doEdit();
			} catch (EditException e) {
				logger.error("failed to add dataflow input", e);
			}
		for (OutputPort aop : getOutputPorts())
			try {
				DataflowOutputPort dop = EDITS.createDataflowOutputPort(
						aop.getName(), skeletonDataflow);
				EDITS.getAddDataflowOutputPortEdit(skeletonDataflow, dop)
						.doEdit();
			} catch (EditException e) {
				logger.error("failed to add dataflow output", e);
			}
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs,
			final AsynchronousActivityCallback callback) {
		// try {
		//
		// Field field = callback.getClass().getDeclaredField("this$0");
		// field.setAccessible(true);
		// AbstractDispatchLayer container = (AbstractDispatchLayer)
		// field.get(callback);
		//
		// Processor containingProcessor = container.getProcessor();
		//
		// String description = aTools.getAnnotationString(containingProcessor,
		// FreeTextDescription.class, null);
		// } catch (Exception e) {
		// logger.error(e);
		// }
		try {
			ExceptionHandling exceptionHandling = configBean
					.getExceptionHandling();
			// InvocationContextImpl newContext =
			// copyInvocationContext(callback);

			AsynchronousActivityCallback useCallback = new ProxyCallback(
					callback, callback.getContext(), exceptionHandling);
			getComponentRealization().executeAsynch(inputs, useCallback);
		} catch (ActivityConfigurationException e) {
			callback.fail("Unable to execute component", e);
		}
	}

	@SuppressWarnings("unused")
	private InvocationContextImpl copyInvocationContext(
			final AsynchronousActivityCallback callback) {
		InvocationContext originalContext = callback.getContext();
		ReferenceService rs = originalContext.getReferenceService();
		InvocationContextImpl newContext = new InvocationContextImpl(rs, null);
		// for (Object o : originalContext.getEntities(Object.class)) {
		// newContext.addEntity(o);
		// }
		return newContext;
	}

	@Override
	public ComponentActivityConfigurationBean getConfiguration() {
		return configBean;
	}

	public DataflowActivity getComponentRealization()
			throws ActivityConfigurationException {
		synchronized (componentRealization) {
			if (componentRealization.getConfiguration() == null) {
				Dataflow d;
				try {
					d = getDataflow(configBean);
				} catch (RegistryException e) {
					throw new ActivityConfigurationException(
							"Unable to read dataflow", e);
				}
				componentRealization.configure(d);

				for (Class<?> c : aTools.getAnnotatingClasses(this)) {
					String annotationValue = aTools.getAnnotationString(d, c,
							null);
					if (annotationValue == null)
						continue;
					try {
						aTools.setAnnotationString(this, c, annotationValue)
								.doEdit();
					} catch (EditException e) {
						logger.error("failed to set annotation string", e);
					}
				}

			}
			return componentRealization;
		}
	}

	@Override
	public Dataflow getNestedDataflow() {
		// FIXME To go when integrated into Taverna properly
		StackTraceElement[] stackTrace = currentThread().getStackTrace();
		for (StackTraceElement elem : stackTrace)
			if (elem.getClassName().contains("GraphController"))
				return skeletonDataflow;
		try {
			return getDataflow(configBean);
		} catch (RegistryException e) {
			logger.error("failed to get component realization", e);
		}
		return skeletonDataflow;
	}

}
