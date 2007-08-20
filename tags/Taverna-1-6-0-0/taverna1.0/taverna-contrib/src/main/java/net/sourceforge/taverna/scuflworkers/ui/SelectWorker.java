package net.sourceforge.taverna.scuflworkers.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor displays a dialog with a select box of options. It should only
 * be used with interactive workflows that are being run from Taverna.
 * Server-side or command-line workflows should not use this processor.
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 * 
 * @tavinput valueList An array of values to be displayed.
 * @tavinput message A prompt message to be displayed.
 * @tavinput title The title to be displayed in the dialog's titlebar.
 * 
 * @tavoutput answer The selection that the user has made
 */
public class SelectWorker implements LocalWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		HashMap outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
		String[] valueList = inAdapter.getStringArray("valueList");

		String answer = (String) JOptionPane.showInputDialog(null, inAdapter.getString("message"), inAdapter
				.getString("title"), JOptionPane.QUESTION_MESSAGE, null, valueList, valueList[0]

		);
		outAdapter.putString("answer", answer);

		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "valueList", "message", "title" };
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
		return new String[] { "answer" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "'text/plain'" };
	}

}
