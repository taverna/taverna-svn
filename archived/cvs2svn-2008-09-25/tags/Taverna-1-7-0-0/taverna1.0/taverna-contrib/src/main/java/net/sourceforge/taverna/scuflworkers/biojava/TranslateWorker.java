package net.sourceforge.taverna.scuflworkers.biojava;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class translates a DNA sequence into a protein sequence.
 * 
 * @author Mark
 * @version $Revision: 1.3 $
 * 
 * @tavinput dna_seq A raw DNA sequence
 * @tavoutput prot_seq A raw protein sequence
 */
public class TranslateWorker implements LocalWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		String dnaSeq = inAdapter.getString("dna_seq");

		Map outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		try {
			// create a DNA SymbolList
			SymbolList symL = DNATools.createDNA(dnaSeq);

			// transcribe to RNA (after biojava 1.4 this method is deprecated)
			// symL = RNATools.transcribe(symL);

			// transcribe to RNA (after biojava 1.4 use this method instead)
			// symL = DNATools.toRNA(symL);

			// translate to protein
			symL = RNATools.translate(symL);

			// prove that it worked
			outAdapter.putString("prot_seq", symL.seqString());
		} catch (IllegalAlphabetException ex) {
			throw new TaskExecutionException(ex);
		} catch (IllegalSymbolException ex) {
			throw new TaskExecutionException(ex);
		} catch (Throwable th) {
			throw new TaskExecutionException(th);
		}
		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "dna_seq" };
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
		return new String[] { "prot_seq" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "'text/plain'" };
	}
}
