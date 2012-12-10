package net.sf.taverna.t2.component;

import java.io.Serializable;
import java.net.URL;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentUtil;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.registry.local.LocalComponentRegistry;
import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentComponentRegistry;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * Component activity configuration bean.
 * 
 */
public class ComponentActivityConfigurationBean extends ComponentVersionIdentification implements Serializable {
	
	
	private transient Dataflow dataflow;

	public ComponentActivityConfigurationBean(
			ComponentVersionIdentification toBeCopied) {
		super(toBeCopied);
		
	}


	public Dataflow getDataflow() throws ComponentRegistryException {
		if (dataflow == null) {
			ComponentVersion version = ComponentUtil.calculateComponentVersion(this);
			dataflow = version.getDataflow();
		}
		return dataflow;
	}







	
	
}
