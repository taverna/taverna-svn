/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package net.sourceforge.taverna.scuflworkers.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class transforms an input document
 * Last edited by $Author: phidias $
 * @author mfortner
 * @version $Revision: 1.1 $
 */
public class XSLTWorker implements LocalWorker {
	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		
	    DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
	    
	    Map outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
		
		
		String xslFilename = inAdapter.getString("xslFileURL");	
		String outFilename = inAdapter.getString("outFileURL");
		String inFilename = inAdapter.getString("inFileURL");
		
        try {
            // Create transformer factory
            TransformerFactory factory = TransformerFactory.newInstance();

            // Use the factory to create a template containing the xsl file
            Templates template = factory.newTemplates(new StreamSource(
                new FileInputStream(xslFilename)));

            // Use the template to create a transformer
            Transformer xformer = template.newTransformer();

            // Prepare the input and output files
            Source source = new StreamSource(new FileInputStream(inFilename));
            StringWriter resultStr = new StringWriter();
            Result result = new StreamResult(resultStr);

            // Apply the xsl file to the source file and write the result to the output file
            xformer.transform(source, result);
            outAdapter.putString("outputStr",resultStr.toString());
            
        } catch (FileNotFoundException e) {
        } catch (TransformerConfigurationException e) {
            // An error occurred in the XSL file
        } catch (TransformerException e) {
            // An error occurred while applying the XSL file
            // Get location of error in input file
            SourceLocator locator = e.getLocator();
            int col = locator.getColumnNumber();
            int line = locator.getLineNumber();
            String publicId = locator.getPublicId();
            String systemId = locator.getSystemId();
        }
    		
		
		return outputMap;
	}
	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[]{"xslFileURL","outFileURL","inFileURL"};
	}
	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[]{"'text/plain'","'text/plain'","'text/plain'"};
	}
	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {
		return new String[]{"outputStr"};
	}
	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[]{"'text/xml'"};
	}
}
