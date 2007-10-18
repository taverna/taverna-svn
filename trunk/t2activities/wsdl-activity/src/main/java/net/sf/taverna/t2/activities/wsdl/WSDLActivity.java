package net.sf.taverna.t2.activities.wsdl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPortBuilder;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityPortBuilderImpl;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.TypeDescriptor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.UnknownOperationException;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.WSDLParser;
import org.xml.sax.SAXException;

/**
 *
 * @author Stuart Owen
 */
public class WSDLActivity extends AbstractActivity<WSDLActivityConfigurationBean> {
    private WSDLActivityConfigurationBean configurationBean;
    private WSDLParser parser;

    protected ActivityPortBuilder getPortBuilder() {
        return ActivityPortBuilderImpl.getInstance();
    }

    public void configure(WSDLActivityConfigurationBean bean) throws ActivityConfigurationException {
        this.configurationBean = bean;
        try {
            parseWSDL();
        } catch (Exception ex) {
            throw new ActivityConfigurationException("Unable to parse the WSDL",ex);
        } 
    }

    public WSDLActivityConfigurationBean getConfiguration() {
        return configurationBean;
    }
    
    private void parseWSDL() throws ParserConfigurationException,
			WSDLException, IOException, SAXException, UnknownOperationException {
        parser=new WSDLParser(configurationBean.getWsdl());
        List<TypeDescriptor> inputDescriptors=parser.getOperationInputParameters(configurationBean.getOperation());
        List<TypeDescriptor> outputDescriptors=parser.getOperationOutputParameters(configurationBean.getOperation());
        for (TypeDescriptor descriptor : inputDescriptors) {
            List<String>mimeTypes = new ArrayList<String>();
            mimeTypes.add(descriptor.getType());
            addInput(descriptor.getName(),determineDepthFromSyntacticType(descriptor.getType()),mimeTypes);
        }
        
        for (TypeDescriptor descriptor : outputDescriptors) {
            List<String>mimeTypes = new ArrayList<String>();
            mimeTypes.add(descriptor.getType());
            //addOutput(descriptor.getName(),determineDepthFromSyntacticType(descriptor.getType()),mimeTypes);
            addOutput(descriptor.getName(),1,mimeTypes);
        }  
    }
    
    /**
     * @param syntacticType
     * @return the depth determined from the syntactic mime type of the original
     *         port. i.e text/plain = 0, l('text/plain') = 1, l(l('text/plain')) =
     *         2, ... etc.
     */
    private int determineDepthFromSyntacticType(String syntacticType) {
            if (syntacticType == null) {
                    return 0;
            } else {
                    return syntacticType.split("l\\(").length - 1;
            }
    }
    
}
