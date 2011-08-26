package net.sourceforge.taverna.scuflworkers.io;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor exposes the Java environment variables as an XML document.
 * 
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 * 
 * @tavoutput properties An XML document containing the Java environment
 *            variables.
 */
public class EnvVariableWorker implements LocalWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {

		Map outMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outMap);

		Properties sysProps = System.getProperties();
		Set keys = sysProps.keySet();
		Iterator it = keys.iterator();
		String currKey = null;
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\"?>\n");
		sb.append("<property-list>\n");
		while (it.hasNext()) {
			currKey = (String) it.next();
			sb.append("<property ");
			sb.append(" name=\"" + currKey + "\"");
			sb.append(" value=\"" + sysProps.getProperty(currKey) + "\"/>\n");
		}
		sb.append("</property-list>");

		outAdapter.putString("properties", sb.toString());

		return outMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		// TODO Auto-generated method stub
		return new String[0];
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		// TODO Auto-generated method stub
		return new String[0];
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {
		return new String[] { "properties" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "'text/xml'" };
	}

}
