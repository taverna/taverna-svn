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
import net.sf.taverna.t2.workbench.file.impl.actions.CloseWorkflowAction;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentCloseAction extends AbstractAction implements Observer<FileManagerEvent> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -153986599735293879L;

	private static Logger logger = Logger.getLogger(ComponentCloseAction.class);
	
	private static Action closeWorkflowAction = new CloseWorkflowAction();
	
	private static FileManager fileManager = FileManager.getInstance();
	
	private static final String CLOSE_COMPONENT = "Close component";
	
	public ComponentCloseAction() {
		super (CLOSE_COMPONENT, ComponentServiceIcon.getIcon());
		fileManager.addObserver(this);
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		closeWorkflowAction.actionPerformed(arg0);
	}
	
	public boolean isEnabled() {
		return Utils.currentDataflowIsComponent();
	}


	@Override
	public void notify(Observable<FileManagerEvent> sender,
			FileManagerEvent message) throws Exception {
		this.setEnabled(Utils.currentDataflowIsComponent());
	}

}
