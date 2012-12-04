/**
 * 
 */
package net.sf.taverna.t2.component.ui.file;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.registry.local.LocalComponentRegistry;
import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentComponentRegistry;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceDesc;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceProviderConfig;
import net.sf.taverna.t2.component.ui.util.Utils;
import net.sf.taverna.t2.workbench.file.AbstractDataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentSaver extends AbstractDataflowPersistenceHandler
		implements DataflowPersistenceHandler {

	private static final ComponentFileType COMPONENT_FILE_TYPE = new ComponentFileType();
	
	private static Logger logger = Logger.getLogger(ComponentSaver.class);
	
	@Override
	public DataflowInfo saveDataflow(Dataflow dataflow, FileType fileType,
			Object destination) throws SaveException {
		if (!getSaveFileTypes().contains(fileType)) {
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		}
		if (!(destination instanceof ComponentServiceDesc)) {
			throw new IllegalArgumentException("Unsupported destination type " + destination.getClass().getName());
		}
		
		ComponentServiceDesc desc = (ComponentServiceDesc) destination;
		
		ComponentRegistry registry;
		if (desc.getRegistryBase().getProtocol().startsWith("http")) {
			registry = MyExperimentComponentRegistry.getComponentRegistry(desc.getRegistryBase());
		}
		else {
			registry = LocalComponentRegistry.getComponentRegistry(desc.getRegistryBase());
		}
		ComponentFamily family;
		Component component;
		try {
			family = registry.getComponentFamily(desc.getFamilyName());
			component = family.getComponent(desc.getComponentName());
		} catch (ComponentRegistryException e1) {
			throw new SaveException("Unable to read component", e1);
		}
		
		ComponentVersion newVersion = null;
		try {
			newVersion = component.addVersionBasedOn(dataflow);
		} catch (ComponentRegistryException e) {
			logger.error("Unable to save new version of component", e);
			throw new SaveException("Unable to save new version of component", e);
		}
		
		ComponentServiceDesc newDesc = new ComponentServiceDesc();
		newDesc.setRegistryBase(desc.getRegistryBase());
		newDesc.setFamilyName(desc.getFamilyName());
		newDesc.setComponentName(desc.getComponentName());
		newDesc.setComponentVersion(newVersion.getVersionNumber());
		
		ComponentServiceProviderConfig config = new ComponentServiceProviderConfig();
		config.setRegistryBase(desc.getRegistryBase());
		config.setFamilyName(desc.getFamilyName());
		try {
			Utils.refreshComponentServiceProvider(config);
		} catch (ConfigurationException e) {
			logger.error("Unable to refresh service panel");
		}
		
		return new DataflowInfo(COMPONENT_FILE_TYPE, destination, dataflow);
	}
	
	@Override
	public List<FileType> getSaveFileTypes() {
		return Arrays.<FileType> asList(COMPONENT_FILE_TYPE);
	}

	@Override
	public List<Class<?>> getSaveDestinationTypes() {
		return Arrays.<Class<?>> asList(ComponentServiceDesc.class);
	}

	@Override
	public boolean wouldOverwriteDataflow(Dataflow dataflow, FileType fileType,
			Object destination, DataflowInfo lastDataflowInfo) {
		if (!getSaveFileTypes().contains(fileType)) {
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		}
		return false;
	}
}
