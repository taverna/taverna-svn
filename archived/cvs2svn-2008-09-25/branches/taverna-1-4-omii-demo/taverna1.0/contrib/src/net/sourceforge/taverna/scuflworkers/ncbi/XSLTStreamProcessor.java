package net.sourceforge.taverna.scuflworkers.ncbi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sourceforge.taverna.io.StreamProcessor;

import org.embl.ebi.escience.baclava.DataThing;

/**
 * This class is used to apply an XSLT to a stream.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class XSLTStreamProcessor implements StreamProcessor {

    private String outputFile;
    private String xslt;
    private HashMap outputMap = new HashMap();
    
    /**
     * 
     * @param outputMap
     * @param outputFile
     * @param xslt
     */
    public XSLTStreamProcessor(HashMap outputMap, String outputFile, String xslt){
        this.outputMap = outputMap;
        this.outputFile = outputFile;
        this.xslt = xslt;
    }

    /**
     * @see net.sourceforge.taverna.io.StreamProcessor#processStream(java.io.InputStream)
     */
    public Map processStream(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream),2000);
        String str;
        String lineEnding = System.getProperty("line.separator");
        //StringWriter sw = new StringWriter();
        //BufferedWriter out = new BufferedWriter(sw, this.bufferSize);
        FileWriter fw = null;
        if (this.outputFile != null){
            fw = new FileWriter(this.outputFile);
        }

        boolean startTagFound = false;
        boolean endTagFound = false;

        // Create transformer factory
        TransformerFactory factory = TransformerFactory.newInstance();

        // Use the factory to create a template containing the xsl file
        Templates template;
        StringWriter sw = new StringWriter();
        
        System.out.println("Reading xml & starting transform");

        try {
            template = factory.newTemplates(new StreamSource(
                    new FileInputStream(this.xslt)));

            // Use the template to create a transformer
            Transformer xformer = template.newTransformer();
            
            StringReader reader = new StringReader("");
            
            StreamSource source = new StreamSource(reader);
            
            Result result = new StreamResult(sw);

            sw.write("<?xml version=\"1.0\"?>");
            sw.write("<searchResults>");
            int count=0;

            while ((str = in.readLine()) != null) {

                // Apply the xsl file to the source file and write the result to
                // the
                // output file
                reader.read(str.toCharArray());
                
                xformer.transform(source, result);
                
                count++;
            }
            sw.write("</searchResults>");
            sw.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            // An error occurred while applying the XSL file
            // Get location of error in input file
            SourceLocator locator = e.getLocator();
            int col = locator.getColumnNumber();
            int line = locator.getLineNumber();
            String publicId = locator.getPublicId();
            String systemId = locator.getSystemId();
        }
        
        if (this.outputFile != null) {
            fw.write(sw.toString());
            fw.flush();
            fw.close();
        }
        outputMap.put("outputText", new DataThing(sw.toString()));
        return this.outputMap;
    }

}
