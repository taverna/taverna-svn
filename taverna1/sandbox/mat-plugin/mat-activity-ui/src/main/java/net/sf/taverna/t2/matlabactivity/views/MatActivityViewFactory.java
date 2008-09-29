package net.sf.taverna.t2.matlabactivity.views;

import net.sf.taverna.t2.activities.matlab.MatActivity;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

/**
 *
 * @author petarj
 */
public class MatActivityViewFactory implements 
        ContextualViewFactory<MatActivity> {

    public ContextualView getView(MatActivity selection) {
        MatActivityContextualView contextualView = new MatActivityContextualView(
                selection);
        return contextualView;
    }

    public boolean canHandle(Object selection) {
        return selection.getClass().isAssignableFrom(MatActivity.class);
    }
}
