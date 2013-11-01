/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.WEST;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon.getIcon;
import static org.apache.log4j.Logger.getLogger;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.ui.panel.ComponentChoiceMessage;
import net.sf.taverna.t2.component.ui.panel.ComponentChooserPanel;
import net.sf.taverna.t2.component.ui.panel.ProfileChoiceMessage;
import net.sf.taverna.t2.component.ui.panel.RegistryAndFamilyChooserPanel;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentCopyAction extends AbstractAction {
	private static final long serialVersionUID = -4440978712410081685L;
	private static final Logger logger = getLogger(ComponentCopyAction.class);
	private static final String COPY_COMPONENT = "Copy component...";

	public ComponentCopyAction() {
		super(COPY_COMPONENT, getIcon());
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
		gbc.anchor = WEST;
		gbc.fill = BOTH;
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
				Profile componentProfile = null;
				Family componentFamily = message.getComponentFamily();
				if (componentFamily != null)
					componentProfile = componentFamily.getComponentProfile();
				ProfileChoiceMessage profileMessage = new ProfileChoiceMessage(
						componentProfile);
				target.notify(null, profileMessage);
			}
		});

		int answer = showConfirmDialog(null, overallPanel, "Copy Component",
				OK_CANCEL_OPTION);
		if (answer == OK_OPTION)
			doCopy(source.getChosenComponent(), target.getChosenFamily());
	}

	private void doCopy(Component sourceComponent, Family targetFamily) {
		if (sourceComponent == null) {
			showMessageDialog(null, "Unable to determine source component",
					"Component Copy Problem", ERROR_MESSAGE);
			return;
		} else if (targetFamily == null) {
			showMessageDialog(null, "Unable to determine target family",
					"Component Copy Problem", ERROR_MESSAGE);
			return;
		}

		try {
			String componentName = sourceComponent.getName();
			boolean alreadyUsed = targetFamily.getComponent(componentName) != null;
			if (alreadyUsed) {
				showMessageDialog(null, componentName + " is already used",
						"Duplicate component name", ERROR_MESSAGE);
			} else {
				Version sourceVersion = sourceComponent
						.getComponentVersionMap().get(
								sourceComponent.getComponentVersionMap()
										.lastKey());
				targetFamily.createComponentBasedOn(componentName,
						sourceComponent.getDescription(),
						sourceVersion.getDataflow());
			}
		} catch (RegistryException e) {
			logger.error(e);
			showMessageDialog(null,
					"Unable to create component: " + e.getMessage(),
					"Component Copy Problem", ERROR_MESSAGE);
		}
	}

}
