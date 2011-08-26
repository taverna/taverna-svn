/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.biomoby.client.taverna.plugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * A local processor to construct a biomoby data packet
 * from either an ID or a string content
 * @author Tom Oinn
 */
public class CreateMobySimple implements LocalWorker {

    static Namespace mobyNS = Namespace.getNamespace("moby",
            "http://www.biomoby.org/moby");
    static protected ArrayList arrayList = new ArrayList();
    public String[] inputNames() {
        return new String[] { "namespace", "id", "stringvalue", "floatvalue",
                "intvalue" };
    }

    public String[] inputTypes() {
        return new String[] { LocalWorker.STRING, LocalWorker.STRING,
                LocalWorker.STRING, LocalWorker.STRING, LocalWorker.STRING };
    }

    public String[] outputNames() {
        return new String[] { "mobySimple" };
    }

    public String[] outputTypes() {
        return new String[] { "l('text/xml')" };
    }

    @SuppressWarnings("unchecked")
	public Map execute(Map inputs) throws TaskExecutionException {
        // Only allow one of stringvalue, floatvalue, intvalue
        // If none are specified that's fine as well, it's just an ID
        // objects
        boolean hasString = inputs.containsKey("stringvalue");
        boolean hasFloat = inputs.containsKey("floatvalue");
        boolean hasInt = inputs.containsKey("intvalue");

        String namespaceValue, idValue;

        try {
            namespaceValue = (String) ((DataThing) inputs.get("namespace"))
                    .getDataObject();
            idValue = (String) ((DataThing) inputs.get("id")).getDataObject();
        } catch (Exception ex) {
            throw new TaskExecutionException(
                    "Both ID and namespace must be specified to build the moby object");
        }


        Element mobySimple = new Element("Simple", mobyNS);
        mobySimple.setAttribute("articleName", "", mobyNS);

        Element dataElement = null;

        if (!(hasString || hasFloat || hasInt)) {
            // Build an object
            dataElement = new Element("Object", mobyNS);
        } else if (hasString && !(hasFloat || hasInt)) {
            // Build a string
            dataElement = new Element("String", mobyNS);
            String stringValue = (String) ((DataThing) inputs
                    .get("stringvalue")).getDataObject();
            dataElement.setText(stringValue);
        } else if (hasFloat && !(hasInt || hasString)) {
            // Build a float
            dataElement = new Element("Float", mobyNS);
            try {
                String floatValue = (String) ((DataThing) inputs
                        .get("floatvalue")).getDataObject();
                Float.parseFloat(floatValue);
                dataElement.setText(floatValue);
            } catch (NumberFormatException nfe) {
                throw new TaskExecutionException(
                        "Float value specified but not parsable as a floating point numeral");
            }
        } else if (hasInt && !(hasFloat || hasString)) {
            // Build an integer
            dataElement = new Element("Integer", mobyNS);
            try {
                String intValue = (String) ((DataThing) inputs.get("intvalue"))
                        .getDataObject();
                Integer.parseInt(intValue);
                dataElement.setText(intValue);
            } catch (NumberFormatException nfe) {
                throw new TaskExecutionException(
                        "Int value specified but not parsable as an integer");
            }
        } else {
            // Invalid combination of inputs, throw exception
            throw new TaskExecutionException(
                    "To construct a moby data object you must specify at most one of string, int or float values.");
        }

        dataElement.setAttribute("namespace", namespaceValue, mobyNS);
        dataElement.setAttribute("id", idValue, mobyNS);
        mobySimple.addContent(dataElement);

        XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
        String mobyOutputString = xo.outputString(new Document(mobySimple));
        Map results = new HashMap();
        arrayList.add(mobyOutputString);
        results.put("mobySimple", new DataThing(arrayList));
        return results;
    }
    
    public Class getProcessorClass(){
        return org.biomoby.client.taverna.plugin.CreateMobySimple.class;
    }

}
