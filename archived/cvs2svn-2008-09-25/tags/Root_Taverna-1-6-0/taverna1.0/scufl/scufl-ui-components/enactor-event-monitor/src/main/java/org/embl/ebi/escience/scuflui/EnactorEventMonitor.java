/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import javax.swing.JTextArea;

import org.embl.ebi.escience.scufl.enactor.WorkflowEventAdapter;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventListener;
import org.embl.ebi.escience.scufl.enactor.event.CollectionConstructionEvent;
import org.embl.ebi.escience.scufl.enactor.event.IterationCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.UserChangedDataEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCreationEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowFailureEvent;
import org.embl.ebi.escience.scufl.enactor.implementation.WorkflowEventDispatcher;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

/**
 * Prints events from the enactor as they occur, to be used for debugging.
 * 
 * @author Tom Oinn
 */
public class EnactorEventMonitor extends JTextArea implements UIComponentSPI {

	WorkflowEventDispatcher dispatcher = WorkflowEventDispatcher.DISPATCHER;

	WorkflowEventListener listener = null;

	public EnactorEventMonitor() {
		super();
		setLineWrap(true);
		setWrapStyleWord(true);
		setEditable(false);
		this.listener = new WorkflowEventAdapter() {
			public void processCompleted(ProcessCompletionEvent e) {
				EnactorEventMonitor.this.addText(e.toString());
			}

			public void processCompletedWithIteration(IterationCompletionEvent e) {
				EnactorEventMonitor.this.addText(e.toString());
			}

			public void processFailed(ProcessFailureEvent e) {
				EnactorEventMonitor.this.addText(e.toString());
			}

			public void collectionConstructed(CollectionConstructionEvent e) {
				EnactorEventMonitor.this.addText(e.toString());
			}

			public void workflowCreated(WorkflowCreationEvent e) {
				EnactorEventMonitor.this.addText(e.toString());
			}

			public void workflowFailed(WorkflowFailureEvent e) {
				EnactorEventMonitor.this.addText(e.toString());
			}

			public void workflowCompleted(WorkflowCompletionEvent e) {
				EnactorEventMonitor.this.addText(e.toString());
			}

			public void dataChanged(UserChangedDataEvent e) {
				EnactorEventMonitor.this.addText(e.toString());
			}
		};
	}

	private synchronized void addText(String text) {
		setText(this.getText() + "\n" + text);
		repaint();
	}

	public javax.swing.ImageIcon getIcon() {
		return TavernaIcons.inputValueIcon;
	}

	/**
	 * A name for this component
	 */
	public String getName() {
		return "DEBUG - Enactor event monitor";
	}

	public void onDisplay() {
		dispatcher.addListener(this.listener);
	}

	public void onDispose() {
		dispatcher.removeListener(this.listener);
	}

}
