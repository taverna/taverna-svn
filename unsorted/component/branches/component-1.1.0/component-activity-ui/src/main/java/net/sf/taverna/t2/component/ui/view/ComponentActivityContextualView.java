package net.sf.taverna.t2.component.ui.view;

import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateComponent;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateComponentVersion;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateFamily;
import static org.apache.log4j.Logger.getLogger;

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
import net.sf.taverna.t2.component.ui.config.ComponentConfigureAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class ComponentActivityContextualView extends
		HTMLBasedActivityContextualView<ComponentActivityConfigurationBean> {
	private static final String VERSION_DESCRIPTION_LABEL = "Component version description";
	private static final String COMPONENT_DESCRIPTION_LABEL = "Component description";
	private static final String FAMILY_DESCRIPTION_LABEL = "Family description";
	private static Logger logger = getLogger(ComponentActivityContextualView.class);

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
	public Action getConfigureAction(Frame owner) {
		return new ComponentConfigureAction((ComponentActivity) getActivity(),
				owner);
	}

	@Override
	protected String getRawTableRowsHtml() {
		StringBuilder html = new StringBuilder();

		URL registryBase = getConfigBean().getRegistryBase();
		appendRow(html, "Component registry base", registryBase);

		String familyName = getConfigBean().getFamilyName();
		appendRow(html, "Component family", familyName);
		try {
			Family family = calculateFamily(registryBase, familyName);
			if (family != null)
				appendDescriptionHtml(html, FAMILY_DESCRIPTION_LABEL,
						family.getDescription());
		} catch (RegistryException e) {
			logger.error("failed to get component family description", e);
		}

		String componentName = getConfigBean().getComponentName();
		appendRow(html, "Component name", componentName);
		try {
			Component component = calculateComponent(registryBase, familyName,
					componentName);
			if (component != null)
				appendDescriptionHtml(html, COMPONENT_DESCRIPTION_LABEL,
						component.getDescription());
		} catch (RegistryException e) {
			logger.error("failed to get component description", e);
		}

		Integer componentVersion = getConfigBean().getComponentVersion();
		appendRow(html, "Component version", componentVersion);
		try {
			Version version = calculateComponentVersion(registryBase,
					familyName, componentName, componentVersion);
			if (version != null)
				appendDescriptionHtml(html, VERSION_DESCRIPTION_LABEL,
						version.getDescription());
		} catch (RegistryException e) {
			logger.error("failed to get component version description", e);
		}

		List<ActivityInputPortDefinitionBean> inputPortDefinitions = getConfigBean()
				.getPorts().getInputPortDefinitions();
		if (!inputPortDefinitions.isEmpty()) {
			appendHeaderRow(html, "Input Port Name", "Depth");
			for (ActivityInputPortDefinitionBean bean : inputPortDefinitions)
				appendPlainRow(html, bean.getName(), bean.getDepth());
		}
		List<ActivityOutputPortDefinitionBean> outputPortDefinitions = getConfigBean()
				.getPorts().getOutputPortDefinitions();
		if (!outputPortDefinitions.isEmpty()) {
			appendHeaderRow(html, "Output Port Name", "Depth");
			for (ActivityOutputPortDefinitionBean bean : outputPortDefinitions)
				appendPlainRow(html, bean.getName(), bean.getDepth());
		}
		return html.toString();
	}

	private void appendRow(StringBuilder html, Object label, Object value) {
		html.append("<tr><td><b>").append(label).append("</b></td><td>")
				.append(value).append("</td></tr>");
	}

	private void appendHeaderRow(StringBuilder html, Object label1,
			Object label2) {
		html.append("<tr><th>").append(label1).append("</th><th>")
				.append(label2).append("</th></tr>");
	}

	private void appendPlainRow(StringBuilder html, Object value1, Object value2) {
		html.append("<tr><td>").append(value1).append("</td><td>")
				.append(value2).append("</td></tr>");
	}

	private void appendDescriptionHtml(StringBuilder html, String header,
			String description) {
		if ((description != null) && !description.isEmpty())
			html.append("<tr><td colspan=\"2\"><b>")
					.append(header)
					.append("</b></td></tr>")
					.append("<tr><td colspan=\"2\" nowrap=\"wrap\" style=\"width:100px;\">")
					.append(description).append("</td></tr>");
	}
}
