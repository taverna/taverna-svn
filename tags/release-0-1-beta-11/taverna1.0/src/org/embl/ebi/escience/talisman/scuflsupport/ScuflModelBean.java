/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.talisman.scuflsupport;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.view.DotView;
import org.embl.ebi.escience.scufl.view.XScuflView;

// IO Imports
import java.io.Serializable;

import java.lang.String;



/** 
 * This bean allows you to load a ScuflModel with attached
 * XScuflView and DotView representations into a Talisman
 * bean field. It exposes the XML and Dot representations
 * by the getXScufl and getDot methods, which can therefore
 * be accessed by Talisman's bean accessor proxy fields,
 * see the documentation for the Talisman resolver for more
 * details of how to do this.
 * @author Tom Oinn
 */
public class ScuflModelBean implements Serializable {

    private ScuflModel model = null;
    private DotView dotView  = null;
    private XScuflView xscuflView = null;

    /** 
     * Must have a default constructor to comply with the
     * Talisman bean requirements. In this case, it creates
     * an empty ScuflModel, and attaches a DotView and XScuflView
     * to it.
     */
    public ScuflModelBean() {
	this.model = new ScuflModel();
	this.dotView = new DotView(model);
	this.dotView.setPortDisplay(DotView.BOUND);
	this.xscuflView = new XScuflView(model);
    }

    /**
     * Get the underlying model for this bean
     */
    public ScuflModel getModel() {
	return this.model;
    }

    /**
     * Get the XML text
     */
    public String getXScufl() {
	return this.xscuflView.getXMLText();
    }

    /** 
     * Get the Dot description
     */
    public String getDot() {
	return this.dotView.getDot();
    }

}
