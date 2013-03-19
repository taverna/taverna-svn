package net.sf.taverna.t2.component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.component.PatchedInvoke.PatchedInvokeCallBack;
import net.sf.taverna.t2.component.profile.ExceptionHandling;
import net.sf.taverna.t2.component.registry.ComponentDataflowCache;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.impl.InvocationContextImpl;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class ComponentActivity extends
		AbstractAsynchronousActivity<ComponentActivityConfigurationBean>
		implements AsynchronousActivity<ComponentActivityConfigurationBean> {
	
	private static Logger logger = Logger.getLogger(ComponentActivity.class);

	private volatile DataflowActivity componentRealization = new DataflowActivity();
	
	private ComponentActivityConfigurationBean configBean;
	
	private static AnnotationTools aTools = new AnnotationTools();

	@Override
	public void configure(ComponentActivityConfigurationBean configBean)
			throws ActivityConfigurationException {
		
		this.configBean = configBean;

		configurePorts(configBean.getPorts());
		

	}

	@SuppressWarnings("unchecked")
	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs,
			final AsynchronousActivityCallback callback) {
		try {
			ExceptionHandling exceptionHandling = configBean.getExceptionHandling();
			if (callback instanceof PatchedInvokeCallBack) {
				InvocationContext originalContext = callback.getContext();
				ReferenceService rs = originalContext.getReferenceService();
				InvocationContextImpl newContext = new InvocationContextImpl(rs, null);
				((PatchedInvokeCallBack) callback).overrideContext(newContext);
			}
			AsynchronousActivityCallback useCallback = callback;
			if (exceptionHandling != null) {
				useCallback = new ProxyCallback(callback, exceptionHandling);
			}
			getComponentRealization().executeAsynch (inputs, useCallback);
		} catch (ActivityConfigurationException e) {
			callback.fail("Unable to execute component", e);
		}
	}

	@Override
	public ComponentActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}
	
	public DataflowActivity getComponentRealization() throws ActivityConfigurationException {
		synchronized(componentRealization) {
			if (componentRealization.getConfiguration() == null) {

			Dataflow d;
			try {
				d = ComponentDataflowCache.getDataflow(configBean);
			} catch (ComponentRegistryException e) {
				throw new ActivityConfigurationException("Unable to read dataflow", e);
			}
			componentRealization.configure(d);

			for (Class c : aTools.getAnnotatingClasses(this)) {
				String annotationValue = aTools.getAnnotationString(d, c, null);
				if (annotationValue != null) {
					try {
						aTools.setAnnotationString(this, c, annotationValue).doEdit();
					} catch (EditException e) {
						logger.error(e);
					}
				}
			}

		}
		}
		return componentRealization;
	}


}
