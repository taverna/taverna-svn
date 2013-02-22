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
public class ComponentMergeAction extends AbstractAction {
	
	private static Logger logger = Logger.getLogger(ComponentMergeAction.class);
		
	private static final String MERGE_COMPONENT = "Merge component...";
	
	public ComponentMergeAction() {
		super (MERGE_COMPONENT, ComponentServiceIcon.getIcon());
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
		
		final ComponentChooserPanel target = new ComponentChooserPanel();
		target.setBorder(new TitledBorder("Target component"));
		gbc.gridy++;
		overallPanel.add(target, gbc);
		
		source.addObserver(new Observer<ComponentChoiceMessage>() {

			@Override
			public void notify(Observable<ComponentChoiceMessage> sender,
					ComponentChoiceMessage message) throws Exception {
				ProfileChoiceMessage profileMessage = new ProfileChoiceMessage(message.getComponentFamily().getComponentProfile());
				target.notify(null, profileMessage);
			}});
		
		int answer = JOptionPane.showConfirmDialog(null, overallPanel, "Merge Component", JOptionPane.OK_CANCEL_OPTION);
		if (answer == JOptionPane.OK_OPTION) {
				Component sourceComponent = source.getChosenComponent();
				if (sourceComponent == null) {
					JOptionPane.showMessageDialog(null, "Unable to determine source component", "Component Merge Problem", JOptionPane.ERROR_MESSAGE);
					return;
				}
				Component targetComponent = target.getChosenComponent();
				if (targetComponent == null) {
					JOptionPane.showMessageDialog(null, "Unable to determine target component", "Component Merge Problem", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (sourceComponent.equals(targetComponent)) {
					JOptionPane.showMessageDialog(null, "Cannot merge a component with itself", "Component Merge Problem", JOptionPane.ERROR_MESSAGE);
					return;				
				}
				
				try {
					ComponentVersion sourceVersion = sourceComponent.getComponentVersionMap().get(sourceComponent.getComponentVersionMap().lastKey());
					targetComponent.addVersionBasedOn(sourceVersion.getDataflow());
				} catch (ComponentRegistryException e) {
					logger.error(e);
				}
		}
	}

}
