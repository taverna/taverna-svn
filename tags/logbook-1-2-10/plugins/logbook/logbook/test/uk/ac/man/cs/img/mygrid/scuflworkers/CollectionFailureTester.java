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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Simple LocalWorker that accepts an array of Strings as input and always fails.
 * Used in <code>collection-failure.xml</code> workflow.
 * @author dturi
 * 
 * $Id: CollectionFailureTester.java,v 1.1 2007-12-14 12:53:39 stain Exp $
 */
public class CollectionFailureTester implements LocalWorker {


    static Logger logger = Logger.getLogger(CollectionFailureTester.class);

   
    public Map execute(Map inputs) throws TaskExecutionException {
        if (logger.isDebugEnabled()) {
            logger.debug("execute(Map inputs = " + inputs + ") - start");
        }

        Map outputs = new HashMap();
        try {
            new URL("http://wrong").openStream();
            DataThing dataThing = new DataThing("");
            outputs.put("output", dataThing);

            if (logger.isDebugEnabled()) {
                logger.debug("execute(Map) - end");
            }

        } catch (MalformedURLException e) {
            System.out.println("Failed as expected.");
        } catch (IOException e) {
            System.out.println("Failed as expected.");
        }
        return outputs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
        return new String[] { "input", "dummy" };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[] { STRING_ARRAY, STRING };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
     */
    public String[] outputNames() {
        return new String[] { "output" };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
     */
    public String[] outputTypes() {
        return new String[] { STRING };
    }

}
