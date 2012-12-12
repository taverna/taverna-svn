package net.sf.taverna.t2.component.ui.view;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.lang.ui.HtmlUtils;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

@SuppressWarnings("serial")
public class ComponentContextualView extends ContextualView {

	private JEditorPane editorPane;
	private final ComponentVersionIdentification component;

	public ComponentContextualView(ComponentVersionIdentification component) {
		this.component = component;
		initView();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#getMainFrame()
	 */
	@Override
	public JComponent getMainFrame() {
		editorPane = HtmlUtils.createEditorPane(buildHtml());
		return HtmlUtils.panelForHtml(editorPane);
	}

	private String buildHtml() {
		String html = HtmlUtils.getHtmlHead(getBackgroundColour());
		html += HtmlUtils.buildTableOpeningTag();

		html += "<tr><td>Registry base</td><td>" + component.getRegistryBase().toString() + "</td></tr>";
		html += "<tr><td>Family</td><td>" + component.getFamilyName() + "</td></tr>";
		html += "<tr><td>Name</td><td>" + component.getComponentName() + "</td></tr>";
		html += "<tr><td>Version</td><td>" + component.getComponentVersion() + "</td></tr>";

		html += "</table>";
		html += "</body></html>";
		return html;
	}

	public String getBackgroundColour() {
		Color colour = ColourManager.getInstance().getPreferredColour("net.sf.taverna.t2.component.registry.Component");
		return "#" + Integer.toHexString(colour.getRed()) +
				Integer.toHexString(colour.getGreen()) +
				Integer.toHexString(colour.getBlue());
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#getPreferredPosition()
	 */
	@Override
	public int getPreferredPosition() {
		return 100;
	}

	private static int MAX_LENGTH = 50;

	private String limitName(String fullName) {
		if (fullName.length() > MAX_LENGTH) {
			return (fullName.substring(0, MAX_LENGTH - 3) + "...");
		}
		return fullName;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#getViewTitle()
	 */
	@Override
	public String getViewTitle() {
		return "Component " + limitName(component.getComponentName());
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#refreshView()
	 */
	@Override
	public void refreshView() {
		editorPane.setText(buildHtml());
		repaint();
	}

}
