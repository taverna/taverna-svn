package net.sf.taverna.t2.activities.interaction.view;

import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.InteractionActivityConfigurationBean;
import net.sf.taverna.t2.activities.interaction.actions.InteractionActivityConfigurationAction;
import net.sf.taverna.t2.activities.interaction.config.InteractionActivityConfigureAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;


@SuppressWarnings("serial")
public class InteractionActivityContextualView extends HTMLBasedActivityContextualView<InteractionActivityConfigurationBean> {
	public InteractionActivityContextualView(Activity<?> activity) {
        super(activity);
        init();
}

private void init() {
}

@Override
protected String getRawTableRowsHtml() {
        String html = "";
        html = html
        + "<tr><th>Input Port Name</th>" 
                +       "<th>Depth</th>" 
        +"</tr>";
        for (ActivityInputPortDefinitionBean bean : getConfigBean()
                        .getInputPortDefinitions()) {
                html = html + "<tr><td>" + bean.getName() + "</td><td>"
                                + bean.getDepth() + "</td></tr>";
        }
        html = html
                        + "<tr><th>Output Port Name</th>" 
                                +       "<th>Depth</th>" 
                        +"</tr>";
        for (ActivityOutputPortDefinitionBean bean : getConfigBean()
                        .getOutputPortDefinitions()) {
                html = html + "<tr><td>" + bean.getName() + "</td><td>"
                                + bean.getDepth() + "</td>" 
//                                              + "<td>" + bean.getGranularDepth()
//                              + "</td>"
                                + "</tr>";
        }
        return html;
}

@Override
public String getViewTitle() {
        return "Interaction service";
}

@Override
public Action getConfigureAction(Frame owner) {
        return new InteractionActivityConfigurationAction(
                        (InteractionActivity) getActivity(), owner);
}

@Override
public int getPreferredPosition() {
        return 100;
}

}
