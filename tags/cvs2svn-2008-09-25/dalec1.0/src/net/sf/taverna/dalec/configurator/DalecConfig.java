package net.sf.taverna.dalec.configurator;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA. User: Tony Burdett Date: 04-Sep-2005 Time: 11:31:58 To change this template use File |
 * Settings | File Templates.
 */
public class DalecConfig
{
    private File dazzleFile;
    private Document dazzleDoc;
    private static final String DATASOURCE_TAG_NAME = "datasource";
    private ArrayList dalecs;

    public DalecConfig(File dazzlecfg)
    {
        this.dazzleFile = dazzlecfg;
        dalecs = new ArrayList();
        // Parse existing dazzleDoc document
        try
        {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            this.dazzleDoc = db.parse(dazzleFile);

            // get the root element of dazzleDoc file
            Element dazzle = dazzleDoc.getDocumentElement();
            // now get all datasources
            NodeList datasources = dazzle.getElementsByTagName(DATASOURCE_TAG_NAME);
            for (int i = 0; i < datasources.getLength(); i++)
            {
                // get the attribute for this datasource to check if it is a net.sf.taverna.dalec.configurator.DalecModel source
                Node datasource = datasources.item(i);
                Node jclass = datasource.getAttributes().getNamedItem("jclass");
                if (jclass.getNodeValue().equals("net.sf.taverna.dalec.DalecAnnotationSource"))
                {
                    // datasource is a dalec datasource, so create a new net.sf.taverna.dalec.configurator.DalecModel
                    DalecModel dalec = new DalecModel();

                    // get all the children - these will be string elements describing dalec params
                    NodeList children = datasource.getChildNodes();
                    for (int j = 0; j < children.getLength(); j++)
                    {
                        Node child = children.item(j);
                        if (child instanceof Element)
                        {
                            Element next = (Element) child;
                            // only concerned with the string elements for now
                            if (next.getTagName().equals("string"))
                            {
                                Node nameAttrib = next.getAttributes().getNamedItem("name");
                                if (nameAttrib.getNodeValue().equals(DalecModel.NAME))
                                {
                                    dalec.setName(children.item(j).getAttributes().getNamedItem("value").getNodeValue());
                                }
                                else if (nameAttrib.getNodeValue().equals(DalecModel.DESCRIPTION))
                                {
                                    dalec.setDescription(children.item(j).getAttributes().getNamedItem("value").getNodeValue());
                                }
                                else if (nameAttrib.getNodeValue().equals(DalecModel.MAPMASTER))
                                {
                                    dalec.setMapMaster(children.item(j).getAttributes().getNamedItem("value").getNodeValue());
                                }
                                else if (nameAttrib.getNodeValue().equals(DalecModel.XSCUFLFILE))
                                {
                                    dalec.setXScuflFile(children.item(j).getAttributes().getNamedItem("value").getNodeValue());
                                }
                                else if (nameAttrib.getNodeValue().equals(DalecModel.DBLOCATION))
                                {
                                    dalec.setDBLocation(children.item(j).getAttributes().getNamedItem("value").getNodeValue());
                                }
                            }
                        }
                    }
                    // add this new net.sf.taverna.dalec.configurator.DalecModel to the arraylist
                    dalecs.add(dalec);
                }
            }
        }
        catch (ParserConfigurationException e)
        {
            // if can't create new document

        }
        catch (IOException e)
        {
            // if the dazzleDoc.xml file can't be found

        }
        catch (SAXException e)
        {
            // whilst parsing the dazzleDoc.xml file

        }
    }

    public ArrayList getDalecs()
    {
        return dalecs;
    }

    public void addDalec(DalecModel dalecToAdd)
    {
        // create a new datasource element for our new dalec
        Element dalecElement = dazzleDoc.createElement(DATASOURCE_TAG_NAME);
        // set attributes on the dalec element
        dalecElement.setAttribute("id", (String) dalecToAdd.getAttributes().get(DalecModel.NAME));
        dalecElement.setAttribute("jclass", "net.sf.taverna.dalec.DalecAnnotationSource");

        // get all the dalec attributes, we will need a child node for each one
        Set keys = dalecToAdd.getAttributes().keySet();
        for (Iterator it = keys.iterator(); it.hasNext();)
        {
            String key = (String) it.next();
            // create the child
            Element child = dazzleDoc.createElement("string");
            child.setAttribute("name", key);
            child.setAttribute("value", (String) dalecToAdd.getAttributes().get(key));
            // append the child to the new dalecElement
            dalecElement.appendChild(child);
        }

        // finally, append the new dalecElement to the doc tree
        dazzleDoc.getDocumentElement().appendChild(dalecElement);
    }

    public void clearDalecs()
    {
        ArrayList elementsToRemove = new ArrayList();
        // Get all datasources
        NodeList datasources = dazzleDoc.getElementsByTagName(DATASOURCE_TAG_NAME);
        for (int i = 0; i < datasources.getLength(); i++)
        {
            // get the attribute for this datasource to check if it is a net.sf.taverna.dalec.configurator.DalecModel source
            Node datasource = datasources.item(i);
            Node jclass = datasource.getAttributes().getNamedItem("jclass");

            if (jclass.getNodeValue().equals("net.sf.taverna.dalec.DalecAnnotationSource"))
            {
                // if we have an existing net.sf.taverna.dalec.configurator.DalecModel annotation source, remove it from the document
                elementsToRemove.add(datasource);
            }
        }

        for (Iterator it = elementsToRemove.iterator(); it.hasNext();)
        {
            dazzleDoc.getDocumentElement().removeChild((Node)it.next());
        }
        // now all dalec datasource elements have been removed, so we can add anew
    }

    public void writeXML()
    {
        // we're not going to do anything with our doc except write it out
        try
        {
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(new DOMSource(dazzleDoc), new StreamResult(new FileOutputStream(dazzleFile, false)));
        }
        catch (TransformerException e)
        {
            // if there's a transformer exception

        }
        catch (FileNotFoundException e)
        {
            // if dazzleFile doesn't exist - which it always should!

        }
    }
}
