package net.sf.taverna.t2.component;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import uk.org.taverna.ns._2012.component.profile.ExceptionHandling;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentDataflowCache;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentUtil;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.registry.local.LocalComponentRegistry;
import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentComponentRegistry;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

/**
 * Component activity configuration bean.
 * 
 */
public class ComponentActivityConfigurationBean extends ComponentVersionIdentification implements Serializable {
	private static Logger logger = Logger.getLogger(ComponentActivity.class);
	
	private transient ActivityPortsDefinitionBean ports = null;
	
	ExceptionHandling eh = null;

	public ComponentActivityConfigurationBean(
			ComponentVersionIdentification toBeCopied) {
		super(toBeCopied);
		try {
			eh = ComponentUtil.calculateFamily(this.getRegistryBase(), this.getFamilyName()).getComponentProfile().getExceptionHandling();
		} catch (ComponentRegistryException e) {
			logger.error(e);
		}
		getPorts();
		
	}

	private ActivityPortsDefinitionBean getPortsDefinition(Dataflow d) {
		ActivityPortsDefinitionBean result = new ActivityPortsDefinitionBean();
		List<ActivityInputPortDefinitionBean> inputs = result.getInputPortDefinitions();
		List<ActivityOutputPortDefinitionBean> outputs = result.getOutputPortDefinitions();

			for (DataflowInputPort dip : d.getInputPorts()) {
				ActivityInputPortDefinitionBean activityInputPortDefinitionBean = new ActivityInputPortDefinitionBean();
				activityInputPortDefinitionBean
						.setHandledReferenceSchemes(null);
				activityInputPortDefinitionBean.setMimeTypes(null);
				activityInputPortDefinitionBean
						.setTranslatedElementType(String.class);
				activityInputPortDefinitionBean
						.setAllowsLiteralValues(true);
				activityInputPortDefinitionBean
						.setDepth(dip.getDepth());
				activityInputPortDefinitionBean.setName(dip.getName());
				inputs.add(activityInputPortDefinitionBean);
			}
			
			for (DataflowOutputPort dop : d.getOutputPorts()) {
				
				ActivityOutputPortDefinitionBean activityOutputPortDefinitionBean = new ActivityOutputPortDefinitionBean();
				activityOutputPortDefinitionBean.setMimeTypes(new ArrayList<String>());
				activityOutputPortDefinitionBean
						.setDepth(dop.getDepth());
				activityOutputPortDefinitionBean.setGranularDepth(dop.getDepth());
				activityOutputPortDefinitionBean.setName(dop.getName());
				outputs.add(activityOutputPortDefinitionBean);
				
			}
			if (eh != null) {
				ActivityOutputPortDefinitionBean activityOutputPortDefinitionBean = new ActivityOutputPortDefinitionBean();
				activityOutputPortDefinitionBean.setMimeTypes(new ArrayList<String>());
				activityOutputPortDefinitionBean
						.setDepth(1);
				activityOutputPortDefinitionBean.setGranularDepth(1);
				activityOutputPortDefinitionBean.setName("error_channel");
				outputs.add(activityOutputPortDefinitionBean);
				
			}
			return result;
		}

	/**
	 * @return the ports
	 */
	public ActivityPortsDefinitionBean getPorts() {
		if (ports == null) {
			try {
				ports = getPortsDefinition(ComponentDataflowCache.getDataflow(this));
			} catch (ComponentRegistryException e) {
				logger.error(e);
			}			
		}
		return ports;
	}

	public ExceptionHandling getExceptionHandling() {
		return eh;
	}

}
