package net.sf.taverna.t2.component.ui.serviceprovider;

import static java.util.Arrays.asList;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateRegistry;
import static org.apache.log4j.Logger.getLogger;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.panel.RegistryAndFamilyChooserPanel;
import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.CustomizedConfigurePanelProvider;

import org.apache.log4j.Logger;

public class ComponentServiceProvider extends
		AbstractConfigurableServiceProvider<ComponentServiceProviderConfig>
		implements
		CustomizedConfigurePanelProvider<ComponentServiceProviderConfig> {
	private static final URI providerId = URI
			.create("http://taverna.sf.net/2012/service-provider/component");
	private static Logger logger = getLogger(ComponentServiceProvider.class);

	public ComponentServiceProvider() {
		super(new ComponentServiceProviderConfig());
	}

	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@Override
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		ComponentServiceProviderConfig config = getConfiguration();

		Registry registry;
		try {
			registry = calculateRegistry(config.getRegistryBase());
		} catch (RegistryException e) {
			logger.error(e);
			callBack.fail("Unable to read components", e);
			return;
		}

		List<ComponentServiceDesc> results = new ArrayList<ComponentServiceDesc>();

		try {
			for (Family family : registry.getComponentFamilies()) {
				// TODO get check on family name in there
				if (family.getName().equals(config.getFamilyName()))
					for (Component component : family.getComponents())
						try {
							Version.ID ident = new ComponentVersionIdentification(
									config.getRegistryBase(), family.getName(),
									component.getName(), component
											.getComponentVersionMap().lastKey());
							ComponentServiceDesc newDesc = new ComponentServiceDesc(
									ident);
							results.add(newDesc);
						} catch (Exception e) {
							logger.error(e);
						}
				callBack.partialResults(results);
				callBack.finished();
			}
		} catch (RegistryException e) {
			logger.error(e);
			callBack.fail("Unable to read components", e);
		}
	}

	/**
	 * Icon for service provider
	 */
	@Override
	public Icon getIcon() {
		return ComponentServiceIcon.getIcon();
	}

	/**
	 * Name of service provider, appears in right click for 'Remove service
	 * provider'
	 */
	@Override
	public String getName() {
		return "Component service";
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getId() {
		return providerId.toASCIIString();
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		return asList(getConfiguration().getRegistryBase().toString(),
				getConfiguration().getFamilyName());
	}

	@Override
	public void createCustomizedConfigurePanel(
			CustomizedConfigureCallBack<ComponentServiceProviderConfig> callBack) {
		RegistryAndFamilyChooserPanel panel = new RegistryAndFamilyChooserPanel();

		if (showConfirmDialog(null, panel, "Component family import",
				OK_CANCEL_OPTION) != OK_OPTION)
			return;

		Registry chosenRegistry = panel.getChosenRegistry();
		Family chosenFamily = panel.getChosenFamily();
		if ((chosenRegistry == null) || (chosenFamily == null))
			return;

		ComponentServiceProviderConfig newConfig = new ComponentServiceProviderConfig();
		newConfig.setRegistryBase(chosenRegistry.getRegistryBase());
		newConfig.setFamilyName(chosenFamily.getName());
		callBack.newProviderConfiguration(newConfig);
	}
}
