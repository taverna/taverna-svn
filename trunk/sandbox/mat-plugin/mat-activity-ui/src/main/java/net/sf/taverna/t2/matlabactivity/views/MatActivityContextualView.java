package net.sf.taverna.t2.matlabactivity.views;

import java.awt.Frame;
import javax.swing.Action;
import net.sf.taverna.t2.activities.matlab.MatActivity;
import net.sf.taverna.t2.activities.matlab.MatActivityConfigurationBean;
import net.sf.taverna.t2.matlabactivity.actions.MatActivityConfiguationAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

/**
 *
 * @author petarj
 */
public class MatActivityContextualView extends HTMLBasedActivityContextualView<MatActivityConfigurationBean> {

    private static final long serialVersionUID = -9016726600126223668L;

    public MatActivityContextualView(Activity<?> activity) {
        super(activity);
        init();
    }

    @Override
    protected String getRawTableRowsHtml() {
        StringBuilder html = new StringBuilder(
                "<tr><th>Input Port Name</th><th>Depth</th></tr>");
        for (ActivityInputPortDefinitionBean bean : getConfigBean().
                getInputPortDefinitions()) {
            html.append("<tr><td>" + bean.getName() + "</td><td>" + bean.
                    getDepth() + "</td></th>");
        }
        html.append(
                "<tr><th>Output Port Name</th><th>Depth</th><th>Granular Depth</th></tr>");
        for (ActivityOutputPortDefinitionBean bean : getConfigBean().
                getOutputPortDefinitions()) {
            html.append("<tr><td>" + bean.getName() + "</td><td>" + bean.
                    getDepth() + "</td><td>" + bean.getGranularDepth() +
                    "</td></tr>");
        }
        return html.toString();
    }

    @Override
    protected String getViewTitle() {
        return "MatPlugin contextual view";
    }

    @Override
    public Action getConfigureAction(Frame owner) {
        return new MatActivityConfiguationAction((MatActivity) getActivity(),
                owner);
    }

    private void init() {
    }
}
