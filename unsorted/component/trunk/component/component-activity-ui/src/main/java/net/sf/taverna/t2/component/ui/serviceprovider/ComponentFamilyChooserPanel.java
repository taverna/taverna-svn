/**
 *
 */
package net.sf.taverna.t2.component.ui.serviceprovider;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.local.LocalComponentRegistry;
import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentComponentRegistry;
import net.sf.taverna.t2.component.ui.view.ComponentListCellRenderer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentFamilyChooserPanel extends JPanel {

	private static Logger logger = Logger.getLogger(ComponentFamilyChooserPanel.class);

	

//	public static final String LOCAL = "Local machine";
	
	public static ComponentRegistry MY_EXPERIMENT_REGISTRY = null;
	
	public static ComponentRegistry LOCAL_REGISTRY = null;
	
	private DefaultComboBoxModel familyModel = new DefaultComboBoxModel();

	private ComponentRegistry currentChoice = MY_EXPERIMENT_REGISTRY;



	private final JComboBox familyChoice = new JComboBox();

	public ComponentFamilyChooserPanel(boolean editableFamily) {
		super();
		
		if (MY_EXPERIMENT_REGISTRY == null) {
			try {
				MY_EXPERIMENT_REGISTRY = MyExperimentComponentRegistry.getComponentRegistry(URI.create("http://www.myexperiment.org").toURL());
			} catch (MalformedURLException e) {
				logger.error(e);
			}			
		}
		
		if (LOCAL_REGISTRY == null) {
			LOCAL_REGISTRY = LocalComponentRegistry.getComponentRegistry(new File(ApplicationRuntime.getInstance().getApplicationHomeDir(), "components"));
		}
		
		
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		final JComboBox sourceChoice = new JComboBox(new ComponentRegistry[] {LOCAL_REGISTRY, MY_EXPERIMENT_REGISTRY});
		sourceChoice.setRenderer(new ComponentListCellRenderer());
		
//		sourceChoice.setEditable(true);

		familyChoice.setEditable(editableFamily);

		final FamilyChoiceEditor familyChoiceEditor = new FamilyChoiceEditor();
		currentChoice = MY_EXPERIMENT_REGISTRY;
		sourceChoice.setSelectedItem(MY_EXPERIMENT_REGISTRY);

		familyChoice.setModel(familyModel);
		familyChoice.setRenderer(new ComponentListCellRenderer());
		if (editableFamily) {
			familyChoiceEditor.setRegistry(MY_EXPERIMENT_REGISTRY);
			familyChoice.setEditor(familyChoiceEditor);
			familyChoiceEditor.setRegistry((ComponentRegistry) sourceChoice.getSelectedItem());
		}
		updateFamilyModel((ComponentRegistry) sourceChoice.getSelectedItem());
		sourceChoice.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ComponentRegistry v = (ComponentRegistry) sourceChoice.getSelectedItem();
				if (!v.equals(currentChoice)) {
					updateFamilyModel(v);
					familyChoiceEditor.setRegistry(v);
					currentChoice = v;
				}
			}});

		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add(new JLabel("Component family site"), gbc);
		gbc.weightx = 1;
		this.add(sourceChoice, gbc);
		gbc.gridy = 1;
		gbc.weightx = 0;
		this.add(new JLabel("Component family"), gbc);
		gbc.weightx = 1;
		this.add(familyChoice, gbc);

	}


	private void updateFamilyModel (ComponentRegistry source) {

		familyModel.removeAllElements();
		try {
			for (ComponentFamily family : source.getComponentFamilies()) {
				familyModel.addElement(family);
			}
		} catch (ComponentRegistryException e) {
			logger.error("Unable to read component families", e);
		}
	}

	public boolean sourceChoiceIsLocal() {
		return currentChoice instanceof LocalComponentRegistry;
	}

	public ComponentFamily getFamilyChoice() {
		return (ComponentFamily) familyModel.getSelectedItem();
	}

	public ComponentRegistry getSourceChoice() {
		return currentChoice;
	}

	public ComponentServiceProviderConfig getConfig() {
		ComponentServiceProviderConfig newConfig = new ComponentServiceProviderConfig();
			newConfig.setRegistryBase(getSourceChoice().getRegistryBase());
			newConfig.setFamilyName(getFamilyChoice().getName());
		return newConfig;
	}
	
	protected void addFamilyChoiceListener(ActionListener listener) {
		familyChoice.addActionListener(listener);
	}
}
