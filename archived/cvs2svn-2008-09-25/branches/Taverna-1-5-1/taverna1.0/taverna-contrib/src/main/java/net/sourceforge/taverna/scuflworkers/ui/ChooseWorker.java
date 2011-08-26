package net.sourceforge.taverna.scuflworkers.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor allows the user to select an option from a list of radio
 * buttons. It should only be used with interactive workflows that are being run
 * from Taverna. Server-side or command-line workflows should not use this
 * processor.
 * 
 * 
 * @author Mark
 * @version $Revision: 1.2.2.1 $
 * 
 * @tavinput title The title to be displayed in the dialog box's titlebar
 * @tavinput message The prompt message to be displayed
 * @tavinput selectionValues An array of values to be displayed for the radio
 *           buttons.
 * 
 * @tavoutput answer The user's selection.
 */
public class ChooseWorker implements LocalWorker {

	public ChooseWorker() {

	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		HashMap outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
		String[] valueList = inAdapter.getStringArray("selectionValues");
		ButtonGroup group = new ButtonGroup();
		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BoxLayout(messagePanel,BoxLayout.Y_AXIS));

		messagePanel.add(new JLabel(inAdapter.getString("message")));
		
		
		JRadioButton[] buttonArray = new JRadioButton[valueList.length];
		for (int i = 0; i < valueList.length; i++) {			
			buttonArray[i] = new JRadioButton(valueList[i]);
			if (i==0) buttonArray[i].setSelected(true);
			group.add(buttonArray[i]);
			messagePanel.add(buttonArray[i]);
		}				

		JOptionPane.showOptionDialog(null, messagePanel, inAdapter.getString("title"),
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"OK"}, null

		);
		
		String value="";
		for (JRadioButton button : buttonArray) {
			if (button.isSelected()) {
				value=button.getText();
			}
		}

		outAdapter.putString("answer", value);

		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "title", "message", "selectionValues" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'", "'text/plain'", "l('text/plain')" };
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
