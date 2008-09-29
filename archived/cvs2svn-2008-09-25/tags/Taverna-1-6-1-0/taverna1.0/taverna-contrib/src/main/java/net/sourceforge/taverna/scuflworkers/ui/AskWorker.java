package net.sourceforge.taverna.scuflworkers.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor creates a Dialog with a text box for user input. This
 * processor should only be used in workflows for client-side applications.
 * Server side applications should not require this level of interaction.
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 * 
 * @tavinput message The prompt message to be displayed.
 * @tavinput title The title for the title-bar of the dialog window.
 * 
 * @tavinput answer The response from the user.
 */
public class AskWorker implements LocalWorker {

	public AskWorker() {

	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		HashMap outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		String answer = JOptionPane.showInputDialog(null, inAdapter.getString("message"), inAdapter.getString("title"),
				JOptionPane.QUESTION_MESSAGE

		);
		outAdapter.putString("answer", answer);
		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "title", "message" };
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
		return new String[] { "answer" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "'text/plain'" };
	}

}
