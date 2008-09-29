package net.sourceforge.taverna.scuflworkers.ncbi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public abstract class AbstractXmlWorkerTest extends TestCase {
    
    public abstract void testExecute() throws Exception;
    
    /**
     * This method verifies whether or not certain elements exist within the XML.
     * @param root
     * @param xpath
     * @return
     * @throws Exception
     */
    public void testXPath(Element root, String xpath) throws Exception{

    }
    
    /**
     * This method is meant to be called from within the testExecute method.
     * It verifies whether or not the XML that is returned by the worker is parseable
     * and valid.
     * @param xml
     * @return
     * @throws Exception
     */
    public void testXmlParsing(String xml) throws Exception{
        try {
            Element root = parseXml(xml);
            
        }catch (Exception ex){
            ex.printStackTrace();
            fail("Not able to successfully parse xml" + xml);
        }
        
    }
    
    /**
     * This method is meant to be called from within the testExecute method.
     * It verifies whether or not the XML that is returned by the worker contains
     * any values.  By default all workers return this "<?xml version="1.0"?><searchResults></searchResults>"
     * If the searchResults element has children, then the results are not considered empty.
     * @param root
     * @throws Exception
     */
    public void testXmlNotEmpty(Element root) throws Exception{
        assertTrue("The root has no child nodes",root.hasChildNodes());
        NodeList nl = root.getChildNodes();
        int numNodes = nl.getLength();
        Node currNode = null;
        boolean hasElementNodes = false;
        for (int i=0; i < numNodes; i++){
            currNode = nl.item(i);
            if (currNode.getNodeType() == Node.ELEMENT_NODE){
                hasElementNodes = true;
                break;
            }
        }
        assertTrue("There were no element nodes", hasElementNodes);
    }
    
    /**
     * This method is used to parse the XML.
     * @param xml
     * @return
     * @throws Exception
     */
    public Element parseXml(String xml) throws Exception{
        Element root = null;
        
        // Create a builder factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);

        // Create the builder and parse the file
        Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        root = doc.getDocumentElement();
       
        return root;
    }
    
    /**
     * This method allows you to dump the xml contents out for debugging and verification purposes.
     * By default, all files are dumped to your user home directory.
     * @param filename
     * @param content
     */
    public void writeFile(String filename, String content)throws IOException{
    	
    	/**
        String home = System.getProperty("user.home");
        FileWriter writer = new FileWriter(new File(home,filename));
        writer.write(content);
        writer.flush();
        writer.close();
        **/        
    }

}
