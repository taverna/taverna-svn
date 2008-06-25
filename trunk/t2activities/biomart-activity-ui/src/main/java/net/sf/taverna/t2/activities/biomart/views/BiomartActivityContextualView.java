package net.sf.taverna.t2.activities.biomart.views;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.activities.biomart.BiomartActivityConfigurationBean;
import net.sf.taverna.t2.activities.biomart.actions.BiomartActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartServiceXMLHandler;
import org.biomart.martservice.query.Attribute;
import org.biomart.martservice.query.Filter;

public class BiomartActivityContextualView extends HTMLBasedActivityContextualView<BiomartActivityConfigurationBean> {

	private static final long serialVersionUID = -33919649695058443L;

	public BiomartActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected String getRawTableRowsHtml() {
		MartQuery q = MartServiceXMLHandler.elementToMartQuery(getConfigBean().getQuery(), null);
		String html="<tr><td>Location</td><td>"+q.getMartService().getLocation()+"</td></tr>";
		boolean firstFilter=true;
		for (Filter filter : q.getQuery().getFilters()) {
			html+=firstFilter ? "<tr><td>Filter</td><td>" : "<tr><td></td></td>";
			firstFilter=false;
			html+=filter.getName()+"</td></tr>";
		}
		boolean firstAttribute=true;
		for (Attribute attribute : q.getQuery().getAttributes()) {
			html+=firstAttribute ? "<tr><td>Attribute</td><td>" : "<tr><td></td><td>";
			firstAttribute=false;
			html+=attribute.getName()+"</td></tr>";
		}
		html+="<tr><td>Dataset</td><td>"+q.getMartDataset().getDisplayName()+"</td></tr>";
		return html;
	}

	@Override
	protected String getViewTitle() {
		return "Biomart activity";
	}

	@SuppressWarnings("serial")
	@Override
	public Action getConfigureAction() {
		return new BiomartActivityConfigurationAction((BiomartActivity)getActivity()) {

			@Override
			public void actionPerformed(ActionEvent action) {
				super.actionPerformed(action);
				refreshView();
			}
			
		};
	}
	
	

	
}
