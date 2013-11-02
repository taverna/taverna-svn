/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import static net.sf.taverna.t2.component.ui.menu.component.ComponentServiceCreatorAction.getNewComponentIdentification;
import static net.sf.taverna.t2.component.ui.menu.component.ComponentServiceCreatorAction.saveWorkflowAsComponent;
import static net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon.getIcon;
import static net.sf.taverna.t2.component.ui.util.Utils.currentDataflowIsComponent;
import static org.apache.log4j.Logger.getLogger;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentWorkflowCreatorAction extends AbstractAction implements
		Observer<FileManagerEvent> {
	private static final long serialVersionUID = -299685223430721587L;
	private static Logger logger = getLogger(ComponentWorkflowCreatorAction.class);
	private static FileManager fileManager = FileManager.getInstance();
	private static final String CREATE_COMPONENT = "Create component...";

	public ComponentWorkflowCreatorAction() {
		super(CREATE_COMPONENT, getIcon());
		fileManager.addObserver(this);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Dataflow d = FileManager.getInstance().getCurrentDataflow();
		try {
			Version.ID ident = getNewComponentIdentification(d.getLocalName());
			if (ident == null)
				return;

			saveWorkflowAsComponent(d, ident);
		} catch (Exception e) {
			logger.error("failed to save workflow as component", e);
		}
	}

	@Override
	public void notify(Observable<FileManagerEvent> sender,
			FileManagerEvent message) throws Exception {
		setEnabled(!currentDataflowIsComponent());
	}
}
