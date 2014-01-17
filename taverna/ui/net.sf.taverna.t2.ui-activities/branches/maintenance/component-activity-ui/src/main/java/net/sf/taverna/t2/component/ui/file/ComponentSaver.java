/**
 * 
 */
package net.sf.taverna.t2.component.ui.file;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.checkComponent;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateRegistry;
import static net.sf.taverna.t2.component.ui.util.Utils.refreshComponentServiceProvider;
import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.ComponentFileType;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.profile.SemanticAnnotationProfile;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceProviderConfig;
import net.sf.taverna.t2.workbench.file.AbstractDataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentSaver extends AbstractDataflowPersistenceHandler
		implements DataflowPersistenceHandler {
	private static final String UNSATISFIED_PROFILE_WARNING = "The component does not satisfy the profile.\nSee validation report.\nDo you still want to save?";
	private static final FileType COMPONENT_FILE_TYPE = ComponentFileType.instance;
	private static final Logger logger = getLogger(ComponentSaver.class);

	@Override
	public DataflowInfo saveDataflow(Dataflow dataflow, FileType fileType,
			Object destination) throws SaveException {
		if (!getSaveFileTypes().contains(fileType))
			throw new IllegalArgumentException("Unsupported file type "
					+ fileType);
		if (!(destination instanceof Version.ID))
			throw new IllegalArgumentException("Unsupported destination type "
					+ destination.getClass().getName());

		DataflowValidationReport dvr = dataflow.checkValidity();
		if (!dvr.isValid()) {
			throw new SaveException("Cannot save a structurally invalid workflow as a component");
		}
		// Saving an invalid dataflow is OK. Validity check is done to get predicted depth for output (if possible)

		Version.ID ident = (Version.ID) destination;

		if (ident.getComponentVersion() == -1) {
			ComponentVersionIdentification newIdent = new ComponentVersionIdentification(
					ident);
			newIdent.setComponentVersion(0);
			return new DataflowInfo(COMPONENT_FILE_TYPE, newIdent, dataflow);
		}

		Family family;
		try {
			Registry registry = calculateRegistry(ident.getRegistryBase());
			family = registry.getComponentFamily(ident.getFamilyName());
		} catch (RegistryException e1) {
			throw new SaveException("Unable to read component", e1);
		}

		Version newVersion = null;
		try {
			List<SemanticAnnotationProfile> problemProfiles = new ArrayList<SemanticAnnotationProfile>(
					checkComponent(dataflow, family.getComponentProfile()));

			if (!problemProfiles.isEmpty()) {
				int answer = showConfirmDialog(null,
						UNSATISFIED_PROFILE_WARNING, "Profile problem",
						OK_CANCEL_OPTION);
				if (answer != OK_OPTION)
					throw new SaveException("Saving cancelled");
			}

			if (ident.getComponentVersion() == 0) {
				JTextArea descriptionArea = new JTextArea(10, 60);
				int answer = showConfirmDialog(null, new JScrollPane(
						descriptionArea), "Component description",
						OK_CANCEL_OPTION);
				if (answer == OK_OPTION) {
					newVersion = family.createComponentBasedOn(
							ident.getComponentName(),
							descriptionArea.getText(), dataflow);
				} else {
					throw new SaveException("Saving cancelled");
				}
			} else {
				Component component = family.getComponent(ident
						.getComponentName());
				JTextArea descriptionArea = new JTextArea(10, 60);
				int answer = showConfirmDialog(null, new JScrollPane(
						descriptionArea), "Version description",
						OK_CANCEL_OPTION);
				if (answer != OK_OPTION)
					throw new SaveException("Saving cancelled");
				newVersion = component.addVersionBasedOn(dataflow,
						descriptionArea.getText());
			}
		} catch (RegistryException e) {
			logger.error("Unable to save new version of component", e);
			throw new SaveException("Unable to save new version of component",
					e);
		}

		ComponentVersionIdentification newIdent = new ComponentVersionIdentification(
				ident);
		newIdent.setComponentVersion(newVersion.getVersionNumber());

		try {
			refreshComponentServiceProvider(new ComponentServiceProviderConfig(
					ident));
		} catch (ConfigurationException e) {
			logger.error("Unable to refresh service panel", e);
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
