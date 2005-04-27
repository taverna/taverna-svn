/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import javax.swing.JTextArea;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.view.XScuflView;

import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import java.lang.String;

import org.embl.ebi.escience.scufl.enactor.implementation.*;

import org.embl.ebi.escience.scufl.enactor.*;
import org.embl.ebi.escience.scufl.enactor.event.*;
import org.apache.commons.discovery.tools.Service;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.resource.ClassLoaders;
import java.util.*;

/**
 * Prints events from the enactor as they occur, to be used
 * for debugging.
 * @author Tom Oinn
 */
public class EnactorEventMonitor extends JTextArea
    implements ScuflModelEventListener,
	       ScuflUIComponent {
    
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

    public void attachToModel(ScuflModel model) {
	dispatcher.addListener(this.listener);
    }
    
    public void detachFromModel() {
	dispatcher.removeListener(this.listener);
    }
    
    public void receiveModelEvent(ScuflModelEvent event) {
	//
    }
    
    public synchronized void addText(String text) {
	setText(this.getText()+"\n"+text);
	repaint();
    }
    
    public javax.swing.ImageIcon getIcon() {
	return ScuflIcons.inputValueIcon;
    }

    /**
     * A name for this component
     */
    public String getName() {
	return "DEBUG - Enactor event monitor";
    }


}
