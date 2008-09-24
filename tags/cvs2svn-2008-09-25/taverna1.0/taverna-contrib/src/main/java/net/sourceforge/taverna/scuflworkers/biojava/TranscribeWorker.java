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
 * This processor takes a DNA sequence and transcribes it into an RNA sequence.
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 * 
 * @tavinput dna_seq A raw DNA sequence.
 * @tavoutput rna_seq A raw RNA sequence.
 */
public class TranscribeWorker implements LocalWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		String dnaSeq = inAdapter.getString("dna_seq");

		Map outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		try {
			// make a DNA SymbolList
			SymbolList symL = DNATools.createDNA(dnaSeq);

			// transcribe it to RNA (after BioJava 1.4 this method is
			// deprecated)
			symL = RNATools.transcribe(symL);

			// (after BioJava 1.4 use this method instead)
			// symL = DNATools.toRNA(symL);

			// just to prove it worked
			outAdapter.putString("rna_seq", symL.seqString());
		} catch (IllegalSymbolException ex) {
			new TaskExecutionException(ex);
		} catch (IllegalAlphabetException ex) {
			new TaskExecutionException(ex);
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
		return new String[] { "rna_seq" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "'text/plain'" };
	}

}
