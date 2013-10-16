/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.ui.panel.ComponentChoiceMessage;
import net.sf.taverna.t2.component.ui.panel.ComponentChooserPanel;
import net.sf.taverna.t2.component.ui.panel.ProfileChoiceMessage;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentMergeAction extends AbstractAction {
	private static final long serialVersionUID = 6791184757725253807L;
	private static final Logger logger = Logger
			.getLogger(ComponentMergeAction.class);
	private static final String MERGE_COMPONENT = "Merge component...";

	public ComponentMergeAction() {
		super(MERGE_COMPONENT, ComponentServiceIcon.getIcon());
	}

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
				ProfileChoiceMessage profileMessage = new ProfileChoiceMessage(
						message.getComponentFamily().getComponentProfile());
				target.notify(null, profileMessage);
			}
		});

		int answer = showConfirmDialog(null, overallPanel, "Merge Component",
				OK_CANCEL_OPTION);
		if (answer == OK_OPTION) {
			doMerge(source.getChosenComponent(), target.getChosenComponent());
		}
	}

	private void doMerge(Component sourceComponent, Component targetComponent) {
		if (sourceComponent == null) {
			showMessageDialog(null, "Unable to determine source component",
					"Component Merge Problem", ERROR_MESSAGE);
			return;
		}
		if (targetComponent == null) {
			showMessageDialog(null, "Unable to determine target component",
					"Component Merge Problem", ERROR_MESSAGE);
			return;
		}
		if (sourceComponent.equals(targetComponent)) {
			showMessageDialog(null, "Cannot merge a component with itself",
					"Component Merge Problem", ERROR_MESSAGE);
			return;
		}

		try {
			Version sourceVersion = sourceComponent.getComponentVersionMap()
					.get(sourceComponent.getComponentVersionMap().lastKey());
			targetComponent.addVersionBasedOn(sourceVersion.getDataflow(),
					"Some description");
		} catch (RegistryException e) {
			logger.error(e);
		}
	}

}
