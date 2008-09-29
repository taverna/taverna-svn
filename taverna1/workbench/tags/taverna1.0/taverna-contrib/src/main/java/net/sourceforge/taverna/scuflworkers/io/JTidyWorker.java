package net.sourceforge.taverna.scuflworkers.io;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.w3c.tidy.Tidy;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor tidies up bad HTML and converts it into valid XHTML.
 * 
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 * 
 * @tavinput inputHtml
 * @tavoutput results
 */
public class JTidyWorker implements LocalWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		String html = inAdapter.getString("inputHtml");
		if (html == null || html.equals("")) {
			throw new TaskExecutionException("The 'inputHTML' parameter cannot be null");
		}

		Tidy tidy = new Tidy(); // obtain a new Tidy instance
		tidy.setXHTML(true); // set desired config options using tidy setters
		// tidy.setSmartIndent(true);
		tidy.setEncloseBlockText(true);
		tidy.setEncloseText(true);
		tidy.setIndentContent(false);
		tidy.setXmlOut(true);

		byte[] byteArray = html.getBytes();
		InputStream is = new StringBufferInputStream(html);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		tidy.parse(is, os);

		Map outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
		outAdapter.putString("results", os.toString());
		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "inputHtml" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'" };
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
