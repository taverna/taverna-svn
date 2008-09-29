package org.embl.ebi.escience.scuflui.renderers;


import javax.swing.JComponent;

import org.embl.ebi.escience.baclava.DataThing;

import cht.svista.SeqVISTA;

/**
 * Uses the SeqVista renderer to draw an EMBL or SwissProt sequence
 */
public class SeqVistaRenderer extends AbstractRenderer.ByMimeType {
    
    private String seqType = "fasta";
    // 0 = auto, 1 = nucleotide, 2 = protein
    private int np = 0;

    public SeqVistaRenderer() {
	super("SeqVista");
    }
    
    public boolean isTerminal() {
	return true;
    }
    
    public boolean canHandle(RendererRegistry renderers,
			     Object userObject,
			     String mimeType) {
	if (mimeType.matches(".*chemical/x-swissprot.*")) {
	    seqType = "embl";
	    np = 2;
	    return true;
	}
	else if (mimeType.matches(".*chemical/x-embl-dl-nucleotide.*")) {
	    seqType = "embl";
	    np = 1;
	    return true;
	}
	else if (mimeType.matches(".*chemical/x-fasta.*")) {
	    seqType = "fasta";
	    np = 0;
	    return true;
	}
	else if (mimeType.matches(".*chemical/x-ppd.*")) {
	    seqType = "ppd";
	    return true;
	}
	else if (mimeType.matches(".*chemical/seq-na-genbank.*")) {
	    seqType = "auto";
	    np = 1;
	    return true;
	}
	else if (mimeType.matches(".*chemical/seq-aa-genpept.*")) {
	    seqType = "auto";
	    np = 2;
	    return true;
	}
	return false;
    }
    
    public JComponent getComponent(RendererRegistry renderers,
				   DataThing dataThing) 
	throws RendererException {
	SeqVISTA vista = new SeqVISTA() {
		public java.awt.Dimension getPreferredSize() {
		    return new java.awt.Dimension(100,100);
		}
	    };
	try {
	    System.out.println((String)dataThing.getDataObject());
	    System.out.println(seqType);
	    vista.loadFromText((String)dataThing.getDataObject(), false, seqType, np );
	    return vista;
	}
	catch (Exception ex) {
	    RendererException re = new RendererException("Unable to launch seqvista!");
	    re.initCause(ex);
	    throw re;
	}
    }

}
