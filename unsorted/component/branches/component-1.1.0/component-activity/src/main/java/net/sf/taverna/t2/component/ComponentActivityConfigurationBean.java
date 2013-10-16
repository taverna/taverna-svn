package net.sf.taverna.t2.component;

import static net.sf.taverna.t2.component.registry.ComponentDataflowCache.getDataflow;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateFamily;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.profile.ExceptionHandling;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;

import org.apache.log4j.Logger;

/**
 * Component activity configuration bean.
 * 
 */
public class ComponentActivityConfigurationBean extends
		ComponentVersionIdentification implements Serializable {
	public static final String ERROR_CHANNEL = "error_channel";
	public static final List<String> ignorableNames = Arrays
			.asList(ERROR_CHANNEL);
	private static final long serialVersionUID = 5774901665863468058L;
	private static final Logger logger = Logger
			.getLogger(ComponentActivity.class);

	private transient ActivityPortsDefinitionBean ports = null;
	private transient ExceptionHandling eh;

	public ComponentActivityConfigurationBean(Version.ID toBeCopied) {
		super(toBeCopied);
		getPorts();
	}

	private ActivityPortsDefinitionBean getPortsDefinition(Dataflow d) {
		ActivityPortsDefinitionBean result = new ActivityPortsDefinitionBean();
		List<ActivityInputPortDefinitionBean> inputs = result
				.getInputPortDefinitions();
		List<ActivityOutputPortDefinitionBean> outputs = result
				.getOutputPortDefinitions();

		for (DataflowInputPort dip : d.getInputPorts())
			inputs.add(makeInputDefinition(dip));

		for (DataflowOutputPort dop : d.getOutputPorts())
			outputs.add(makeOutputDefinition(dop.getDepth(), dop.getName()));

		try {
			eh = calculateFamily(getRegistryBase(), getFamilyName())
					.getComponentProfile().getExceptionHandling();
			if (eh != null)
				outputs.add(makeOutputDefinition(1, ERROR_CHANNEL));
		} catch (RegistryException e) {
			logger.error(e);
		}
		return result;
	}

	private ActivityInputPortDefinitionBean makeInputDefinition(
			DataflowInputPort dip) {
		ActivityInputPortDefinitionBean activityInputPortDefinitionBean = new ActivityInputPortDefinitionBean();
		activityInputPortDefinitionBean.setHandledReferenceSchemes(null);
		activityInputPortDefinitionBean.setMimeTypes(null);
		activityInputPortDefinitionBean.setTranslatedElementType(String.class);
		activityInputPortDefinitionBean.setAllowsLiteralValues(true);
		activityInputPortDefinitionBean.setDepth(dip.getDepth());
		activityInputPortDefinitionBean.setName(dip.getName());
		return activityInputPortDefinitionBean;
	}

	private ActivityOutputPortDefinitionBean makeOutputDefinition(int depth,
			String name) {
		ActivityOutputPortDefinitionBean activityOutputPortDefinitionBean = new ActivityOutputPortDefinitionBean();
		activityOutputPortDefinitionBean.setMimeTypes(new ArrayList<String>());
		activityOutputPortDefinitionBean.setDepth(depth);
		activityOutputPortDefinitionBean.setGranularDepth(depth);
		activityOutputPortDefinitionBean.setName(name);
		return activityOutputPortDefinitionBean;
	}

	/**
	 * @return the ports
	 */
	public ActivityPortsDefinitionBean getPorts() {
		try {
			if (ports == null)
				ports = getPortsDefinition(getDataflow(this));
		} catch (RegistryException e) {
			logger.error(e);
		}
		return ports;
	}

	public ExceptionHandling getExceptionHandling() {
		return eh;
	}
}
