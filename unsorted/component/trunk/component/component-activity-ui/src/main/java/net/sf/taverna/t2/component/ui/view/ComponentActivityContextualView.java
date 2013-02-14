package net.sf.taverna.t2.component.ui.view;

import java.awt.Frame;
import java.net.URL;

import javax.swing.Action;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentUtil;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.ui.config.ComponentConfigureAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;

@SuppressWarnings("serial")
public class ComponentActivityContextualView extends HTMLBasedActivityContextualView<ComponentActivityConfigurationBean> {

	private static Logger logger = Logger.getLogger(ComponentActivityContextualView.class);


	public ComponentActivityContextualView(ComponentActivity activity) {
		super(activity);
		init();
	}

	private void init() {
	}

	@Override
	public String getViewTitle() {
		return "Component service";
	}

	/**
	 * View position hint
	 */
	@Override
	public int getPreferredPosition() {
		// We want to be on top
		return 100;
	}

	@Override
	public Action getConfigureAction(final Frame owner) {
		return new ComponentConfigureAction((ComponentActivity) getActivity(), owner);
	}

	@Override
	protected String getRawTableRowsHtml() {
		String html = "";

		URL registryBase = getConfigBean().getRegistryBase();
		html += "<tr><td><b>Component registry base</b></td><td>" + registryBase.toString() + "</td></tr>";
		String familyName = getConfigBean().getFamilyName();
		html += "<tr><td><b>Component family</b></td><td>" + familyName + "</td></tr>";
		try {
			ComponentFamily family = ComponentUtil.calculateFamily(registryBase, familyName);
			if (family != null) {
				html += getDescriptionHtml("Family description", family.getDescription());
			}
		} catch (ComponentRegistryException e) {
			logger.error(e);
		}
		String componentName = getConfigBean().getComponentName();
		html += "<tr><td><b>Component name</b></td><td>" + componentName + "</td></tr>";
		try {
			Component component = ComponentUtil.calculateComponent(registryBase, familyName, componentName);
			if (component != null) {
				html += getDescriptionHtml("Component description", component.getDescription());
			}
		} catch (ComponentRegistryException e) {
			logger.error(e);
		}
		
		Integer componentVersion = getConfigBean().getComponentVersion();
		html += "<tr><td><b>Component version</b></td><td>" + componentVersion + "</td></tr>";
		try {
			ComponentVersion version = ComponentUtil.calculateComponentVersion(registryBase, familyName, componentName, componentVersion);
			if (version != null) {
				html += getDescriptionHtml("Component version description", version.getDescription());
			}
		} catch (ComponentRegistryException e) {
			logger.error(e);
		}

		return html;
	}
	
	private String getDescriptionHtml(String header, String description) {
		String result = "";
		if ((description != null) && !description.isEmpty()) {
			result += "<tr><td colspan=\"2\"><b>" + header + "</b></td></tr>";
			result += "<tr><td colspan=\"2\" nowrap=\"wrap\" style=\"width:100px;\">" + description + "</td></tr>";
		}
		return result;
		
	}

}
