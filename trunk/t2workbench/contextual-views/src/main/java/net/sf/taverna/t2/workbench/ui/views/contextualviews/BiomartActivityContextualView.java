package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import net.sf.taverna.t2.activities.biomart.BiomartActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartServiceXMLHandler;

public class BiomartActivityContextualView extends HTMLBasedActivityContextualView<BiomartActivityConfigurationBean> {

	private static final long serialVersionUID = -33919649695058443L;

	public BiomartActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected String getRawTableRowsHtml() {
		MartQuery q = MartServiceXMLHandler.elementToMartQuery(getConfigBean().getQuery(), null);
		String html="<tr><td>Location:</td><td>"+q.getMartService().getLocation()+"</td></tr>";
		html+="<tr><td>Dataset</td><td>"+q.getMartDataset().getDisplayName()+"</td></tr>";
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
