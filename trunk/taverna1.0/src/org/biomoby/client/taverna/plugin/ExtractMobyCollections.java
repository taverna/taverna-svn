/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.biomoby.client.taverna.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.jdom.Namespace;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * A local processor to parse moby collections
 * and return a list of simples each 'packaged' correctly. 
 * @author Tom Oinn
 */
public class ExtractMobyCollections implements LocalWorker {

    static Namespace mobyNS = Namespace.getNamespace("moby",
            "http://www.biomoby.org/moby");

    public String[] inputNames() {
        return new String[] { "Collections" };
    }

    public String[] inputTypes() {
        return new String[] { "'text/xml'" };
    }

    public String[] outputNames() {
        return new String[] { "mobyData" };
    }

    public String[] outputTypes() {
        return new String[] { "l('text/xml')" };
    }

    public Map execute(Map inputs) throws TaskExecutionException {
    	String xml = null;
        try {
            xml = (String) ((DataThing) inputs.get("Collections"))
                    .getDataObject();
        } catch (Exception ex) {
            throw new TaskExecutionException(
                    "In order to parse a collection, one has to be submitted!");
        }
        List list = XMLUtilities.createMobySimpleListFromCollection(xml, null,"");
        
        Map results = new HashMap();
        results.put("mobyData", new DataThing(list));
        return results;
    }
    public Class getProcessorClass(){
        return ExtractMobyCollections.class;
    }

}
