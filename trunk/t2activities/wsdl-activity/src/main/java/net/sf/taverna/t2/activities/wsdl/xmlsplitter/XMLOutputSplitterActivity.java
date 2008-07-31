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
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.xmlsplitter.XMLOutputSplitter;
import net.sf.taverna.wsdl.xmlsplitter.XMLSplitterSerialisationHelper;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * An activity that replicates the behaviour of the Taverna 1 XMLOutputSplitter.
 * 
 * @author Stuart Owen
 *
 */
public class XMLOutputSplitterActivity extends AbstractAsynchronousActivity<XMLSplitterConfigurationBean>{

	XMLSplitterConfigurationBean configBean;
	TypeDescriptor typeDescriptor;
	
	@Override
	public void configure(XMLSplitterConfigurationBean configBean)
			throws ActivityConfigurationException {
		this.configBean = configBean;
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

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				try {
					ReferenceService referenceService = callback.getContext().getReferenceService();
					XMLOutputSplitter splitter = createSplitter();
					Map<String,String> inputMap = buildInputMap(data,referenceService);
					Map<String,Object> outputMap = splitter.execute(inputMap);
					callback.receiveResult(createOutputData(outputMap,referenceService), new int[0]);
				}
				catch(Exception e) {
					callback.fail("Error in XMLInputSplitterActivity",e);
				}
			}

			private Map<String, T2Reference> createOutputData(
					Map<String, Object> outputMap,ReferenceService referenceService) throws ReferenceServiceException {
				Map<String,T2Reference> result = new HashMap<String, T2Reference>();
				for (String outputName : outputMap.keySet()) {
					Object output = outputMap.get(outputName);
					//TODO check if the output can be anything other than String or List
					if (output instanceof List) {
						result.put(outputName, referenceService.register(output, 1, true, callback.getContext()));
					} else {
						result.put(outputName, referenceService.register(output, 0, true, callback.getContext()));
					}
				}
				return result;
			}

			private XMLOutputSplitter createSplitter() {
				List<String> inputNames = new ArrayList<String>();
				List<String> outputTypes = new ArrayList<String>();
				List<String> outputNames = new ArrayList<String>();
				
				//FIXME: need to use the definition beans for now to get the mimetype. Need to use the actual InputPort once the mimetype becomes available again.
				for (ActivityOutputPortDefinitionBean defBean : getConfiguration().getOutputPortDefinitions()) {
					outputNames.add(defBean.getName());
					outputTypes.add(defBean.getMimeTypes().get(0));
				}
				
				for (InputPort outputPorts : getInputPorts()) {
					inputNames.add(outputPorts.getName());
				}
				
				return new XMLOutputSplitter(typeDescriptor,outputNames.toArray(new String[]{}),outputTypes.toArray(new String[]{}),inputNames.toArray(new String[]{}));
			}
			
			private Map<String,String> buildInputMap(Map<String, T2Reference> data,ReferenceService referenceService) throws ReferenceServiceException {
				Map<String,String> result = new HashMap<String, String>();
				for (String inputName : data.keySet()) {
					T2Reference id = data.get(inputName);
					result.put(inputName, (String)referenceService.renderIdentifier(id,String.class, callback.getContext()));
					
				}
				return result;
			}
		});
	}

	public TypeDescriptor getTypeDescriptorForOutputPort(String portName) {
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
	public XMLSplitterConfigurationBean getConfiguration() {
		return configBean;
	}
}
