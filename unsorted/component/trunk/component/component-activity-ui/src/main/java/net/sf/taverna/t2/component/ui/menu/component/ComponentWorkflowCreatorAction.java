/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.component.ui.util.Utils;
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
public class ComponentWorkflowCreatorAction extends AbstractAction  implements Observer<FileManagerEvent> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -299685223430721587L;

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
			ComponentVersionIdentification ident = ComponentServiceCreatorAction.getNewComponentIdentification(d.getLocalName());
			if (ident == null) {
				return;
			}
			
			ComponentServiceCreatorAction.saveWorkflowAsComponent(d, ident);
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
