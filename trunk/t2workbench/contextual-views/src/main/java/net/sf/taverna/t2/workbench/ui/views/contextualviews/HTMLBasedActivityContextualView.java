package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public abstract class HTMLBasedActivityContextualView<ConfigBean> extends ActivityView<ConfigBean>
{
	public HTMLBasedActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected JComponent getMainFrame() {
		String html = buildHtml();
		String style = getStyle();
		return panelForHtml(style+html);
	}

	private String buildHtml() {
		String html=buildTableOpeningTag();
		html+="<tr><td colspan=2>"+getViewTitle()+"</td></tr>";
		html+=getRawTableRowsHtml()+"</table>";
		return html;
	}

	private String buildTableOpeningTag() {
		String result="<table ";
		Map<String,String> props=getTableProperties();
		for (String key : props.keySet()) {
			result+=key+"=\""+props.get(key)+"\" ";
		}
		result+=">";
		return result;
	}

	protected abstract String getRawTableRowsHtml();
	
	protected Map<String,String> getTableProperties() {
		Map<String,String> result = new HashMap<String,String>();
		result.put("width", "100%");
		result.put("bgcolor", getBackgroundColour());
		result.put("border", "1");
		result.put("align", "center");
		return result;
	}
	
	public String getBackgroundColour() {
		return "gray";
	}

	protected String getStyle() {
		String style = "<style type='text/css'>";
		style+="table {align:center}";
		style += "</style>";
		return style;
	}
	

	protected JPanel panelForHtml(String html) {
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());
		JEditorPane editorPane = new JEditorPane("text/html",html);
		editorPane.setEditable(false);
		result.add(editorPane,BorderLayout.CENTER);
		return result;
	}
}
