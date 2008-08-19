package net.sf.taverna.t2.activities.wsdl.xmlsplitter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.xmlsplitter.XMLInputSplitter;
import net.sf.taverna.wsdl.xmlsplitter.XMLSplitterSerialisationHelper;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * An activity that replicates the behaviour of the Taverna 1 XMLInputSplitters.
 * 
 * @author Stuart Owen
 *
 */
public class XMLInputSplitterActivity extends AbstractAsynchronousActivity<XMLSplitterConfigurationBean> {

	XMLSplitterConfigurationBean configBean;
	TypeDescriptor typeDescriptor;
	
	@Override
	public void configure(XMLSplitterConfigurationBean config) throws ActivityConfigurationException {
		configBean = config;
		configurePorts(configBean);
		String xml = configBean.getWrappedTypeXML();
		Element element;
		try {
			element = new SAXBuilder().build(new StringReader(xml)).getRootElement();
		} catch (JDOMException e) {
			throw new ActivityConfigurationException("Error reading xml for XMLInputSplitter",e);
		} catch (IOException e) {
			throw new ActivityConfigurationException("Error reading xml for XMLInputSplitter",e);
		}
		typeDescriptor = XMLSplitterSerialisationHelper.extensionXMLToTypeDescriptor(element);
	}
	
	/**
	 * Returns a TypeDescriptor for the given port name. If the port cannot be found, or is not based upon a complex type, then null is returned.
	 * @param portName
	 * @return
	 */
	public TypeDescriptor getTypeDescriptorForInputPort(String portName) {
		TypeDescriptor result = null;
		if (typeDescriptor instanceof ComplexTypeDescriptor) {
			for (TypeDescriptor desc : ((ComplexTypeDescriptor)typeDescriptor).getElements()) {
				if (desc.getName().equals(portName)) {
					result = desc;
					break;
				}
			}
		}
		return result;
	}
	
	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				try {
					ReferenceService referenceService = callback.getContext().getReferenceService();
					XMLInputSplitter splitter = createSplitter();
					Map<String,Object> inputMap = buildInputMap(data,referenceService);
					Map<String,String> outputMap = splitter.execute(inputMap);
					callback.receiveResult(createOutputData(outputMap,referenceService), new int[0]);
				}
				catch(Exception e) {
					callback.fail("Error in XMLInputSplitterActivity",e);
				}
			}

			private Map<String, T2Reference> createOutputData(
					Map<String, String> outputMap,ReferenceService referenceService) throws ReferenceServiceException {
				Map<String,T2Reference> result = new HashMap<String, T2Reference>();
				for (String outputName : outputMap.keySet()) {
					String xmlOut = outputMap.get(outputName);
					result.put(outputName, referenceService.register(xmlOut, 0, true, callback.getContext()));
				}
				return result;
			}

			private XMLInputSplitter createSplitter() {
				List<String> inputNames = new ArrayList<String>();
				List<String> inputTypes = new ArrayList<String>();
				List<String> outputNames = new ArrayList<String>();
				
				//FIXME: need to use the definition beans for now to get the mimetype. Need to use the actual InputPort once the mimetype becomes available again.
				for (ActivityInputPortDefinitionBean defBean : getConfiguration().getInputPortDefinitions()) {
					inputNames.add(defBean.getName());
					inputTypes.add(defBean.getMimeTypes().get(0));
				}
				
				for (OutputPort outputPorts : getOutputPorts()) {
					outputNames.add(outputPorts.getName());
				}
				
				return new XMLInputSplitter(typeDescriptor,inputNames.toArray(new String[]{}),inputTypes.toArray(new String[]{}),outputNames.toArray(new String[]{}));
			}
			
			private Map<String,Object> buildInputMap(Map<String, T2Reference> data,ReferenceService referenceService) throws ReferenceServiceException {
				Map<String,Object> result = new HashMap<String, Object>();
				for (String inputName : data.keySet()) {
					T2Reference id = data.get(inputName);
					result.put(inputName, referenceService.renderIdentifier(id,String.class, callback.getContext()));
					
				}
				return result;
			}
		});
		
	}

	@Override
	public XMLSplitterConfigurationBean getConfiguration() {
		return configBean;
	}
}
