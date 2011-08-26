package net.sourceforge.taverna.scuflworkers.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.AbstractStreamProcessor;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor reads the text from a file, and replaces all instances of the
 * oldText with instances of the new text. Use either the inputFile or the
 * inputText parameter. Note that the oldText array and the newText array must
 * be the same length.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 * 
 * @tavinput inputFile A complete path to a text file to be processed.
 * @tavinput inputText The text of a file to be processed.
 * @tavinput oldText An array of text values that you want to replace.
 * @tavinput newText An array of new text values.
 * 
 * @tavoutput results The text after it has been processed.
 */
public class RegexFileWorker implements LocalWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		String inputFile = inAdapter.getString("inputFile");
		String inputText = inAdapter.getString("inputText");
		String[] oldText = inAdapter.getStringArray("oldText");
		String[] newText = inAdapter.getStringArray("newText");

		if (inputText == null && inputFile == null) {
			throw new TaskExecutionException("You must specify either the 'inputFile' or the 'inputText' parameter");
		}

		if (oldText == null) {
			throw new TaskExecutionException("You must specify the 'oldText' parameter");
		}

		if (newText == null) {
			throw new TaskExecutionException("You must specify the 'newText' parameter");
		}

		if (oldText.length != newText.length) {
			throw new TaskExecutionException("The number of 'oldText' values and 'newText' values must be the same");
		}

		if (inputFile != null) {
			StringBuffer sb = new StringBuffer(2000);
			try {
				BufferedReader in = new BufferedReader(new FileReader("infilename"));
				String str;
				while ((str = in.readLine()) != null) {
					sb.append(str);
					sb.append(AbstractStreamProcessor.NEWLINE);
				}
				in.close();
				inputText = sb.toString();
			} catch (IOException e) {
				throw new TaskExecutionException(e);
			}
		}

		for (int i = 0; i < oldText.length; i++) {
			inputText = inputText.replaceAll(oldText[i], newText[i]);
		}

		Map outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		outAdapter.putString("results", inputText);
		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "inputFile", "inputText", "oldText", "newText" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'", "'text/plain'", "l('text/plain')", "l('text/plain')" };
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
