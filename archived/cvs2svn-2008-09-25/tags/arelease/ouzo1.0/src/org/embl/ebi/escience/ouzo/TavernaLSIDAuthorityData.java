/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.ouzo;

import com.ibm.lsid.LSID;
import com.ibm.lsid.server.LSIDDataService;
import com.ibm.lsid.server.LSIDServerException;
import com.ibm.lsid.server.LSIDServiceConfig;

// IO Imports
import java.io.InputStream;

import org.embl.ebi.escience.ouzo.TavernaLSIDDataLookup;
/**
 * An authority layer over the TavernaLSIDDataLookup
 * class that interfaces between it and the IBM default
 * authority servlet.
 * @author Tom Oinn
 */
public class TavernaLSIDAuthorityData implements LSIDDataService {
    
    // A handle to the actual data lookup class
    private TavernaLSIDDataLookup lookup = null;
    
    /**
     * Get the data for the given LSID
     * @param lsid the value to obtain data for
     * @return an InputStream containing the data
     * @exception LSIDServerException if an error
     * occurs connecting to the underlying database
     * or if the supplied LSID doesn't exist.
     */
    public InputStream getData(LSID lsid) throws LSIDServerException {
	if (lookup == null)
	    throw new LSIDServerException(500, "Cannot query database");
	return lookup.lsidData(lsid);
    }
    
    /**
     * Create a new TavernaLSIDDataLookup object to
     * be used by this data service.
     */
    public void initDataService(LSIDServiceConfig cf) throws LSIDServerException {
	lookup = new TavernaLSIDDataLookup();
    }
    
}
