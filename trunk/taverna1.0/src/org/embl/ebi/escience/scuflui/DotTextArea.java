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
import org.embl.ebi.escience.scufl.view.DotView;

import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import java.lang.String;



/**
 * A swing component that provides a textual dot representation
 * corresponding to a given ScuflModel instance
 * @author Tom Oinn
 */
public class DotTextArea extends JTextArea
    implements ScuflModelEventListener,
	       ScuflUIComponent {
    
    private DotView dot = null;
    private ScuflModel model = null;

    public DotTextArea() {
	super();
	setLineWrap(true);
	setWrapStyleWord(true);
	setEditable(false);
    }

    public void attachToModel(ScuflModel model) {
	if (this.model == null) {
	    this.dot = new DotView(model);
	    model.addListener(this);
	    updateText();
	}
    }
    
    public javax.swing.ImageIcon getIcon() {
	return ScuflIcons.inputValueIcon;
    }

    public void detachFromModel() {
	if (this.model != null) {
	    model.removeListener(this);
	    model.removeListener(dot);
	    this.model = null;
	    this.dot = null;
	    updateText();
	}
    }

    private void updateText() {
	if (this.dot != null) {
	    setText(dot.getDot());
	}
	else {
	    setText(null);
	}
	repaint();
    }

    private int updateStatus = 0;
    public void receiveModelEvent(ScuflModelEvent event) {
	if (updateStatus == 0) {
	    updateStatus = 1;
	    while (updateStatus != 0) {
		updateText();
		if (updateStatus == 2) {
		    updateStatus = 1;
		}
		else {
		    updateStatus = 0;
		}
	    }
	}
	else {
	    updateStatus = 2;
	}
    }
    
    /**
     * A name for this component
     */
    public String getName() {
	return "Dot text";
    }


}
