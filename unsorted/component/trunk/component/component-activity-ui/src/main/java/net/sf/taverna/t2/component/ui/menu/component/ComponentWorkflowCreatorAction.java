/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.component.ui.util.Utils;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public class ComponentWorkflowCreatorAction extends AbstractAction  implements Observer<FileManagerEvent> {
	
	private static Logger logger = Logger.getLogger(ComponentWorkflowCreatorAction.class);

	private static FileManager fileManager = FileManager.getInstance();
		
	private static final String CREATE_COMPONENT = "Create component...";
	
	public ComponentWorkflowCreatorAction() {
		super(CREATE_COMPONENT, ComponentServiceIcon.getIcon());
		fileManager.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Dataflow d = FileManager.getInstance().getCurrentDataflow();
		try {
			ComponentServiceCreatorAction.saveWorkflowAsComponent(d, d.getLocalName());
		} catch (Exception e1) {
			logger.error(e1);
		}
	}

	@Override
	public void notify(Observable<FileManagerEvent> sender,
			FileManagerEvent message) throws Exception {
		this.setEnabled(!Utils.currentDataflowIsComponent());
	}

}
