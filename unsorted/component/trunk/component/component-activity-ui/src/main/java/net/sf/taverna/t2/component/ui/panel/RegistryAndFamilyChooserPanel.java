/**
 *
 */
package net.sf.taverna.t2.component.ui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;

import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class RegistryAndFamilyChooserPanel extends JPanel implements
		Observer<ProfileChoiceMessage>, Observable<FamilyChoiceMessage> {
	private static Logger logger = Logger
			.getLogger(RegistryAndFamilyChooserPanel.class);
	private static final long serialVersionUID = -535518473593617735L;
	RegistryChooserPanel registryPanel = new RegistryChooserPanel();
	FamilyChooserPanel familyPanel = new FamilyChooserPanel();

	@SuppressWarnings("unchecked")
	public RegistryAndFamilyChooserPanel() {
		super();
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		this.add(registryPanel, gbc);

		registryPanel.addObserver(familyPanel);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weighty = 1;
		this.add(familyPanel, gbc);
	}

	public Registry getChosenRegistry() {
		return registryPanel.getChosenRegistry();
	}

	public Family getChosenFamily() {
		return familyPanel.getChosenFamily();
	}

	@Override
	public void addObserver(Observer<FamilyChoiceMessage> observer) {
		familyPanel.addObserver(observer);
	}

	@Override
	public List<Observer<FamilyChoiceMessage>> getObservers() {
		return familyPanel.getObservers();
	}

	@Override
	public void removeObserver(Observer<FamilyChoiceMessage> observer) {
		familyPanel.removeObserver(observer);
	}

	@Override
	public void notify(Observable<ProfileChoiceMessage> sender,
			ProfileChoiceMessage message) throws Exception {
		try {
			familyPanel.notify(sender, message);
		} catch (Exception e) {
			logger.error(e);
		}
	}

}
