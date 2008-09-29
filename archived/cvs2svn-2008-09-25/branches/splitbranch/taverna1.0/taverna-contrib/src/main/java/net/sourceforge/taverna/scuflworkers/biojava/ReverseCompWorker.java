package net.sourceforge.taverna.scuflworkers.biojava;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor takes a raw DNA sequence and returns the reverse complement of
 * the sequence.
 * 
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 * 
 * @tavinput rawSeq A raw sequence (not FASTA).
 * @tavoutput revSeq The reverse complement of the input sequence.
 */
public class ReverseCompWorker implements LocalWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		String seq = inAdapter.getString("rawSeq");

		HashMap outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		try {
			// make a DNA SymbolList
			SymbolList symL = DNATools.createDNA(seq);

			// reverse complement it
			symL = DNATools.reverseComplement(symL);

			// prove that it worked
			outAdapter.putString("revSeq", symL.seqString());
		} catch (IllegalSymbolException ex) {
			throw new TaskExecutionException(ex);
		} catch (IllegalAlphabetException ex) {
			throw new TaskExecutionException(ex);
		}
		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "rawSeq" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {
		return new String[] { "revSeq" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "'text/plain'" };
	}

}
