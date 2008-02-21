package net.sf.taverna.t2.renderers;

import javax.swing.JComponent;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

import cht.svista.SeqVISTA;

/**
 * Uses the SeqVista renderer to draw an EMBL or SwissProt sequence
 * @author Ian Dunlop
 */
public class SeqVistaRenderer implements Renderer {

	private String seqType = "fasta";
	// 0 = auto, 1 = nucleotide, 2 = protein
	private int np = 0;

	public SeqVistaRenderer() {
	}

	// public boolean isTerminal() {
	// return true;
	// }
	

	public boolean canHandle(String mimeType) {
		if (mimeType.matches(".*chemical/x-swissprot.*")) {
			seqType = "embl";
			np = 2;
			return true;
		} else if (mimeType.matches(".*chemical/x-embl-dl-nucleotide.*")) {
			seqType = "embl";
			np = 1;
			return true;
		} else if (mimeType.matches(".*chemical/x-fasta.*")) {
			seqType = "fasta";
			np = 0;
			return true;
		} else if (mimeType.matches(".*chemical/x-ppd.*")) {
			seqType = "ppd";
			return true;
		} else if (mimeType.matches(".*chemical/seq-na-genbank.*")) {
			seqType = "auto";
			np = 1;
			return true;
		} else if (mimeType.matches(".*chemical/seq-aa-genpept.*")) {
			seqType = "auto";
			np = 2;
			return true;
		}
		return false;
	}

	public JComponent getComponent(EntityIdentifier entityIdentifier,
			DataFacade dataFacade) {
		//no idea what is going on here
		String resolve = null;
		try {
			resolve = (String) dataFacade.resolve(entityIdentifier, String.class);
		} catch (RetrievalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SeqVISTA vista = new SeqVISTA() {
			public java.awt.Dimension getPreferredSize() {
				return new java.awt.Dimension(100, 100);
			}
		};
		try {
			System.out.println(resolve);
			System.out.println(seqType);
			vista.loadFromText(resolve, false,
					seqType, np);
			return vista;
		} catch (Exception ex) {
			//throw some sort of exception
		}
		return null;
	}

	public boolean canHandle(DataFacade facade,
			EntityIdentifier entityIdentifier, String mimeType) {
		return canHandle(mimeType);
	}

	public String getType() {
		return "Seq Vista";
	}

}
