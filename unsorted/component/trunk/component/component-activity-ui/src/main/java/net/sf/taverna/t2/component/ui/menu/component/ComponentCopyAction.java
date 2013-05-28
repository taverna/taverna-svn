/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.ui.panel.ComponentChoiceMessage;
import net.sf.taverna.t2.component.ui.panel.ComponentChooserPanel;
import net.sf.taverna.t2.component.ui.panel.FamilyChooserPanel;
import net.sf.taverna.t2.component.ui.panel.ProfileChoiceMessage;
import net.sf.taverna.t2.component.ui.panel.ProfileChooserPanel;
import net.sf.taverna.t2.component.ui.panel.RegistryAndFamilyChooserPanel;
import net.sf.taverna.t2.component.ui.panel.RegistryChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.file.impl.actions.SaveWorkflowAction;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentCopyAction extends AbstractAction {
	
	private static Logger logger = Logger.getLogger(ComponentCopyAction.class);
		
	private static final String COPY_COMPONENT = "Copy component...";
	
	public ComponentCopyAction() {
		super (COPY_COMPONENT, ComponentServiceIcon.getIcon());
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		JPanel overallPanel = new JPanel();
		overallPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		ComponentChooserPanel source = new ComponentChooserPanel();
		source.setBorder(new TitledBorder("Source component"));
		
		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		overallPanel.add(source, gbc);
		
		final RegistryAndFamilyChooserPanel target = new RegistryAndFamilyChooserPanel();
		target.setBorder(new TitledBorder("Target family"));
		gbc.gridy++;
		overallPanel.add(target, gbc);
		
		source.addObserver(new Observer<ComponentChoiceMessage>() {

			@Override
			public void notify(Observable<ComponentChoiceMessage> sender,
					ComponentChoiceMessage message) throws Exception {
				ComponentProfile componentProfile = null;
				ComponentFamily componentFamily = message.getComponentFamily();
				if (componentFamily != null) {
					componentProfile = componentFamily.getComponentProfile();
				}
				ProfileChoiceMessage profileMessage = new ProfileChoiceMessage(componentProfile);
				target.notify(null, profileMessage);
			}});
		
		int answer = JOptionPane.showConfirmDialog(null, overallPanel, "Copy Component", JOptionPane.OK_CANCEL_OPTION);
		if (answer == JOptionPane.OK_OPTION) {
				Component sourceComponent = source.getChosenComponent();
				if (sourceComponent == null) {
					JOptionPane.showMessageDialog(null, "Unable to determine source component", "Component Copy Problem", JOptionPane.ERROR_MESSAGE);
					return;
				}
				ComponentFamily targetFamily = target.getChosenFamily();
				if (targetFamily == null) {
					JOptionPane.showMessageDialog(null, "Unable to determine target family", "Component Copy Problem", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String componentName = sourceComponent.getName();
				
				try {
					boolean alreadyUsed = targetFamily.getComponent(componentName) != null;
					if (alreadyUsed) {
						JOptionPane.showMessageDialog(null, componentName + " is already used", "Duplicate component name", JOptionPane.ERROR_MESSAGE);
					} else {
						ComponentVersion sourceVersion = sourceComponent.getComponentVersionMap().get(sourceComponent.getComponentVersionMap().lastKey());
						targetFamily.createComponentBasedOn(componentName, sourceComponent.getDescription(), sourceVersion.getDataflow());
					}
				} catch (ComponentRegistryException e) {
					JOptionPane.showMessageDialog(null, "Unable to create component", "Component Copy Problem", JOptionPane.ERROR_MESSAGE);
					logger.error(e);
				}
		}
		
	}

}
