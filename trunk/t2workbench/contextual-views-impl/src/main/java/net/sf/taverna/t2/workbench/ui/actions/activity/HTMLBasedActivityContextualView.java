package net.sf.taverna.t2.workbench.ui.actions.activity;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.annotationbeans.HostInstitution;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public abstract class HTMLBasedActivityContextualView<ConfigBean> extends
		ActivityContextualView<ConfigBean> {
	private JEditorPane editorPane;

	public HTMLBasedActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected JComponent getMainFrame() {
		return panelForHtml(buildHtml());
	}

	private String buildHtml() {
		String html = "<html><head>" + getStyle() + "</head><body>";
		html += buildTableOpeningTag();
		html += "<tr><td colspan=2>" + getViewTitle() + "</td></tr>";
		html += getRawTableRowsHtml() + "</table>";
		html += "</body></html>";
		return html;
	}

	private String buildTableOpeningTag() {
		String result = "<table ";
		Map<String, String> props = getTableProperties();
		for (String key : props.keySet()) {
			result += key + "=\"" + props.get(key) + "\" ";
		}
		result += ">";
		return result;
	}

	protected abstract String getRawTableRowsHtml();

	protected Map<String, String> getTableProperties() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("border", "1");
		return result;
	}

	public String getBackgroundColour() {
		// FIXME would prefer instanceof but no class def found error was thrown
		// even though the pom had the activity in it - spring peoblem?
		if (getActivity().getClass().getName().equalsIgnoreCase(
				"net.sf.taverna.t2.activities.localworker.LocalworkerActivity")) {
			if (checkAnnotations()) {
				String colour = (String) ColourManager
						.getInstance()
						.getPropertyMap()
						.get(
								"net.sf.taverna.t2.activities.beanshell.BeanshellActivity");
				return colour;
			}
		}
		String colour = (String) ColourManager.getInstance().getPropertyMap()
				.get(getActivity().getClass().getName());
		return colour == null ? "#ffffff" : colour;
	}

	private boolean checkAnnotations() {
		for (AnnotationChain chain : getActivity().getAnnotations()) {
			for (AnnotationAssertion<?> assertion : chain.getAssertions()) {
				Object detail = assertion.getDetail();
				System.out.println(detail.getClass().getName());
				if (detail instanceof HostInstitution) {
					// this is a user defined localworker so use the beanshell
					// colour!
					return true;
				}
			}
		}
		return false;
	}

	protected String getStyle() {
		String style = "<style type='text/css'>";
		style += "table {align:center; border:solid black 1px; background-color:"
				+ getBackgroundColour()
				+ ";width:100%; height:100%; overflow:auto;}";
		style += "</style>";
		return style;
	}

	protected JPanel panelForHtml(String html) {
		JPanel result = new JPanel();

		result.setLayout(new BorderLayout());
		editorPane = new JEditorPane("text/html", html);
		editorPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(editorPane);
		result.add(scrollPane, BorderLayout.CENTER);
		return result;
	}

	/**
	 * Update the html view with the latest information in the configuration
	 * bean
	 */
	public void refreshView() {
		editorPane.setText(buildHtml());
	}
}
