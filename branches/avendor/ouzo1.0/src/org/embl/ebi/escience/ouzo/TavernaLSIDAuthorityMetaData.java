/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.ouzo;

import com.ibm.lsid.ExpiringResponse;
import com.ibm.lsid.LSID;
import com.ibm.lsid.server.LSIDMetaDataService;
import com.ibm.lsid.server.LSIDServerException;
import com.ibm.lsid.server.LSIDServiceConfig;

// IO Imports
import java.io.ByteArrayInputStream;

import org.embl.ebi.escience.ouzo.TavernaLSIDDataLookup;
import java.lang.String;
import java.lang.StringBuffer;



/**
 * Provides metadata lookup services to be used by the
 * IBM metadata authority servlet.
 * @author Tom Oinn
 */
public class TavernaLSIDAuthorityMetaData implements LSIDMetaDataService {
    
    private TavernaLSIDDataLookup lookup = null;
    
    /**
     * Create a handle to a TavernaLSIDDataLookup object
     */
    public void initMetaDataService(LSIDServiceConfig cf) throws LSIDServerException {
	lookup = new TavernaLSIDDataLookup();
    }
    
    private static final String RDF_NS=
	"http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String DC_NS=
	"http://purl.org/dc/elements/1.1/";
    private static final String I3CP_NS=
	"urn:lsid:i3c.org:predicates:";
    private static final String I3C_CONTENT=
	"urn:lsid:i3c.org:types:content";
    private static final String I3C_SPROT=
	"urn:lsid:i3c.org:formats:sprot";
    private static final String I3C_FASTA=
	"urn:lsid:i3c.org:formats:fasta";
    
    private void appendTripleResource(StringBuffer src, String subj, String pred, String obj) {
	src.append("<rdf:Description rdf:about=\"");
	src.append(subj);
	src.append("<");
	src.append(pred);
	src.append(" rdf:resource=\")");
	src.append(obj);
	src.append("\"/></rdf:Description>");
    }
    
    /**
     * Return a block of RDF for the supplied ID.
     * This method will extract the appropriate
     * RDF statements describing workflow definitions
     * and intermediate values, i.e. dublin core
     * in the first case for authors and such and
     * our custom metadata in the last expressing
     * the relationships to other LSID named values
     * in the workflow instance.<p>
     * For now it just returns a fixed chunk of RDF
     * which isn't particularly interesting, I just
     * want to see the authority working.
     */
    public ExpiringResponse getMetaData(LSID lsid) throws LSIDServerException {
	int lsType;
	try {
	    lsType = lookup.lsidType(lsid);
	}
	catch (LSIDServerException ex) {
	    ex.printStackTrace();
	    lsType = TavernaLSIDDataLookup.UNKNOWN;
	}
	if (lsType == TavernaLSIDDataLookup.UNKNOWN) {
	    throw new LSIDServerException(201, "Unknown LSID");
	}
	
	StringBuffer result= new StringBuffer();
	result.append("<?xml version=\"1.0\"?><rdf:RDF");
	result.append(" xmlns:rdf=\"");
	result.append(RDF_NS);
	result.append("\" xmlns:dc=\"");
	result.append(DC_NS);
	result.append("\" xmlns:i3cp=\"");
	result.append(I3CP_NS);
	result.append(">");
	result.append("</rdf:RDF>");
	
	return new ExpiringResponse(new ByteArrayInputStream(result.toString().getBytes()),null);
    }
}
