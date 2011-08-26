package net.sourceforge.taverna.scuflworkers.biojava;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.biojava.StringXMLEmitter;

import org.biojava.bio.program.sax.BlastLikeSAXParser;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor parses BLAST results and returns an XML document containing
 * the results.
 * 
 * @author Mark
 * @version $Revision: 1.3 $
 * 
 * @tavinput fileUrl
 * @tavinput strict
 * 
 * @tavoutput blastresults XML-formatted BLAST results.
 * 
 */
public class BlastParserWorker implements LocalWorker {
	private XMLReader oParser;

	public BlastParserWorker() {

	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		String fileUrl = inAdapter.getString("fileUrl");

		String strictStr = inAdapter.getString("strict");
		boolean tStrict = (strictStr != null) ? Boolean.getBoolean(strictStr) : false;

		HashMap outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		try {

			/*
			 * Create a SAX Parser that takes the native output from blast-like
			 * bioinformatics software.
			 */
			oParser = (XMLReader) new BlastLikeSAXParser();

			if (tStrict) {
				((BlastLikeSAXParser) oParser).setModeStrict();
			} else {
				((BlastLikeSAXParser) oParser).setModeLazy();
			}

			/*
			 * Dynamically change configuration of the parser in regard of
			 * Namespace support. Here, the xml.org/sax/features/namespaces
			 * feature is simply "reset" to its default value for SAX2. The
			 * xml.org/sax/features/namespaces-prefixes feature is also set to
			 * true. This is to ensure that xmlns attributes are reported by the
			 * parser. These are required because we want to configure the
			 * XMLEmitter to output qualified names (see below).
			 */

			oParser.setFeature("http://xml.org/sax/features/namespaces", true);
			oParser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

			/*
			 * Create an XML ContentHandler. This implementation of the
			 * DocumentHandler interface simple outputs nicely formatted XML.
			 * Passing a true value to the SimpleXMLEmitter constructor
			 * instructs the ContentHandler to take QNames from the SAXParser,
			 * rather than LocalNames.
			 */
			StringWriter writer = new StringWriter();
			StringXMLEmitter emitter = new StringXMLEmitter(writer);
			ContentHandler oHandler = (ContentHandler) emitter;

			/*
			 * Give the parser a reference to the ContentHandler so that it can
			 * send SAX2 mesagges.
			 */
			oParser.setContentHandler(oHandler);
			/*
			 * Now make the Parser parse the output from the blast-like software
			 * and emit XML as specificed by the DocumentHandler.
			 */

			FileInputStream oInputFileStream;
			BufferedReader oContents;
			String oLine = null;

			oInputFileStream = new FileInputStream(fileUrl);
			// create input stream
			oContents = new BufferedReader(new InputStreamReader(oInputFileStream));

			oParser.parse(new InputSource(oContents));

			outAdapter.putString("blastresults", writer.getBuffer().toString());

		} catch (java.io.FileNotFoundException x) {
			throw new TaskExecutionException(x);

		} catch (Exception ex) {
			throw new TaskExecutionException(ex);
		}

		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "fileUrl", "strict" };
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
		return new String[] { "blastresults" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "'text/xml'" };
	}

}
