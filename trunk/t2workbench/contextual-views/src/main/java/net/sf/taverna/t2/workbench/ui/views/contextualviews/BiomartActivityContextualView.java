package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import net.sf.taverna.t2.activities.biomart.BiomartActivityConfigurationBean;

public class BiomartActivityContextualView extends HTMLBasedActivityContextualView<BiomartActivityConfigurationBean> {

	private static final long serialVersionUID = -33919649695058443L;

	public BiomartActivityContextualView(
			BiomartActivityConfigurationBean configBean) {
		super(configBean);
	}

	@Override
	protected String getRawTableRowsHtml() {
		String html="<tr><td>Endpoint</td><td>eee</td></tr>";
		
		return html;
	}

	@Override
	protected String getViewTitle() {
		return "Biomart activity";
	}

	@Override
	protected void setNewValues() {
		// TODO Auto-generated method stub
		
	}

	
}
