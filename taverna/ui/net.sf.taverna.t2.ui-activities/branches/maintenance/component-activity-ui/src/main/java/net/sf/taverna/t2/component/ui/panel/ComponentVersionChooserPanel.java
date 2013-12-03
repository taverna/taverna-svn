/**
 *
 */
package net.sf.taverna.t2.component.ui.panel;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;
import static java.awt.event.ItemEvent.SELECTED;
import static net.sf.taverna.t2.component.ui.util.Utils.SHORT_STRING;
import static org.apache.log4j.Logger.getLogger;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentVersionChooserPanel extends JPanel implements
		Observer<ComponentChoiceMessage> {
	private static final long serialVersionUID = 5125907010496468219L;
	private static Logger logger = getLogger(ComponentVersionChooserPanel.class);

	private final JComboBox componentVersionChoice = new JComboBox();
	private final SortedMap<Integer, Version> componentVersionMap = new TreeMap<Integer, Version>();
	private final ComponentChooserPanel componentChooserPanel = new ComponentChooserPanel();

	public ComponentVersionChooserPanel() {
		super();
		setLayout(new GridBagLayout());

		componentVersionChoice.setPrototypeDisplayValue(SHORT_STRING);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.anchor = WEST;
		gbc.fill = HORIZONTAL;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		this.add(componentChooserPanel, gbc);
		componentChooserPanel.addObserver(this);

		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.fill = NONE;
		this.add(new JLabel("Component version:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		this.add(componentVersionChoice, gbc);
		componentVersionChoice.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == SELECTED)
					updateToolTipText();
			}
		});
	}

	protected void updateToolTipText() {
		Version chosenComponentVersion = getChosenComponentVersion();
		if (chosenComponentVersion != null)
			componentVersionChoice.setToolTipText(chosenComponentVersion
					.getDescription());
		else
			componentVersionChoice.setToolTipText(null);
	}

	private void updateComponentVersionModel() {
		componentVersionMap.clear();
		componentVersionChoice.removeAllItems();
		componentVersionChoice.setToolTipText(null);
		componentVersionChoice.addItem("Reading component versions");
		componentVersionChoice.setEnabled(false);
		new ComponentVersionUpdater().execute();
	}

	public Version getChosenComponentVersion() {
		if (componentVersionMap.isEmpty())
			return null;
		try {
            return componentVersionMap.get(new Integer(componentVersionChoice
                            .getSelectedItem().toString()));
		} catch (NumberFormatException nfe) {
            // Not a number, no version chosen
            return null;
		}
	}

	@Override
	public void notify(Observable<ComponentChoiceMessage> sender,
			ComponentChoiceMessage message) {
		try {
			updateComponentVersionModel();
		} catch (RuntimeException e) {
			logger.error("problem updating view from component version", e);
		}
	}

	public Registry getChosenRegistry() {
		return componentChooserPanel.getChosenRegistry();
	}

	public Family getChosenFamily() {
		return componentChooserPanel.getChosenFamily();
	}

	public Component getChosenComponent() {
		return componentChooserPanel.getChosenComponent();
	}

	private class ComponentVersionUpdater extends SwingWorker<String, Object> {
		@Override
		protected String doInBackground() throws Exception {
			Component chosenComponent = componentChooserPanel
					.getChosenComponent();
			if (chosenComponent != null)
				for (Version version : chosenComponent.getComponentVersionMap()
						.values()) {
					Integer versionNumber = version.getVersionNumber();
					componentVersionMap.put(versionNumber, version);
				}
			return null;
		}

		@Override
		protected void done() {
			componentVersionChoice.removeAllItems();
			for (Integer versionNumber : componentVersionMap.keySet())
				componentVersionChoice.addItem(versionNumber);

			if (!componentVersionMap.isEmpty()) {
				componentVersionChoice.setSelectedItem(componentVersionMap
						.lastKey());
				updateToolTipText();
			} else
				componentVersionChoice.addItem("No versions available");
			componentVersionChoice.setEnabled(!componentVersionMap.isEmpty());
		}
	}
}
