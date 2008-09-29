package net.sourceforge.taverna.scuflworkers.biojava;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.biojava.StringXMLEmitter;

import org.biojava.bio.program.sax.FastaSearchSAXParser;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor reads a FASTA file and returns the results as XML.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 * 
 * @tavinput fileurl The complete path to the FASTA file.
 * @tavoutput results An XML formatted sequence.
 */
public class FastaWorker implements LocalWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);

		Map outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		String fileurl = inAdapter.getString("fileurl");

		XMLReader parser = (XMLReader) new FastaSearchSAXParser();
		StringWriter writer = new StringWriter();
		StringXMLEmitter emitter = new StringXMLEmitter(writer);
		ContentHandler handler = (ContentHandler) emitter;

		try {
			FileInputStream inputFileStream = new FileInputStream(new File(fileurl));
			BufferedReader contents = new BufferedReader(new InputStreamReader(inputFileStream));

			parser.setContentHandler(handler);
			parser.parse(new InputSource(contents));

			outAdapter.putString("results", writer.getBuffer().toString());

		} catch (FileNotFoundException fnfe) {
			throw new TaskExecutionException(fnfe);

		} catch (IOException ioe) {
			throw new TaskExecutionException(ioe);
		} catch (SAXException se) {
			throw new TaskExecutionException(se);
		}
		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "fileurl" };
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
		return new String[] { "'text/xml'" };
	}

}