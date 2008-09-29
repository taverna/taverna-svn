/*   
 * Copyright (C) 2004 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 */

package uk.ac.man.cs.img.mygrid.scuflworkers;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Converts a BLAST XML report to RDF using the XSLT at {@link #BLAST_XSL}. The
 * URL of the report is expected as input.
 * 
 * @author dturi
 * 
 * $Id: Blast2RDF.java,v 1.1 2007-12-14 12:53:39 stain Exp $
 */
public class Blast2RDF implements LocalWorker {

    private static final String BLAST_REPORT = "BLASTreport";

    public static final String BLAST_XSL = "http://www.cs.man.ac.uk/~dturi/mygrid/provenance/blast.xsl";

    public static final String DTD_URL = "http://www.cs.man.ac.uk/~dturi/mygrid/provenance/";

    private static final String LSID_PARAM = "lsid";

    static Logger logger = Logger.getLogger(Blast2RDF.class);

    /**
     * Applies {@link #BLAST_XSL} to input report and returns result of
     * transformation.
     * 
     * @param inputs
     *            a Map consisting of a single input: BLASTreport -> url of
     *            report
     * @return a Map consisting of a single output: BLASTRDF -> rdf+xml of
     *         transformed report
     * 
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputs) throws TaskExecutionException {
        if (logger.isDebugEnabled()) {
            logger.debug("execute(Map inputs = " + inputs + ") - start");
        }

        String fullInput = (String) ((DataThing) (inputs.get(BLAST_REPORT)))
                .getDataObject();
        Map outputs = new HashMap();
        try {
            InputStream blastReport = new URL(fullInput).openStream();
            TransformerFactory factory = TransformerFactory.newInstance();
            Templates template = factory.newTemplates(new StreamSource(
                    BLAST_XSL));
            logger.debug("built template");
            Transformer xformer = template.newTransformer();
            xformer.setParameter(LSID_PARAM, "urn:lsid:test:1");

            Source source = new StreamSource(blastReport);
            source.setSystemId(DTD_URL);
            StringWriter resultStr = new StringWriter();
            Result result = new StreamResult(resultStr);

            xformer.transform(source, result);
            logger.debug("transformed");
            DataThing dataThing = new DataThing(resultStr.toString());
            outputs.put("BLASTRDF", dataThing);
            logger.debug("Blast2RDF output syntactic type = "
                    + dataThing.getSyntacticType());
        } catch (TransformerConfigurationException e) {
            logger.debug(e.getMessage());
            logger.debug(e.getLocationAsString());
            e.printStackTrace(System.out);
        } catch (TransformerException e) {
            logger.debug(e.getMessage());
            e.printStackTrace(System.out);
        } catch (MalformedURLException e) {
            logger.error("execute(Map inputs = " + inputs + ")", e);
            e.printStackTrace(System.out);
        } catch (IOException e) {
            logger.error("execute(Map inputs = " + inputs + ")", e);
            e.printStackTrace(System.out);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("execute(Map) - end");
        }
        return outputs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
        return new String[] { BLAST_REPORT };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[] { STRING };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
     */
    public String[] outputNames() {
        return new String[] { "BLASTRDF" };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
     */
    public String[] outputTypes() {
        return new String[] { "'application/rdf+xml'" };
    }

}
