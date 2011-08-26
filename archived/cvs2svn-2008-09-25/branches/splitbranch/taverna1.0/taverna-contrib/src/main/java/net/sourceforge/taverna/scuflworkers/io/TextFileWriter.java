package net.sourceforge.taverna.scuflworkers.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor writes the "filecontents" out to the the url specified in the
 * "outputFile" parameter. Note that the outputMap is always empty.
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 * 
 * @tavinput outputFile A fully qualified path to the text file you want to
 *           create.
 * @tavinput filecontents The contents of the file that you want written out.
 * @tavoutput outputFile The contents of the file (the same as the filecontents
 *            parameter).
 */
public class TextFileWriter implements LocalWorker {

	public TextFileWriter() {

	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inputAdapter = new DataThingAdapter(inputMap);
		Map outputMap = new HashMap();
		DataThingAdapter outputAdapter = new DataThingAdapter(outputMap);

		String fileName = inputAdapter.getString("outputFile");
		if (fileName == null) {
			throw new TaskExecutionException("The outputFile was null");
		}
		String filecontents = inputAdapter.getString("filecontents");
		if (filecontents == null) {
			throw new TaskExecutionException("The file contents were null");
		}

		try {

			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write(filecontents);
			out.close();
			outputAdapter.putString("outputFile", filecontents);

		} catch (IOException io) {
			throw new TaskExecutionException(io);
		}

		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "outputFile", "filecontents" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'", "'text/plain'" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {
		return new String[] { "outputFile" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "'text/plain'" };
	}

}