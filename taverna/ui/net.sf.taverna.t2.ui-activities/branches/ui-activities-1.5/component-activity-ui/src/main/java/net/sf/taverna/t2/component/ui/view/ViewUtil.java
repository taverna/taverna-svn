/**
 * 
 */
package net.sf.taverna.t2.component.ui.view;

import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateComponent;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateComponentVersion;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateFamily;
import static org.apache.log4j.Logger.getLogger;

import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.registry.ComponentUtil;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

/**
 * @author alanrw
 *
 */
public class ViewUtil {
	
	private static Logger logger = getLogger(ViewUtil.class);
	
	private static final String VERSION_DESCRIPTION_LABEL = "Component version description";
	private static final String COMPONENT_DESCRIPTION_LABEL = "Component description";
	private static final String FAMILY_DESCRIPTION_LABEL = "Family description";
	
	private static String plainFormat = "<tr><td><b>%1$s</b></td><td nowrap=\"wrap\" style=\"width:100px;\">%2$s</td></tr>";

	private static String headerFormat = "<tr><th>%1$s</th><th>%2$s</th></tr>";
	
	private static String rowFormat = "<tr><td><b>%1$s</b></td><td>%2$s</td></tr>";
	private static String rowLinkFormat = "<tr><td><b>%1$s</b></td><td><a href=\"%3$s\">%2$s</a></td></tr>";	
	private static String descriptionFormat = "<tr><td colspan=\"2\"><b>%1$s</b></td></tr><tr><td colspan=\"2\" nowrap=\"wrap\" style=\"width:100px;\">%2$s</td></tr>";


	
	public static String getRawTablesHtml(Version.ID id) {
		StringBuilder html = new StringBuilder();

		URL registryBase = id.getRegistryBase();
		String registryLink = null;
		if (registryBase.getProtocol().startsWith("http")) {
			registryLink = registryBase.toExternalForm();
		}
		appendRow(html, "Component registry base", registryBase, registryLink);

		String familyName = id.getFamilyName();
		appendRow(html, "Component family", familyName, null);
		try {
			Family family = calculateFamily(registryBase, familyName);
			if (family != null)
				appendDescriptionHtml(html, FAMILY_DESCRIPTION_LABEL,
						family.getDescription());
		} catch (RegistryException e) {
			logger.error("failed to get component family description", e);
		}

		String componentName = id.getComponentName();
		URL helpURL = null;
		try {
			helpURL = ComponentUtil.calculateComponentVersion(id).getHelpURL();
		}
		catch (RegistryException e) {
			logger.error(e);
		}
		String helpLink = null;
		if (helpURL != null) {
			helpLink = helpURL.toExternalForm();
		}
		appendRow(html, "Component name", componentName, helpLink);
		try {
			Component component = calculateComponent(registryBase, familyName,
					componentName);
			if (component != null)
				appendDescriptionHtml(html, COMPONENT_DESCRIPTION_LABEL,
						component.getDescription());
		} catch (RegistryException e) {
			logger.error("failed to get component description", e);
		}

		Integer componentVersion = id.getComponentVersion();

		appendRow(html, "Component version", componentVersion, helpLink);
		try {
			Version version = calculateComponentVersion(registryBase,
					familyName, componentName, componentVersion);
			if (version != null)
				appendDescriptionHtml(html, VERSION_DESCRIPTION_LABEL,
						version.getDescription());
		} catch (RegistryException e) {
			logger.error("failed to get component version description", e);
		}

		if (id instanceof ComponentActivityConfigurationBean) {
			ComponentActivityConfigurationBean config = (ComponentActivityConfigurationBean) id;
		try {
		List<ActivityInputPortDefinitionBean> inputPortDefinitions = config
				.getPorts().getInputPortDefinitions();
		if (!inputPortDefinitions.isEmpty()) {
			appendHeaderRow(html, "Input Port Name", "Depth");
			for (ActivityInputPortDefinitionBean bean : inputPortDefinitions)
				appendPlainRow(html, bean.getName(), bean.getDepth());
		}
		List<ActivityOutputPortDefinitionBean> outputPortDefinitions = config
				.getPorts().getOutputPortDefinitions();
		if (!outputPortDefinitions.isEmpty()) {
			appendHeaderRow(html, "Output Port Name", "Depth");
			for (ActivityOutputPortDefinitionBean bean : outputPortDefinitions)
				appendPlainRow(html, bean.getName(), bean.getDepth());
		}
		} catch (RegistryException e) {
			logger.error("failed to get component port description", e);
		}
		}
			return html.toString();
		
	}

	private static void appendRow(StringBuilder html, Object label, Object value, String link) {
		if (link == null) {
			html.append(String.format(rowFormat,label.toString(), value.toString()));
		} else {
			html.append(String.format(rowLinkFormat,label.toString(), value.toString(), link));			
		}
	}

	private static void appendHeaderRow(StringBuilder html, Object label1,
			Object label2) {
		html.append(String.format(headerFormat,  label1.toString(), label2.toString()));
	}

	private static void appendPlainRow(StringBuilder html, Object value1, Object value2) {
		html.append(String.format(plainFormat, value1.toString(), value2.toString()));
	}

	private static void appendDescriptionHtml(StringBuilder html, String header,
			String description) {
		if ((description != null) && !description.isEmpty())
			html.append(String.format(descriptionFormat, header,description));
	}

}
