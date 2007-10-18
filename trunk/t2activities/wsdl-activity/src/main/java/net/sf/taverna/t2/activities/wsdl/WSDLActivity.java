package net.sf.taverna.t2.activities.wsdl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.activities.wsdl.parser.TypeDescriptor;
import net.sf.taverna.t2.activities.wsdl.parser.UnknownOperationException;
import net.sf.taverna.t2.activities.wsdl.parser.WSDLParser;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPortBuilder;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityPortBuilderImpl;

import org.xml.sax.SAXException;

/**
 *
 * @author Stuart Owen
 */
public class WSDLActivity extends AbstractAsynchronousActivity<WSDLActivityConfigurationBean> {
    private WSDLActivityConfigurationBean configurationBean;
    private WSDLParser parser;

    protected ActivityPortBuilder getPortBuilder() {
        return ActivityPortBuilderImpl.getInstance();
    }

    public void configure(WSDLActivityConfigurationBean bean) throws ActivityConfigurationException {
        this.configurationBean = bean;
        try {
            parseWSDL();
            configurePorts();
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
          
    }

	private void configurePorts() throws UnknownOperationException, IOException {
		List<TypeDescriptor> inputDescriptors=parser.getOperationInputParameters(configurationBean.getOperation());
        List<TypeDescriptor> outputDescriptors=parser.getOperationOutputParameters(configurationBean.getOperation());
        for (TypeDescriptor descriptor : inputDescriptors) {
            List<String>mimeTypes = new ArrayList<String>();
            mimeTypes.add(descriptor.getMimeType());
            addInput(descriptor.getName(),descriptor.getDepth(),mimeTypes);
        }
        
        for (TypeDescriptor descriptor : outputDescriptors) {
            List<String>mimeTypes = new ArrayList<String>();
            mimeTypes.add(descriptor.getMimeType());
            addOutput(descriptor.getName(),descriptor.getDepth(),mimeTypes);
        }
	}

	@Override
	public void executeAsynch(Map<String, EntityIdentifier> data,
			AsynchronousActivityCallback callback) {
		
		
	}
}
