package net.sf.taverna.t2.component.ui.view;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class ComponentContextViewFactory implements
		ContextualViewFactory<Dataflow> {

	private static final FileManager fileManager = FileManager.getInstance();

	public boolean canHandle(Object selection) {
		if (selection instanceof Dataflow) {
			Object dataflowSource = fileManager
					.getDataflowSource((Dataflow) selection);
			return dataflowSource instanceof Version.ID;
		}
		return false;
	}

	public List<ContextualView> getViews(Dataflow selection) {
		Object dataflowSource = fileManager
				.getDataflowSource((Dataflow) selection);
		return Arrays.<ContextualView> asList(new ComponentContextualView(
				(Version.ID) dataflowSource));
	}

}
