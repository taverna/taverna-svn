/**
 * 
 */
package net.sf.taverna.t2.component.ui.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils;
import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.ComponentFileType;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.component.registry.ComponentUtil;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
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

	private static final FileType COMPONENT_FILE_TYPE = ComponentFileType.instance;
	
	private static Logger logger = Logger.getLogger(ComponentSaver.class);
	
	@Override
	public DataflowInfo saveDataflow(Dataflow dataflow, FileType fileType,
			Object destination) throws SaveException {
		if (!getSaveFileTypes().contains(fileType)) {
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		}
		if (!(destination instanceof Version.ID)) {
			throw new IllegalArgumentException("Unsupported destination type " + destination.getClass().getName());
		}
		
		Version.ID ident = (Version.ID) destination;
		
		if (ident.getComponentVersion() == -1) {
			ComponentVersionIdentification newIdent = new ComponentVersionIdentification(ident);
			newIdent.setComponentVersion(0);
			return new DataflowInfo(COMPONENT_FILE_TYPE, newIdent, dataflow);
		}
		
		Family family;
		try {
			Registry registry = ComponentUtil.calculateRegistry(ident.getRegistryBase());
			family = registry.getComponentFamily(ident.getFamilyName());
		} catch (RegistryException e1) {
			throw new SaveException("Unable to read component", e1);
		}
	
		Version newVersion = null;
		try {
			List<SemanticAnnotationProfile> problemProfiles = new ArrayList<SemanticAnnotationProfile>(SemanticAnnotationUtils.checkComponent(dataflow, family.getComponentProfile()));
			
			if (!problemProfiles.isEmpty()) {
				int answer = JOptionPane.showConfirmDialog(null, "The component does not satisfy the profile.\nSee validation report.\nDo you still want to save?", "Profile problem", JOptionPane.OK_CANCEL_OPTION);
				if (answer != JOptionPane.OK_OPTION) {
					throw new SaveException("Saving cancelled");
				}
			}
			
			if (ident.getComponentVersion() == 0) {
				JTextArea descriptionArea = new JTextArea(10,60);
				int answer = JOptionPane.showConfirmDialog(null, new JScrollPane(descriptionArea), "Component description", JOptionPane.OK_CANCEL_OPTION);
				if (answer == JOptionPane.OK_OPTION) {
					newVersion = family.createComponentBasedOn(ident.getComponentName(), descriptionArea.getText(), dataflow);
				} else {
					throw new SaveException("Saving cancelled");
				}
			} else {
				Component component = family.getComponent(ident.getComponentName());
				JTextArea descriptionArea = new JTextArea(10,60);
				int answer = JOptionPane.showConfirmDialog(null, new JScrollPane(descriptionArea), "Version description", JOptionPane.OK_CANCEL_OPTION);
				if (answer == JOptionPane.OK_OPTION) {
					newVersion = component.addVersionBasedOn(dataflow, descriptionArea.getText());
				} else {
					throw new SaveException("Saving cancelled");
				}
			}
		} catch (RegistryException e) {
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
