
/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package org.jdesktop.swingworker;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;


/**
 * This class should be used when you want the {@link org.jdesktop.swingworker.SwingWorker}
 * to block on the Event Dispatch Thread while {@link org.jdesktop.swingworker.SwingWorker}
 * finishes its task. While SwingWorker is working we display a modal dialog with a message 
 * to the user using the following code. 
 * <code>
   JDialog dialog = new JDialog(owner, true);
   swingWorker.addPropertyChangeListener(new SwingWorkerCompletionWaiter(dialog));
   swingWorker.execute();
   //the dialog will be visible until the SwingWorker is done
   dialog.setVisible(true);
   </code>
 * 
 * This class is based on an example from https://swingworker.dev.java.net/nonav/javadoc/index.html.
 *
 */
public class SwingWorkerCompletionWaiter implements PropertyChangeListener {
    private JDialog dialog;

    public SwingWorkerCompletionWaiter(JDialog dialog) {
        this.dialog = dialog;
    }

    public void propertyChange(PropertyChangeEvent event) {
        if ("state".equals(event.getPropertyName())
                && SwingWorker.StateValue.DONE == event.getNewValue()) {
            dialog.setVisible(false);
            dialog.dispose();
        }
    }
}
