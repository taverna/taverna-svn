package net.sourceforge.taverna.scuflworkers.io;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class extracts a range of values from a two dimension data array
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class DataRangeTask implements LocalWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		HashMap outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		ArrayList inDataArray = inAdapter.getArrayList("inputArray");
		ArrayList outDataArray = new ArrayList();

		// process the coordinates
		String startingCoords = inAdapter.getString("startingPoint");
		String endingCoords = inAdapter.getString("endPoint");
		Dimension startingPt = convertCoords(startingCoords);
		Dimension endPt = convertCoords(endingCoords);

		// copy the array values into a new array.
		int iCount = 0, jCount = 0;
		ArrayList inRowList = new ArrayList();
		for (int i = startingPt.height; i <= endPt.height; i++) {
			ArrayList outRowList = new ArrayList();
			inRowList = (ArrayList) inDataArray.get(i);
			for (int j = startingPt.width; j <= endPt.width; j++) {
				outRowList.add(inRowList.get(j));
			}
			outDataArray.add(outRowList);
		}

		outAdapter.putArrayList("outputArray", outDataArray);
		return outputMap;
	}

	/**
	 * This method converts comma-delimited
	 * 
	 * @param coords
	 * @return
	 */
	public Dimension convertCoords(String coordStr) {
		Dimension coords = new Dimension();
		String[] coordArray = coordStr.split(",");
		coords.width = Integer.parseInt(coordArray[0]);
		coords.height = Integer.parseInt(coordArray[1]);

		return coords;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "inputArray", "startingPoint", "endPoint" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'", "'text/plain'", "'text/plain'" };
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
		return new String[] { "l(l('text/plain'))" };
	}

}
