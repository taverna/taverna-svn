package net.sf.taverna.t2.component.ui.view;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.lang.ui.HtmlUtils;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

@SuppressWarnings("serial")
public class ComponentContextualView extends ContextualView {

	private JEditorPane editorPane;
	private final Version.ID component;

	public ComponentContextualView(Version.ID component) {
		this.component = component;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		editorPane = HtmlUtils.createEditorPane(buildHtml());
		return HtmlUtils.panelForHtml(editorPane);
	}

	private String buildHtml() {
		String html = HtmlUtils.getHtmlHead(getBackgroundColour());
		html += HtmlUtils.buildTableOpeningTag();

		html += "<tr><td>Registry base</td><td title=\"Hello Alan\">"
				+ component.getRegistryBase().toString() + "</td></tr>";
		html += "<tr><td>Family</td><td>" + component.getFamilyName()
				+ "</td></tr>";
		html += "<tr><td>Name</td><td>" + component.getComponentName()
				+ "</td></tr>";
		html += "<tr><td>Version</td><td>" + component.getComponentVersion()
				+ "</td></tr>";

		html += "</table>";
		html += "</body></html>";
		return html;
	}

	public String getBackgroundColour() {
		Color colour = ColourManager.getInstance().getPreferredColour(
				"net.sf.taverna.t2.component.registry.Component");
		return String.format("#%1$2x%2$2x%3$2x", colour.getRed(),
				colour.getGreen(), colour.getBlue());
	}

	@Override
	public int getPreferredPosition() {
		return 50;
	}

	private static int MAX_LENGTH = 50;

	private String limitName(String fullName) {
		if (fullName.length() > MAX_LENGTH) {
			return (fullName.substring(0, MAX_LENGTH - 3) + "...");
		}
		return fullName;
	}

	@Override
	public String getViewTitle() {
		return "Component " + limitName(component.getComponentName());
	}

	@Override
	public void refreshView() {
		editorPane.setText(buildHtml());
		repaint();
	}

}
