package net.sf.taverna.t2.activities.wsdl.xmlsplitter;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.EmptyListException;
import net.sf.taverna.t2.cloudone.datamanager.MalformedListException;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.UnsupportedObjectTypeException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
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
	public void executeAsynch(final Map<String, EntityIdentifier> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				try {
					DataFacade dataFacade = new DataFacade(callback.getContext().getDataManager());
					XMLOutputSplitter splitter = createSplitter();
					Map<String,String> inputMap = buildInputMap(data,dataFacade);
					Map<String,Object> outputMap = splitter.execute(inputMap);
					callback.receiveResult(createOutputData(outputMap,dataFacade), new int[0]);
				}
				catch(Exception e) {
					callback.fail("Error in XMLInputSplitterActivity",e);
				}
			}

			private Map<String, EntityIdentifier> createOutputData(
					Map<String, Object> outputMap,DataFacade dataFacade) throws EmptyListException, MalformedListException, UnsupportedObjectTypeException {
				Map<String,EntityIdentifier> result = new HashMap<String, EntityIdentifier>();
				for (String outputName : outputMap.keySet()) {
					Object output = outputMap.get(outputName);
					result.put(outputName, dataFacade.register(output));
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
			
			private Map<String,String> buildInputMap(Map<String, EntityIdentifier> data,DataFacade dataFacade) throws RetrievalException, NotFoundException {
				Map<String,String> result = new HashMap<String, String>();
				for (String inputName : data.keySet()) {
					EntityIdentifier id = data.get(inputName);
					result.put(inputName, (String)dataFacade.resolve(id,String.class));
					
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
