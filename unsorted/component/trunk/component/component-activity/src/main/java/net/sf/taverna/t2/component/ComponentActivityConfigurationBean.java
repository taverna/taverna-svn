package net.sf.taverna.t2.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.component.profile.ExceptionHandling;
import net.sf.taverna.t2.component.registry.ComponentDataflowCache;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentUtil;
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
public class ComponentActivityConfigurationBean extends ComponentVersionIdentification implements Serializable {
	public static final String ERROR_CHANNEL = "error_channel";
	
	public static List<String> ignorableNames = Arrays.asList(ERROR_CHANNEL);

	/**
	 * 
	 */
	private static final long serialVersionUID = 5774901665863468058L;

	private static Logger logger = Logger.getLogger(ComponentActivity.class);
	
	private transient ActivityPortsDefinitionBean ports = null;
	
	private transient ExceptionHandling eh;

	public ComponentActivityConfigurationBean(
			ComponentVersionIdentification toBeCopied) {
		super(toBeCopied);
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
			try {
				eh = ComponentUtil.calculateFamily(this.getRegistryBase(), this.getFamilyName()).getComponentProfile().getExceptionHandling();
				if (eh != null) {
					ActivityOutputPortDefinitionBean activityOutputPortDefinitionBean = new ActivityOutputPortDefinitionBean();
					activityOutputPortDefinitionBean.setMimeTypes(new ArrayList<String>());
					activityOutputPortDefinitionBean
							.setDepth(1);
					activityOutputPortDefinitionBean.setGranularDepth(1);
					activityOutputPortDefinitionBean.setName(ERROR_CHANNEL);
					outputs.add(activityOutputPortDefinitionBean);
					
				}
			} catch (ComponentRegistryException e) {
				logger.error(e);
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
