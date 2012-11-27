package net.sf.taverna.t2.component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;

public class ComponentActivity extends
		AbstractAsynchronousActivity<ComponentActivityConfigurationBean>
		implements AsynchronousActivity<ComponentActivityConfigurationBean> {

	private volatile DataflowActivity componentRealization = new DataflowActivity();
	
	private ComponentActivityConfigurationBean configBean;

	@Override
	public void configure(ComponentActivityConfigurationBean configBean)
			throws ActivityConfigurationException {
		
		// Store for getConfiguration(), but you could also make
		// getConfiguration() return a new bean from other sources
		this.configBean = configBean;

		Dataflow d;
		try {
			d = configBean.getDataflow();
		} catch (ComponentRegistryException e) {
			throw new ActivityConfigurationException("Unable to read dataflow", e);
		}
		componentRealization.configure(d);
		d.checkValidity();
		// TODO What to do if not valid?
		
		// REQUIRED: (Re)create input/output ports depending on configuration
		configurePorts(d);
	}

	protected void configurePorts(Dataflow d) {
		// In case we are being reconfigured - remove existing ports first
		// to avoid duplicates
		removeInputs();
		removeOutputs();

		for (DataflowInputPort dip : d.getInputPorts()) {
			// TODO what if it is not a String?
			addInput(dip.getName(), dip.getDepth(), true, null, String.class);
		}
		
		for (DataflowOutputPort dop : d.getOutputPorts()) {
			addOutput(dop.getName(), dop.getDepth());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs,
			final AsynchronousActivityCallback callback) {
		componentRealization.executeAsynch (inputs, callback);
	}

	@Override
	public ComponentActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}
	
	public static Dataflow openDataflowString(String dataflowString) throws ActivityConfigurationException {
		return openDataflowStream(new ByteArrayInputStream(dataflowString.getBytes()));
	}
	
	public static Dataflow openDataflowStream(InputStream workflowXMLstream)
			throws ActivityConfigurationException {
		XMLDeserializer deserializer = new XMLDeserializerImpl();
		SAXBuilder builder = new SAXBuilder();
		Document document;
		try {
			document = builder.build(workflowXMLstream);
		} catch (JDOMException e) {
			throw new ActivityConfigurationException("Could not parse XML of the workflow", e);
		} catch (IOException e) {
			throw new ActivityConfigurationException(
					"Could not open the workflow file for parsing", e);
		}

		Dataflow dataflow;
		try {
			dataflow = deserializer.deserializeDataflow(document
					.getRootElement());
		} catch (DeserializationException e) {
			throw new ActivityConfigurationException("Could not deserialise the workflow", e);
		} catch (EditException e) {
			throw new ActivityConfigurationException("Could not construct the workflow", e);
		}
		return dataflow;
	}

	public DataflowActivity getComponentRealization() {
		return componentRealization;
	}


}
