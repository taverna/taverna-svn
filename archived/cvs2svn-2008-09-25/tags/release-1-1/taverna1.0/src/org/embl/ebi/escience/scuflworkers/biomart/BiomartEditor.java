/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import org.embl.ebi.escience.scufl.*;
import org.ensembl.mart.lib.*;
import org.ensembl.mart.lib.config.*;
import org.embl.ebi.escience.scuflworkers.*;
import org.embl.ebi.escience.scuflworkers.biomart.config.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.embl.ebi.escience.scuflui.UIUtils;


/**
 * An editor for a biomart Query object attached
 * to a BiomartProcessor.
 * @author Tom Oinn
 */
public class BiomartEditor implements ProcessorEditor {

    public ActionListener getListener(Processor theProcessor) {
	final BiomartProcessor bp = (BiomartProcessor)theProcessor;
	return new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    UIUtils.createFrame(bp.getModel(),
					new QueryConfigPanel(bp),
					100, 100,
					600, 500);
		}
	    };
    }

    public String getEditorDescription() {
	return "Configure biomart query...";
    }

}
