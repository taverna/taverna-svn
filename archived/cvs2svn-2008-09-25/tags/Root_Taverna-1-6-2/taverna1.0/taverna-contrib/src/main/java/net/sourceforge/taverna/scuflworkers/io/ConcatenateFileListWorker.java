package net.sourceforge.taverna.scuflworkers.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.AbstractStreamProcessor;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor concatenates a series of text files and saves the results into
 * the output file.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 * 
 * @tavinput filelist An array of fully qualified filenames.
 * @tavinput outputfile The fully qualified name of the output file.
 * @tavinput displayresults Indicates whether or not to make the concatenated
 *           text accessible
 * 
 * @tavoutput results The concatenated text.
 */
public class ConcatenateFileListWorker implements LocalWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		HashMap outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		boolean displayResults = inAdapter.getBoolean("displayresults");
		StringBuffer sb = new StringBuffer(2000);

		String outFile = inAdapter.getString("outputfile");
		if (outFile == null) {
			throw new TaskExecutionException("The 'outputfile' parameter cannot be null");
		}

		String[] fileList = inAdapter.getStringArray("filelist");
		if (fileList == null) {
			throw new TaskExecutionException("The 'filelist' parameter cannot be null");
		}
		String str = null;

		try {
			Writer writer = new FileWriter(outFile);
			for (int i = 0; i < fileList.length; i++) {
				InputStream is = new BufferedInputStream(new FileInputStream(fileList[i]));
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				while ((str = reader.readLine()) != null) {
					writer.write(str);
					writer.write(AbstractStreamProcessor.NEWLINE);

					if (displayResults) {
						sb.append(str);
						sb.append(AbstractStreamProcessor.NEWLINE);
					}
				}

				reader.close();

			}
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			throw new TaskExecutionException(e);
		} catch (IOException e) {
			throw new TaskExecutionException(e);
		}

		if (displayResults) {
			outAdapter.putString("results", sb.toString());
		}

		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "filelist", "outputfile", "displayresults" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "l('text/plain')", "'text/plain'", "'text/plain'" };
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
