package net.sf.taverna.t2.component.ui.serviceprovider;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.local.LocalComponentRegistry;
import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentComponentRegistry;
import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.CustomizedConfigurePanelProvider;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;

import org.apache.log4j.Logger;

public class ComponentServiceProvider extends
	AbstractConfigurableServiceProvider<ComponentServiceProviderConfig> implements
	CustomizedConfigurePanelProvider<ComponentServiceProviderConfig> {
	
	private static final String T2FLOW = ".t2flow";

	private static final URI providerId = URI
		.create("http://example.com/2011/service-provider/component");
	
	private static Logger logger = Logger.getLogger(ComponentServiceProvider.class);
	
	MyExperimentClient myExperimentClient = new MyExperimentClient(logger);
	
	public ComponentServiceProvider() {
		super(new ComponentServiceProviderConfig());
	}

	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@SuppressWarnings("unchecked")
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		ComponentServiceProviderConfig config = getConfiguration();
		
		ComponentRegistry registry = null;
		
		if (config.getRegistryBase().getProtocol().startsWith("http")) {
			// TODO This needs improving
			registry = MyExperimentComponentRegistry.getComponentRegistry(config.getRegistryBase());
		}
		else {
			registry = LocalComponentRegistry.getComponentRegistry(config.getRegistryBase());
		}

		List<ComponentServiceDesc> results = new ArrayList<ComponentServiceDesc>();
		
		try {
			for (ComponentFamily family : registry.getComponentFamilies()) {
				
				// TODO get check on family name in there
			if (family.getName().equals(config.getFamilyName())) {
					for (Component component : family.getComponents()) {
						ComponentServiceDesc newDesc = new ComponentServiceDesc();
						newDesc.setComponentName(component.getName());
						newDesc.setComponentVersion(component.getComponentVersionMap().lastKey());
						newDesc.setFamilyName(family.getName());
						newDesc.setRegistryBase(config.getRegistryBase());
						results.add(newDesc);
					}
			}
					callBack.partialResults(results);
					callBack.finished();
			}
		} catch (ComponentRegistryException e) {
			logger.error(e);
			callBack.fail("Unable to read components", e);
		}
	}

	/**
	 * Icon for service provider
	 */
	public Icon getIcon() {
		return ComponentServiceIcon.getIcon();
	}

	/**
	 * Name of service provider, appears in right click for 'Remove service
	 * provider'
	 */
	public String getName() {
		return "Component service";
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public String getId() {
		return providerId.toASCIIString();
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		return Arrays.asList(new Object[] {getConfiguration().getRegistryBase().toString(), getConfiguration().getFamilyName()});
	}

	@Override
	public void createCustomizedConfigurePanel(
			CustomizedConfigureCallBack<ComponentServiceProviderConfig> callBack) {
		
		ComponentFamilyChooserPanel panel = new ComponentFamilyChooserPanel(false);
				
		int result = JOptionPane.showConfirmDialog(null, panel, "Component family import", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			
			ComponentServiceProviderConfig newConfig = panel.getConfig();
			if (newConfig != null) {
				callBack.newProviderConfiguration(newConfig);
			}
		}
		return;

	}

}
