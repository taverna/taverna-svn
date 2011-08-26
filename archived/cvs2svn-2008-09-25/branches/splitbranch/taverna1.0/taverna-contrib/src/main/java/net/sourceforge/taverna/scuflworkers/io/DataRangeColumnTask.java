package net.sourceforge.taverna.scuflworkers.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class allows the user to extract a single column of data from a data
 * array produced by either an ExcelFileReader or by a DelimitedFileReader.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class DataRangeColumnTask extends DataRangeTask implements LocalWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		HashMap outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		String colIndexStr = inAdapter.getString("column");
		if (colIndexStr == null) {
			throw new TaskExecutionException("The 'column' attribute cannot be null");
		}
		int columnIndex = Integer.parseInt(colIndexStr) - 1;

		ArrayList inDataArray = inAdapter.getArrayList("inputArray"); // the
																		// 2D
																		// arraylist
																		// containing
																		// the
																		// values
																		// parsed
																		// from
																		// the
																		// Excel
																		// file.
		if (inDataArray == null || inDataArray.isEmpty()) {
			throw new TaskExecutionException("The 'inputArray' attribute cannot be null or empty");
		}

		ArrayList outDataArray = new ArrayList();

		// copy the array values into a new array.
		ArrayList inRowList = new ArrayList();
		Iterator it = inDataArray.iterator();
		int counter = 0;
		while (it.hasNext()) {
			ArrayList outRowList = new ArrayList();
			inRowList = (ArrayList) inDataArray.get(columnIndex);
			outDataArray.add(inRowList.get(columnIndex));
		}

		outAdapter.putArrayList("outputArray", outDataArray);
		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "inputArray", "column" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "l(l('text/plain'))", "'text/plain'" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {
		return new String[] { "outputArray" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "l('text/plain')" };
	}

}
