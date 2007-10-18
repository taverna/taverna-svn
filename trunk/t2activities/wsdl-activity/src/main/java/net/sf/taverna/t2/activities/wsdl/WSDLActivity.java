package net.sf.taverna.t2.activities.wsdl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.activities.wsdl.parser.TypeDescriptor;
import net.sf.taverna.t2.activities.wsdl.parser.UnknownOperationException;
import net.sf.taverna.t2.activities.wsdl.parser.WSDLParser;
import net.sf.taverna.t2.activities.wsdl.soap.WSDLSOAPInvoker;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.OutputPort;
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
	public void executeAsynch(final Map<String, EntityIdentifier> data,
			final AsynchronousActivityCallback callback) {
		
		callback.requestRun(new Runnable() {

			public void run() {
				
				DataFacade dataFacade = new DataFacade(callback
						.getLocalDataManager());
				
				Map<String, EntityIdentifier> outputData = new HashMap<String, EntityIdentifier>();
				Map<String,Object> invokerInputMap = new HashMap<String, Object>();
	
				try {
					for (String key : data.keySet()) {
						invokerInputMap.put(key, dataFacade.resolve(data.get(key)));
					}
					List<String> outputNames = new ArrayList<String>();
					for (OutputPort port : getOutputPorts()) {
						outputNames.add(port.getName());
					}
					
					WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(parser,configurationBean.getOperation(),outputNames);
					Map<String,Object> invokerOutputMap = invoker.invoke(invokerInputMap);
					
					for (String outputName : invokerOutputMap.keySet()) {
						Object value = invokerOutputMap.get(outputName);
						if (value != null) {
							outputData.put(outputName, dataFacade.register(value));
						}
					}
				}
				catch(NotFoundException e) {
					callback.fail("Unable to find input data",e);
				}
				catch(Exception e) {
					callback.fail("An error occurred invoking the WSDLActivity",e);
				}
				
				callback.receiveResult(outputData, new int[0]);
				
			}
			
		});
		
	}
}
