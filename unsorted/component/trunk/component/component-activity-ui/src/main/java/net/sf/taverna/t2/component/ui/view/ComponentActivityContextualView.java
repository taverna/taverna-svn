package net.sf.taverna.t2.component.ui.view;

import java.awt.Frame;
import java.net.URL;
import java.util.List;

import javax.swing.Action;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.registry.ComponentUtil;
import net.sf.taverna.t2.component.ui.config.ComponentConfigureAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class ComponentActivityContextualView extends
		HTMLBasedActivityContextualView<ComponentActivityConfigurationBean> {

	private static Logger logger = Logger
			.getLogger(ComponentActivityContextualView.class);

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
		return new ComponentConfigureAction((ComponentActivity) getActivity(),
				owner);
	}

	@Override
	protected String getRawTableRowsHtml() {
		String html = "";

		URL registryBase = getConfigBean().getRegistryBase();
		html += "<tr><td><b>Component registry base</b></td><td>"
				+ registryBase.toString() + "</td></tr>";
		String familyName = getConfigBean().getFamilyName();
		html += "<tr><td><b>Component family</b></td><td>" + familyName
				+ "</td></tr>";
		try {
			Family family = ComponentUtil.calculateFamily(registryBase,
					familyName);
			if (family != null) {
				html += getDescriptionHtml("Family description",
						family.getDescription());
			}
		} catch (RegistryException e) {
			logger.error(e);
		}
		String componentName = getConfigBean().getComponentName();
		html += "<tr><td><b>Component name</b></td><td>" + componentName
				+ "</td></tr>";
		try {
			Component component = ComponentUtil.calculateComponent(
					registryBase, familyName, componentName);
			if (component != null) {
				html += getDescriptionHtml("Component description",
						component.getDescription());
			}
		} catch (RegistryException e) {
			logger.error(e);
		}

		Integer componentVersion = getConfigBean().getComponentVersion();
		html += "<tr><td><b>Component version</b></td><td>" + componentVersion
				+ "</td></tr>";
		try {
			Version version = ComponentUtil.calculateComponentVersion(
					registryBase, familyName, componentName, componentVersion);
			if (version != null) {
				html += getDescriptionHtml("Component version description",
						version.getDescription());
			}
		} catch (RegistryException e) {
			logger.error(e);
		}

		List<ActivityInputPortDefinitionBean> inputPortDefinitions = getConfigBean()
				.getPorts().getInputPortDefinitions();
		if (!inputPortDefinitions.isEmpty()) {
			html = html + "<tr><th>Input Port Name</th>" + "<th>Depth</th>"
					+ "</tr>";
			for (ActivityInputPortDefinitionBean bean : inputPortDefinitions) {
				html = html + "<tr><td>" + bean.getName() + "</td><td>"
						+ bean.getDepth() + "</td></tr>";
			}
		}
		List<ActivityOutputPortDefinitionBean> outputPortDefinitions = getConfigBean()
				.getPorts().getOutputPortDefinitions();
		if (!outputPortDefinitions.isEmpty()) {
			html = html + "<tr><th>Output Port Name</th>" + "<th>Depth</th>"
					+ "</tr>";
			for (ActivityOutputPortDefinitionBean bean : outputPortDefinitions) {
				html = html + "<tr><td>" + bean.getName() + "</td><td>"
						+ bean.getDepth() + "</td>" + "</tr>";
			}
		}
		return html;

	}

	private String getDescriptionHtml(String header, String description) {
		String result = "";
		if ((description != null) && !description.isEmpty()) {
			result += "<tr><td colspan=\"2\"><b>" + header + "</b></td></tr>";
			result += "<tr><td colspan=\"2\" nowrap=\"wrap\" style=\"width:100px;\">"
					+ description + "</td></tr>";
		}
		return result;

	}

}
