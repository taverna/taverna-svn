/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.component.ui.util.Utils;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.impl.actions.SaveWorkflowAction;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ComponentSaveAction extends AbstractAction implements
		Observer<FileManagerEvent> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2391891750558659714L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ComponentSaveAction.class);

	private static Action saveWorkflowAction = new SaveWorkflowAction();

	private static FileManager fileManager = FileManager.getInstance();

	private static final String SAVE_COMPONENT = "Save component";

	public ComponentSaveAction() {
		super(SAVE_COMPONENT, ComponentServiceIcon.getIcon());
		fileManager.addObserver(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		saveWorkflowAction.actionPerformed(arg0);
	}

	@Override
	public void notify(Observable<FileManagerEvent> sender,
			FileManagerEvent message) throws Exception {
		setEnabled(/* saveWorkflowAction.isEnabled() && */Utils
				.currentDataflowIsComponent());
	}

}
