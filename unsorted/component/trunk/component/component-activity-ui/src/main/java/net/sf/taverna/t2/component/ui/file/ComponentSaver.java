/**
 * 
 */
package net.sf.taverna.t2.component.ui.file;

import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentFileType;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentUtil;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.registry.local.LocalComponentRegistry;
import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentComponentRegistry;
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
		if (!(destination instanceof ComponentVersionIdentification)) {
			throw new IllegalArgumentException("Unsupported destination type " + destination.getClass().getName());
		}
		
		ComponentVersionIdentification ident = (ComponentVersionIdentification) destination;
		
		if (ident.getComponentVersion() == -1) {
			ComponentVersionIdentification newIdent = new ComponentVersionIdentification(ident);
			newIdent.setComponentVersion(0);
			return new DataflowInfo(COMPONENT_FILE_TYPE, newIdent, dataflow);
		}
		
		ComponentFamily family;
		try {
			ComponentRegistry registry = ComponentUtil.calculateRegistry(ident.getRegistryBase());
			family = registry.getComponentFamily(ident.getFamilyName());
		} catch (ComponentRegistryException e1) {
			throw new SaveException("Unable to read component", e1);
		}
	
		ComponentVersion newVersion = null;
		try {
			if (ident.getComponentVersion() == 0) {
				JTextArea descriptionArea = new JTextArea(10,60);
				int answer = JOptionPane.showConfirmDialog(null, new JScrollPane(descriptionArea), "Component description", JOptionPane.OK_CANCEL_OPTION);
				if (answer == JOptionPane.OK_OPTION) {
					newVersion = family.createComponentBasedOn(ident.getComponentName(), descriptionArea.getText(), dataflow);
				}
			} else {
				Component component = family.getComponent(ident.getComponentName());
				JTextArea descriptionArea = new JTextArea(10,60);
				int answer = JOptionPane.showConfirmDialog(null, new JScrollPane(descriptionArea), "Version description", JOptionPane.OK_CANCEL_OPTION);
				if (answer == JOptionPane.OK_OPTION) {
					newVersion = component.addVersionBasedOn(dataflow, descriptionArea.getText());
				}
			}
		} catch (ComponentRegistryException e) {
			logger.error("Unable to save new version of component", e);
			throw new SaveException("Unable to save new version of component", e);
		}
		
		ComponentVersionIdentification newIdent = new ComponentVersionIdentification(ident);
		newIdent.setComponentVersion(newVersion.getVersionNumber());
		
		ComponentServiceProviderConfig config = new ComponentServiceProviderConfig();
		config.setRegistryBase(ident.getRegistryBase());
		config.setFamilyName(ident.getFamilyName());
		try {
			Utils.refreshComponentServiceProvider(config);
		} catch (ConfigurationException e) {
			logger.error("Unable to refresh service panel");
		}
		
		return new DataflowInfo(COMPONENT_FILE_TYPE, newIdent, dataflow);
	}
	
	@Override
	public List<FileType> getSaveFileTypes() {
		return Arrays.<FileType> asList(COMPONENT_FILE_TYPE);
	}

	@Override
	public List<Class<?>> getSaveDestinationTypes() {
		return Arrays.<Class<?>> asList(ComponentVersionIdentification.class);
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
