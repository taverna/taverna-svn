/**
 * 
 */
package net.sf.taverna.t2.lineageService.generation.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.OrderedPair;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;


/**
 * @author paolo
 *
 */
public class DataflowGenerator {

	Edits edits = new EditsImpl();

	XMLSerializer serializer;
	XMLDeserializer deserializer;

	String templateFileName;

	private static Logger logger = Logger.getLogger(DataflowGenerator.class);

	static {
		PluginManager.setRepository(ApplicationRuntime.getInstance().getRavenRepository());
		PluginManager.getInstance();
	}

	public DataflowGenerator() {
		serializer = new XMLSerializerImpl();		
		deserializer = new XMLDeserializerImpl();
	}

	
	public Processor createProcessorWithPorts(Dataflow df, String pname, 
			                                  List<String> inputPortNames, List<String> outputPortNames) {
				
		// get basic processor from template
		Processor p = null;

		try {			
			Processor ptemplate = getProcessorFromTemplate("empty");  // barebone proc. with a beanshell in it			
			Activity<Object> activity = (Activity<Object>) ptemplate.getActivityList().get(0); // this is the beanshell it contains
			
			ActivityPortsDefinitionBean bean = (ActivityPortsDefinitionBean) activity.getConfiguration();

			// add all desired input ports to the activity 
			for (String inputPortName: inputPortNames) {
				
				List<ActivityInputPortDefinitionBean> inputPortsDef = bean.getInputPortDefinitions();
				ActivityInputPortDefinitionBean aipdb = new ActivityInputPortDefinitionBean();

				aipdb.setName(inputPortName);
				aipdb.setDepth(0);
				inputPortsDef.add(aipdb);
			}
			
			// add all desired output ports to the activity 
			for (String outputPortName: outputPortNames) {

				List<ActivityOutputPortDefinitionBean> outputPortsDef = bean.getOutputPortDefinitions();
				ActivityOutputPortDefinitionBean aopdb = new ActivityOutputPortDefinitionBean();

				aopdb.setName(outputPortName);
				aopdb.setDepth(0);
				outputPortsDef.add(aopdb);
			}

			activity.configure(bean);  // reconfig with the new ports
			
			// now create a processor that wraps precisely this configured activity
			p = net.sf.taverna.t2.workflowmodel.impl.Tools.buildFromActivity(activity);
			
			// and add the processor to the dataflow
			edits.getAddProcessorEdit(df, p).doEdit();
			edits.getRenameProcessorEdit(p, pname).doEdit();
			
		} catch (EditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DeserializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ActivityConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return p;
	}
	
	


	/**
	 * deserializes the template <i>each time</i> we need a copy of the processor from it
	 * @param templateProcessorName
	 * @return
	 * @throws EditException 
	 * @throws DeserializationException 
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	public Processor getProcessorFromTemplate(String templateProcessorName) throws DeserializationException, EditException, JDOMException, IOException {

		// pick up the template so we can clone its processors
		SAXBuilder b = new SAXBuilder();

		Document d = b.build(new File(templateFileName));
		Element templateDataflowElement = d.getRootElement();

		Processor processorCopy = null;

		Dataflow templateDataflow = deserializer.deserializeDataflow(templateDataflowElement);

		// pick up the processors from the template
		List<? extends Processor>  processors = templateDataflow.getProcessors();

//		System.out.println("available processors from the template:");
		for (Processor p:processors) { 		
//			System.out.println(p.getLocalName()); 			
			if (p.getLocalName().equals(templateProcessorName)) { processorCopy= p; }
		}

		return processorCopy;		
	}



	/**
	 * creates an input port to the whole dataflow
	 * @param df
	 * @param inputDepth
	 * @param name
	 * @return 
	 * @throws EditException
	 */
	DataflowInputPort createInputPort(Dataflow df, int inputDepth, String name) throws EditException {

		DataflowInputPort ip;
		ip = edits.createDataflowInputPort(name, inputDepth, inputDepth, df); //$NON-NLS-1$
		edits.getAddDataflowInputPortEdit(df, ip).doEdit();
		return ip;
	}





	/**
	 * creates an output port for the whole dataflow
	 * @param df
	 * @param name
	 * @return
	 * @throws EditException
	 */
	DataflowOutputPort createOutputPort(Dataflow df, String name) throws EditException {

		// create single output port
		DataflowOutputPort op;		
		op = edits.createDataflowOutputPort(name, df); //$NON-NLS-1$
		edits.getAddDataflowOutputPortEdit(df, op).doEdit();
		return op;

	}


	/**
	 * connects a global dataflow input to a processor input port 
	 * @param df
	 * @param dataflowInputName
	 * @param p
	 * @param processorInputName
	 * @return
	 * @throws EditException
	 */
	Dataflow connectGlobalInput(Dataflow df, String dataflowInputName, Processor p, String processorInputName) throws EditException {

//		InputPort aIP = Tools.getActivityInputPort(p.getActivityList().get(0), processorInputName);		
//		ProcessorInputPort procInp = edits.createProcessorInputPort(p, aIP.getName(), aIP.getDepth());
//		edits.getAddProcessorInputPortEdit(p, procInp).doEdit();
//		edits.getAddActivityInputPortMappingEdit(p.getActivityList().get(0), processorInputName, processorInputName).doEdit();

		ProcessorInputPort procIn = null;
		Activity<?> a = p.getActivityList().get(0);
		
		for (Entry<String, String> mapEntry : a.getInputPortMapping().entrySet()) {
			
			if (mapEntry.getValue().equals(processorInputName)) {
				for (ProcessorInputPort processorInputPort : p.getInputPorts()) {
					if (processorInputPort.getName().equals(mapEntry.getKey())) {
						procIn = processorInputPort;
						break;
					}
				}
				break;
			}
		}

		List<? extends DataflowInputPort> iPorts = df.getInputPorts();
		EventForwardingOutputPort sourcePort = null;
		for (DataflowInputPort ip:iPorts) {
			if (ip.getName().equals(dataflowInputName)) {
				sourcePort = ip.getInternalOutputPort();
				break;
			}
		}

		if (sourcePort != null) {
			Tools.getCreateAndConnectDatalinkEdit(df, sourcePort, procIn).doEdit();
		} else {
			System.err.println("input port "+dataflowInputName+" not found"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return df;
	}


	/**
	 * connects a processor's output port to a global dataflow output
	 * @param df
	 * @param dataflowOutputName
	 * @param p
	 * @param processorOutputName
	 * @return
	 * @throws EditException
	 */
	Dataflow connectGlobalOutput(Dataflow df, String dataflowOutputName, Processor p, String processorOutputName) throws EditException {

//		OutputPort aOP = Tools.getActivityOutputPort(p.getActivityList().get(0), processorOutputName);		
//		ProcessorOutputPort procOut = edits.createProcessorOutputPort(p, aOP.getName(), 0, 0);
//		edits.getAddProcessorOutputPortEdit(p, procOut).doEdit();
//		edits.getAddActivityOutputPortMappingEdit(p.getActivityList().get(0), processorOutputName, processorOutputName).doEdit();	

		ProcessorOutputPort procOut = null;
		Activity<?> a = p.getActivityList().get(0);
		
		for (Entry<String, String> mapEntry : a.getOutputPortMapping().entrySet()) {
			
			if (mapEntry.getValue().equals(processorOutputName)) {
				for (ProcessorOutputPort processorOutputPort : p.getOutputPorts()) {
					if (processorOutputPort.getName().equals(mapEntry.getKey())) {
						procOut = processorOutputPort;
						break;
					}
				}
				break;
			}
		}
		
		List<? extends DataflowOutputPort> oPorts = df.getOutputPorts();
		EventHandlingInputPort sinkPort = null;
		for (DataflowOutputPort op:oPorts) {
			if (op.getName().equals(dataflowOutputName)) {
				sinkPort = op.getInternalInputPort();
				break;
			}
		}

		if (sinkPort != null) {
			Tools.getCreateAndConnectDatalinkEdit(df, procOut, sinkPort).doEdit();
		} else {
			System.err.println("output port "+dataflowOutputName+" not found"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return df;
	}



	/**
	 * creates a data links between two ports that are known by name
	 * @param df
	 * @param sourceP
	 * @param sourceVar
	 * @param sinkP
	 * @param sinkVar
	 * @return
	 * @throws EditException
	 */
	Dataflow connectPorts(Dataflow df, Processor sourceP, String sourceVar, Processor sinkP, String sinkVar) throws EditException {

		ProcessorOutputPort procOut = null;
		Activity<?> a = sourceP.getActivityList().get(0);
		
		for (Entry<String, String> mapEntry : a.getOutputPortMapping().entrySet()) {
			
			if (mapEntry.getValue().equals(sourceVar)) {
				for (ProcessorOutputPort processorOutputPort : sourceP.getOutputPorts()) {
					if (processorOutputPort.getName().equals(mapEntry.getKey())) {
						procOut = processorOutputPort;
						break;
					}
				}
				break;
			}
		}
		
		
		ProcessorInputPort procIn = null;
		Activity<?> b = sinkP.getActivityList().get(0);
		
		for (Entry<String, String> mapEntry : b.getInputPortMapping().entrySet()) {
			
			if (mapEntry.getValue().equals(sinkVar)) {
				for (ProcessorInputPort processorInputPort : sinkP.getInputPorts()) {
					if (processorInputPort.getName().equals(mapEntry.getKey())) {
						procIn = processorInputPort;
						break;
					}
				}
				break;
			}
		}

		
//		OutputPort aOP = Tools.getActivityOutputPort(sourceP.getActivityList().get(0), sourceVar); //$NON-NLS-1$
//		ProcessorOutputPort procOut = edits.createProcessorOutputPort(sourceP, aOP.getName(), aOP.getDepth(), aOP.getGranularDepth());
//		edits.getAddProcessorOutputPortEdit(sourceP, procOut).doEdit();
//		edits.getAddActivityOutputPortMappingEdit(sourceP.getActivityList().get(0), sourceVar, sourceVar).doEdit();	 //$NON-NLS-1$ //$NON-NLS-2$

//		InputPort aIP = Tools.getActivityInputPort(sinkP.getActivityList().get(0), sinkVar);		 //$NON-NLS-1$
//		ProcessorInputPort procIn = edits.createProcessorInputPort(sinkP, aIP.getName(), aIP.getDepth());
//		edits.getAddProcessorInputPortEdit(sinkP, procIn).doEdit();
//		edits.getAddActivityInputPortMappingEdit(sinkP.getActivityList().get(0), sinkVar, sinkVar).doEdit(); //$NON-NLS-1$ //$NON-NLS-2$

		if (procOut == null) { 
			System.out.println("output port "+sourceP.getLocalName()+":"+sourceVar+" not found");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return df; 
		}

		if (procIn == null) { 
			System.out.println("input port "+sinkP.getLocalName()+":"+sinkVar+" not found");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return df; 
		}

		Tools.getCreateAndConnectDatalinkEdit(df, (EventForwardingOutputPort) procOut, (EventHandlingInputPort) procIn).doEdit();

		return df;
	}




	Dataflow addControlLink(Dataflow df, Processor controlProcessor, Processor targetProcessor) throws EditException  {

		Edit<OrderedPair<Processor>> addConditionEdit = edits
		.getCreateConditionEdit(controlProcessor, targetProcessor);

		addConditionEdit.doEdit();

		return df;

	}


	Dataflow createEmptyDataflow(String name, String id) throws EditException {

		Edits edits = new EditsImpl();

		// generate new dataflow		
		Dataflow df = edits.createDataflow();
		edits.getUpdateDataflowNameEdit(df, name).doEdit();
		edits.getUpdateDataflowInternalIdentifierEdit(df, id).doEdit();

		return df;
	}


	public Processor addSingleProcessor(Dataflow df, String pname, String withName) throws DeserializationException, EditException, JDOMException, IOException {

		Processor p = getProcessorFromTemplate(pname);

		Activity<Object> activity = (Activity<Object>) p.getActivityList().get(0); // this is the beanshell it contains

		p = net.sf.taverna.t2.workflowmodel.impl.Tools.buildFromActivity(activity);

		edits.getAddProcessorEdit(df, p).doEdit();
		edits.getRenameProcessorEdit(p, withName).doEdit(); //$NON-NLS-1$
		return p;
	}


	void writeSerializedDataflow(Dataflow df, String target) throws SerializationException, IOException {

		Element serialized = serializer.serializeDataflow(df);

		XMLOutputter outputter = new XMLOutputter();
		String dfXML = outputter.outputString(serialized);

		FileWriter fw = new FileWriter(target);

		fw.write(dfXML); fw.close();

//		System.out.println("serialized dataflow: "+dfXML+" written to "+target); //$NON-NLS-1$ //$NON-NLS-2$

	}	


	/**
	 * @return the templateFileName
	 */
	public String getTemplateFileName() {
		return templateFileName;
	}


	/**
	 * @param templateFileName the templateFileName to set
	 */
	public void setTemplateFileName(String templateFileName) {
		this.templateFileName = templateFileName;
	}



}



