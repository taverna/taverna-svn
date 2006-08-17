/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * A local processor to construct a biomoby data packet from either an ID or a
 * string content
 * 
 * @author Tom Oinn
 */
public class CreateMobyCollection implements LocalWorker {

    static Namespace mobyNS = Namespace.getNamespace("moby",
            "http://www.biomoby.org/moby");

    public String[] inputNames() {
        return new String[] { "collectionName", "mobySimple1", "mobySimple2",
                "mobySimple3", "mobySimple4", "mobySimple5", "mobySimple6",
                "mobySimple7", "mobySimple8", "mobySimple9", "mobySimple10",
                "mobySimple11", "mobySimple12", "mobySimple13", "mobySimple14",
                "mobySimple15", "mobySimple16", "mobySimple17", "mobySimple18",
                "mobySimple19", "mobySimple20", "mobySimple21", "mobySimple22",
                "mobySimple23", "mobySimple24", "mobySimple25", "mobySimple26",
                "mobySimple27", "mobySimple28", "mobySimple29", "mobySimple30",
                "mobySimple31", "mobySimple32", "mobySimple33", "mobySimple34",
                "mobySimple35" };
    }

    public String[] inputTypes() {
        return new String[] { LocalWorker.STRING, "'text/xml'", "'text/xml'",
                "'text/xml'", "'text/xml'", "'text/xml'", "'text/xml'",
                "'text/xml'", "'text/xml'", "'text/xml'", "'text/xml'",
                "'text/xml'", "'text/xml'", "'text/xml'", "'text/xml'",
                "'text/xml'", "'text/xml'", "'text/xml'", "'text/xml'",
                "'text/xml'", "'text/xml'", "'text/xml'", "'text/xml'",
                "'text/xml'", "'text/xml'", "'text/xml'", "'text/xml'",
                "'text/xml'", "'text/xml'", "'text/xml'", "'text/xml'",
                "'text/xml'", "'text/xml'", "'text/xml'", "'text/xml'",
                "'text/xml'" };
    }

    public String[] outputNames() {
        return new String[] { "mobyCollection" };
    }

    public String[] outputTypes() {
        return new String[] { "'text/xml'" };
    }

    @SuppressWarnings("unchecked")
	public Map execute(Map inputs) throws TaskExecutionException {

        boolean hasSimple = hasSimple(inputs);
        String name = "";

        // using jdom element
        // create the MOBY element and init any attributes
        Element MOBY = new Element("MOBY", mobyNS);
        Element content = new Element("mobyContent", mobyNS);
        Element mobyData = new Element("mobyData", mobyNS);
        mobyData.setAttribute("queryID", "a1", mobyNS);
        Element mobyCollection = new Element("Collection", mobyNS);
        try {
            name = (String) ((DataThing) inputs.get("collectionName"))
                    .getDataObject();
        } catch (Exception ex) {
            // wasnt specified, so name is empty
        }
        mobyCollection.setAttribute("articleName", name, mobyNS);

        // create nest
        mobyData.addContent(mobyCollection);
        content.addContent(mobyData);
        MOBY.addContent(content);

        if (hasSimple)
            try {
                // create an iterator over inputs that basically appends the
                // simple into a collection node
                // careful to remove the collectionName
                for (Iterator iter = inputs.keySet().iterator(); iter.hasNext();) {
                    String key = (String) iter.next();
                    if (!key.equalsIgnoreCase("collectionName")) {
                        // start adding the simple to the collection
                        DataThing inputThing = (DataThing) inputs.get(key);
                        String simple = (String) inputThing.getDataObject();
                        Document doc;
                        try {
                            SAXBuilder builder = new SAXBuilder();
                            doc = builder.build(new ByteArrayInputStream(simple.getBytes()));
                        } catch (JDOMException e) {
                            e.printStackTrace();
                            throw new TaskExecutionException(
                                    "Error parsing simple " + key + ". Is the simple valid?");
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            throw new TaskExecutionException(
                                    "Could not build the XML. Make sure that the simples are valid XML.");
                        }
                        Element tempElement = (doc.getRootElement().getChild("mobyContent", mobyNS)).getChild("mobyData", mobyNS).getChild("Simple", mobyNS);
                        tempElement.setAttribute("articleName", "", mobyNS);
                        mobyCollection.addContent(tempElement.detach());
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                throw new TaskExecutionException(
                        "At least one mobySimple is needed to build the moby collection");
            }
        else
            throw new TaskExecutionException(
                    "At least one mobySimple is needed to build the moby collection");

        XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
        String mobyOutputString = xo.outputString(new Document(MOBY));

        Map results = new HashMap();
        results.put("mobyCollection", new DataThing(mobyOutputString));
        //CreateMobySimple.arrayList = new ArrayList();
        return results;

    }

    private boolean hasSimple(Map inputs) {
        Iterator it = inputs.keySet().iterator();
        while (it.hasNext()) {
            if (((String)it.next()).indexOf("Simple") > 0 )
                return true;
        }
        return false;
    }

}
