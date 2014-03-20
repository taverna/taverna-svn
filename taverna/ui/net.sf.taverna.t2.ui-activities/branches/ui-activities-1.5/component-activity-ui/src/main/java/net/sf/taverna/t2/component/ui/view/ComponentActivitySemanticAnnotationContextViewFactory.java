package net.sf.taverna.t2.component.ui.view;

import static net.sf.taverna.t2.workflowmodel.utils.Tools.getFirstProcessorWithActivityInputPort;
import static net.sf.taverna.t2.workflowmodel.utils.Tools.getFirstProcessorWithActivityOutputPort;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;

public class ComponentActivitySemanticAnnotationContextViewFactory implements
		ContextualViewFactory<Object> {
	private static FileManager fm = FileManager.getInstance();

	@Override
	public boolean canHandle(Object selection) {
		return getContainingComponentActivity(selection) != null;
	}

	public static ComponentActivity getContainingComponentActivity(
			Object selection) {
		if (selection instanceof ComponentActivity)
			return (ComponentActivity) selection;

		if (selection instanceof ActivityInputPort) {
			Processor p = null;
			Dataflow d = fm.getCurrentDataflow();
			p = getFirstProcessorWithActivityInputPort(d,
					(ActivityInputPort) selection);
			Activity<?> a = p.getActivityList().get(0);
			return getContainingComponentActivity(a);
		}
		if (selection instanceof ActivityOutputPort) {
			Processor p = null;
			Dataflow d = fm.getCurrentDataflow();
			p = getFirstProcessorWithActivityOutputPort(d,
					(ActivityOutputPort) selection);
			Activity<?> a = p.getActivityList().get(0);
			return getContainingComponentActivity(a);
		}
		return null;

	}

	@Override
	public List<ContextualView> getViews(Object selection) {
		return Arrays
				.<ContextualView> asList(new ComponentActivitySemanticAnnotationContextualView(
						selection));
	}

}
