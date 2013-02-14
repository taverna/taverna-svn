/**
 *
 */
package net.sf.taverna.t2.component.ui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentFileType;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;

import org.apache.commons.lang.StringUtils;

/**
 * @author alanrw
 *
 */
public class RegisteryAndFamilyChooserComponentEntryPanel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = -6675545311458594678L;

	private static final String T2FLOW = ".t2flow";

	private static FileType COMPONENT_TYPE = new ComponentFileType();

	private JTextField componentNameField = new JTextField(20);

	private RegistryAndFamilyChooserPanel registryAndFamilyChooserPanel = new RegistryAndFamilyChooserPanel();

	private static FileManager fileManager = FileManager.getInstance();

	private static EditManager editManager = EditManager.getInstance();
	private static Edits edits = editManager.getEdits();

	public RegisteryAndFamilyChooserComponentEntryPanel() {

		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		this.add(registryAndFamilyChooserPanel, gbc);
		gbc.gridy = 1;

		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		this.add(new JLabel("Component name:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add(componentNameField, gbc);
	}

	public String getComponentName() {
		return componentNameField.getText();
	}

	public void setComponentName(String name) {
		componentNameField.setText(name);
	}


	public ComponentVersionIdentification createInitialComponent(Dataflow d)
			throws ComponentRegistryException {
		String componentName = StringUtils.remove(getComponentName(), T2FLOW);

			ComponentFamily familyChoice = registryAndFamilyChooserPanel.getChosenFamily();

			ComponentRegistry registry = registryAndFamilyChooserPanel.getChosenRegistry();

			ComponentVersionIdentification ident = new ComponentVersionIdentification(registry.getRegistryBase(), familyChoice.getName(), componentName, -1);

			try {
				fileManager.saveDataflow(d, COMPONENT_TYPE, ident, false);

				Edit<?> dummyEdit = edits.getUpdateDataflowNameEdit(d, d.getLocalName());
				editManager.doDataflowEdit(d, dummyEdit);
			} catch (OverwriteException e) {
				throw new ComponentRegistryException(e);
			} catch (SaveException e) {
				throw new ComponentRegistryException(e);
			} catch (IllegalStateException e) {
				throw new ComponentRegistryException(e);
			} catch (EditException e) {
				throw new ComponentRegistryException(e);
			}
		return ident;
	}
}
