package net.sf.taverna.t2.component;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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
				
//				ActivityInputPort aip = Tools.getActivityInputPort(this, dip.getName());
//				
//				// Copy the annotations
//				
//				for (Class c : aTools.getAnnotatingClasses(aip)) {
//					String annotationValue = aTools.getAnnotationString(dip, c, null);
//					if (annotationValue != null) {
//						try {
//							aTools.setAnnotationString(aip, c, annotationValue).doEdit();
//						} catch (EditException e) {
//							logger.error(e);
//						}
//					}
//				}
			}
			
			for (DataflowOutputPort dop : d.getOutputPorts()) {
				
				ActivityOutputPortDefinitionBean activityOutputPortDefinitionBean = new ActivityOutputPortDefinitionBean();
				activityOutputPortDefinitionBean.setMimeTypes(new ArrayList<String>());
				activityOutputPortDefinitionBean
						.setDepth(dop.getDepth());
				activityOutputPortDefinitionBean.setGranularDepth(dop.getDepth());
				activityOutputPortDefinitionBean.setName(dop.getName());
				outputs.add(activityOutputPortDefinitionBean);

//				OutputPort aop = Tools.getActivityOutputPort(this, dop.getName());
//				
//				for (Class c : aTools.getAnnotatingClasses(aop)) {
//					String annotationValue = aTools.getAnnotationString(dop, c, null);
//					if (annotationValue != null) {
//						try {
//							aTools.setAnnotationString(aop, c, annotationValue).doEdit();
//						} catch (EditException e) {
//							logger.error(e);
//						}
//					}
//				}
				
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

}
