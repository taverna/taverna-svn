package net.sf.taverna.t2.activities.wsdl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.soap.WSDLSOAPInvoker;

import org.apache.axis.configuration.XMLStringProvider;
import org.xml.sax.SAXException;

/**
 * An asynchronous Activity that is concerned with WSDL based web-services.
 * <p>
 * The activity is configured according to the WSDL location and the operation.<br>
 * The ports are defined dynamically according to the WSDL specification, and in addition an output<br>
 * port <em>attachmentList</em> is added to represent any attachements that are returned by the webservice.
 * </p>
 *
 * @author Stuart Owen
 */
public class WSDLActivity extends AbstractAsynchronousActivity<WSDLActivityConfigurationBean> {
    private WSDLActivityConfigurationBean configurationBean;
    private WSDLParser parser;
    private Map<String,Integer> outputDepth = new HashMap<String, Integer>();

    /**
     * Configures the activity according to the information passed by the configuration bean.<br>
     * During this process the WSDL is parsed to determine the input and output ports.
     * @param bean the {@link WSDLActivityConfigurationBean} configuration bean
     */
    @Override
	public void configure(WSDLActivityConfigurationBean bean) throws ActivityConfigurationException {
        this.configurationBean = bean;
        try {
            parseWSDL();
            configurePorts();
        } catch (Exception ex) {
            throw new ActivityConfigurationException("Unable to parse the WSDL",ex);
        } 
    }

    /**
     * @return a {@link WSDLActivityConfigurationBean} representing the WSDLActivity configuration
     */
    @Override
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
            addInput(descriptor.getName(),descriptor.getDepth(), true,
            		new ArrayList<Class<? extends ReferenceScheme<?>>>(), String.class);
        }
        
        for (TypeDescriptor descriptor : outputDescriptors) {
            List<String>mimeTypes = new ArrayList<String>();
            mimeTypes.add(descriptor.getMimeType());
            addOutput(descriptor.getName(),descriptor.getDepth());
            outputDepth.put(descriptor.getName(), Integer.valueOf(descriptor.getDepth()));
        }
        
        //add output for attachment list
        addOutput("attachmentList", 1);
        outputDepth.put("attachmentList", Integer.valueOf(1));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void executeAsynch(final Map<String, EntityIdentifier> data,
			final AsynchronousActivityCallback callback) {
		
		callback.requestRun(new Runnable() {

			public void run() {
				
				DataFacade dataFacade = new DataFacade(callback.getContext().getDataManager());
				
				Map<String, EntityIdentifier> outputData = new HashMap<String, EntityIdentifier>();
				Map<String,Object> invokerInputMap = new HashMap<String, Object>();
	
				try {
					for (String key : data.keySet()) {
						invokerInputMap.put(key, dataFacade.resolve(data.get(key),String.class));
					}
					List<String> outputNames = new ArrayList<String>();
					for (OutputPort port : getOutputPorts()) {
						outputNames.add(port.getName());
					}
					
					WSDLSOAPInvoker invoker = new T2WSDLSOAPInvoker(parser,configurationBean.getOperation(),outputNames);
					
					/* 
					 * To call secure services - uncomment the following block and comment out the line after it
					// Create security configuration
					 String wssUTProfile = 
							"<deployment xmlns=\"http://xml.apache.org/axis/wsdd/\" "+
							"xmlns:java=\"http://xml.apache.org/axis/wsdd/providers/java\"> \n"+
							"<globalConfiguration> \n" +
							"<requestFlow>\n" +
							"<handler type=\"java:net.sf.taverna.security.T2WSDoAllSender\"> \n"+
							"<parameter name=\"action\" value=\"UsernameToken\"/> \n" +
							"<parameter name=\"passwordType\" value=\"PasswordText\"/> \n"+
							"</handler> \n"+
							"</requestFlow> \n"+
							"</globalConfiguration> \n"+
							"<transport name=\"http\" pivot=\"java:org.apache.axis.transport.http.HTTPSender\"/> \n"+
							"</deployment>\n";
							
					XMLStringProvider wssEngineConfiguration = new XMLStringProvider(wssUTProfile);
														
					Map<String,Object> invokerOutputMap = invoker.invoke(invokerInputMap, wssEngineConfiguration);
					*/
					Map<String,Object> invokerOutputMap = invoker.invoke(invokerInputMap);
					
					for (String outputName : invokerOutputMap.keySet()) {
						Object value = invokerOutputMap.get(outputName);
						
						if (value != null) {
							Integer depth=outputDepth.get(outputName);
							if (depth!=null) {
								outputData.put(outputName, dataFacade.register(value,depth));
							}
							else {
								System.out.println("Depth not recorded for output:"+outputName);
								outputData.put(outputName, dataFacade.register(value));
							}
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
