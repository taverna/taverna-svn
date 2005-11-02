package net.sourceforge.taverna.scuflworkers.xml;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.xpath.XPathException;
import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class allows the user to create XQuery's against
 * an XML document.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.3 $
 */
public class XQueryWorker implements LocalWorker {

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        HashMap outputMap = new HashMap();
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        String script = inAdapter.getString("script");
        String inputDoc = inAdapter.getString("inputdocText");
        
        Configuration config = new Configuration();
        StaticQueryContext staticContext = new StaticQueryContext(config);
        DynamicQueryContext context = new DynamicQueryContext(config);
        try {
           
            XQueryExpression exp =  staticContext.compileQuery(script);
            StreamSource doc = new StreamSource(new StringReader(inputDoc));
            DocumentInfo docInfo = staticContext.buildDocument(doc);
            context.setContextNode(docInfo);
            
            
            // create output buffer
            OutputStream outputStream = new ByteArrayOutputStream();
            BufferedOutputStream buff = new BufferedOutputStream(outputStream);
            StringBuffer sb = new StringBuffer();
            
            // create output properties
            Properties props = new Properties();
            props.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            props.setProperty(OutputKeys.INDENT, "yes");
            props.setProperty(OutputKeys.STANDALONE, "yes");
            props.setProperty(OutputKeys.METHOD,"text");

            DynamicQueryContext dynamicContext = new DynamicQueryContext(config);
            dynamicContext.setContextNode(docInfo);
            SequenceIterator iter = exp.iterator(dynamicContext);
            QueryResult.serializeSequence(iter, config, buff, props);
            
            //QueryResult.serializeSequence(exp.iterator(context), config,buff,null);
            //System.out.println(buff.toString());
            outAdapter.putString("outputText", buff.toString());
        } catch (XPathException e) {
            throw new TaskExecutionException(e);
        }
        
        
        return outputMap;
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
        return new String[]{"script", "inputdocText"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
       return new String[]{"'text/plain'"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
     */
    public String[] outputNames() {
        return new String[]{"results"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
     */
    public String[] outputTypes() {
       return new String[]{"l('text/plain')"};
    }

}
