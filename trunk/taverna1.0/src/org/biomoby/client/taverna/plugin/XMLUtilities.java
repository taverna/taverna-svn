/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.biomoby.shared.MobyException;
import org.biomoby.shared.mobyxml.jdom.MobyObjectClassNSImpl;
import org.biomoby.shared.mobyxml.jdom.jDomUtilities;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XMLUtilities {

    /**
     * 
     * TODO - place brief description here. <p><b>PRE:</b> <p><b>POST:</b>
     * 
     * @param xml
     * @return
     * @throws MobyException
     */
    public static Document getDOMDocument(String xml) {
        SAXBuilder builder = new SAXBuilder();
        // Create the document
        Document doc = null;
        try {
            doc = builder.build(new StringReader(xml));
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    /**
     * 
     * TODO - place brief description here. <p><b>PRE:</b> <p><b>POST:</b>
     * 
     * @param elementToWrap
     * @return
     */
    public static Element createMobyDataElementWrapper(Element elementToWrap) {
        Element element = null;
        Document doc = new Document();
        doc.setBaseURI(MobyObjectClassNSImpl.MOBYNS.getURI());
        // got a document, now need to create mobyData
        element = new Element("MOBY", MobyObjectClassNSImpl.MOBYNS);
        doc.setRootElement(element);
        Element mobyContent = new Element("mobyContent",
                MobyObjectClassNSImpl.MOBYNS);
        Element mobyData = new Element("mobyData", MobyObjectClassNSImpl.MOBYNS);
        mobyData.setAttribute("queryID", "a1", MobyObjectClassNSImpl.MOBYNS);
        mobyContent.addContent(mobyData);
        mobyData.addContent(elementToWrap.detach());
        element.addContent(mobyContent);
        return element;
    }

    /**
     * 
     * Takes in a jDom Element and performs a search for tag with a given
     * objectType, articleName and possible namespaces <p><b>PRE: xml is valid
     * XML and was returned by a Moby service</b> <p><b>POST: The sought after
     * element is returned.</b>
     * 
     * @param xml -
     *            The element to conduct the search on.
     * @param objectType -
     *            The tag name of the element to search for.
     * @param articleName -
     *            The article name of the object
     * @param namespaces -
     *            The list of possible namespaces or null for any.
     * @return String representation of the Element in question with tag name
     *         objectType, article name articleName, and list of possible
     *         namespaces namespaces.
     */
    public static String getMobyElement(Element xml, String objectType,
            String articleName, String[] namespaces, String mobyEndpoint) {
        MobyObjectClassNSImpl moc = new MobyObjectClassNSImpl(mobyEndpoint);
        Element e = jDomUtilities.getElement(objectType, xml);
        // TODO check namespaces, etc.
        if (e != null) {
        	return moc.toString(moc.toSimple(e, ""));
        }
        return "<?xml version=\'1.0\' encoding=\'UTF-8\'?><moby:MOBY xmlns:moby=\'http://www.biomoby.org/moby\' xmlns=\'http://www.biomoby.org/moby\'><moby:mobyContent moby:authority=\'\'><moby:mobyData moby:queryID=\'a1\'/></moby:mobyContent></moby:MOBY>";
    }

    public static Document createDomDocument() {
        Document d = new Document();
        d.setBaseURI(MobyObjectClassNSImpl.MOBYNS.getURI());
        return d;
    }

    /**
     * TODO - place brief description here. <p><b>PRE:</b> <p><b>POST:</b>
     * 
     * @param documentElement
     * @param objectType
     * @param string
     * @param object
     * @return
     */
    public static String getMobyCollection(Element documentElement,
            String objectType, String string, Object object, String mobyEndpoint) {
        MobyObjectClassNSImpl mo = new MobyObjectClassNSImpl(mobyEndpoint);
        Document doc = XMLUtilities.createDomDocument();
        Element root = new Element("MOBY", MobyObjectClassNSImpl.MOBYNS);
        Element content = new Element("mobyContent",
                MobyObjectClassNSImpl.MOBYNS);
        Element mobyData = new Element("mobyData", MobyObjectClassNSImpl.MOBYNS);
        Element mobyCollection = new Element("Collection",
                MobyObjectClassNSImpl.MOBYNS);
        mobyData.setAttribute("queryID", "a1", MobyObjectClassNSImpl.MOBYNS);
        root.addContent(content);
        doc.addContent(root);
        content.addContent(mobyData);
        mobyData.addContent(mobyCollection);
        Element e = documentElement.getChild("mobyContent", MobyObjectClassNSImpl.MOBYNS);
        if (e != null) {
                e = e.getChild("mobyData", MobyObjectClassNSImpl.MOBYNS);
                if (e != null){
                    e = e.getChild(
                        "Collection", MobyObjectClassNSImpl.MOBYNS);
                }
        }
        List list = e.getChildren("Simple",
                MobyObjectClassNSImpl.MOBYNS);
        for (Iterator x = list.iterator(); x.hasNext();) {
            Element child = (Element)x.next();
            List objects = child.getChildren(objectType, MobyObjectClassNSImpl.MOBYNS);
            // iterate through the list adding them to the mobyCollection TODO
            Iterator iter = objects.iterator();
            while (iter.hasNext()) {
                Element simple = new Element("Simple", MobyObjectClassNSImpl.MOBYNS);
                Element _c = (Element)iter.next();
                iter.remove();
                simple.addContent(_c.detach());
                mobyCollection.addContent(simple);
            }
        }
        return mo.toString(root);
    }
}
