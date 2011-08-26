package net.sourceforge.taverna.scuflworkers.biojava;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.bio.seq.io.agave.AgaveWriter;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor parses a SwissProt file and outputs the results in Agave XML
 * format.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 * 
 * @tavinput fileUrl
 */
public class SwissProtParserWorker implements LocalWorker {

	public SwissProtParserWorker() {

	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		BufferedReader br = null;
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		String fileUrl = inAdapter.getString("fileUrl");

		Map outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
		try {

			// create a buffered reader to read the sequence file specified by
			// args[0]
			br = new BufferedReader(new FileReader(fileUrl));

			// read the EMBL File
			SequenceIterator sequences = SeqIOTools.readSwissprot(br);

			// Prepare the writer
			AgaveWriter writer = new AgaveWriter();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StringBuffer sb = new StringBuffer();
			PrintStream ps = new PrintStream(os);

			// iterate through the sequences
			while (sequences.hasNext()) {

				Sequence seq = sequences.nextSequence();
				writer.writeSequence(seq, ps);
				sb.append(os.toString());
			}
			outAdapter.putString("results", sb.toString());

		} catch (FileNotFoundException ex) {
			throw new TaskExecutionException(ex);
		} catch (BioException ex) {
			throw new TaskExecutionException(ex);
		} catch (NoSuchElementException ex) {
			throw new TaskExecutionException(ex);
		} catch (IOException io) {
			throw new TaskExecutionException(io);
		}

		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "fileUrl" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {
		return new String[] { "results" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "'text/plain'" };
	}

}