/**
 * 
 */
package net.sf.taverna.t2.lineageService.generation.test;

import java.io.IOException;
import java.util.List;

import org.jdom.JDOMException;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

/**
 * @author paolo
 *
 */
public class BaseWorkflowGenerator extends DataflowGenerator {

	
	/**
	 * the simple 1-I / 1-O / 1-P workflow
	 * @param df
	 * @return
	 * @throws EditException
	 * @throws SerializationException
	 * @throws IOException
	 * @throws DeserializationException
	 * @throws JDOMException 
	 */
	Dataflow generateBaseWorkflow(Dataflow df) throws EditException, SerializationException, IOException, DeserializationException, JDOMException {
		
		// create input ports
		DataflowInputPort ip;
		
		ip = edits.createDataflowInputPort("I1", 0, 0, df); //$NON-NLS-1$
		edits.getAddDataflowInputPortEdit(df, ip).doEdit();
		
		ip = edits.createDataflowInputPort("I2", 0, 0, df); //$NON-NLS-1$
		edits.getAddDataflowInputPortEdit(df, ip).doEdit();

		// create output ports
		DataflowOutputPort op;
		
		op = edits.createDataflowOutputPort("O", df); //$NON-NLS-1$
		edits.getAddDataflowOutputPortEdit(df, op).doEdit();
		
		// get fresh copy of processor P1 from the template
		Processor p = getProcessorFromTemplate("P1"); //$NON-NLS-1$
		
		// add P to dataflow
		edits.getAddProcessorEdit(df, p).doEdit();
		
		// connect the new processor to the inputs
		List<? extends DataflowInputPort> iPorts = df.getInputPorts();
		List<? extends DataflowOutputPort> oPorts = df.getOutputPorts();

		// we know that P only has a (beanshell) activity...
		Activity a = p.getActivityList().get(0);

		//  connect I1 to X1
		// get input port X1 from processor
		InputPort aIP = Tools.getActivityInputPort(a, "X1");		 //$NON-NLS-1$
		ProcessorInputPort procInp = edits.createProcessorInputPort(p, aIP.getName(), aIP.getDepth());
		edits.getAddProcessorInputPortEdit(p, procInp).doEdit();
		edits.getAddActivityInputPortMappingEdit(a, "X1", "X1").doEdit(); //$NON-NLS-1$ //$NON-NLS-2$
		
		EventForwardingOutputPort sourcePort = ((DataflowInputPort) iPorts.get(0)).getInternalOutputPort();
		Tools.getCreateAndConnectDatalinkEdit(df, sourcePort, procInp).doEdit();

		//  connect I2 to X2
		// get input port X1 from processor
		aIP = Tools.getActivityInputPort(a, "X2");		 //$NON-NLS-1$
		procInp = edits.createProcessorInputPort(p, aIP.getName(), aIP.getDepth());
		edits.getAddProcessorInputPortEdit(p, procInp).doEdit();
		edits.getAddActivityInputPortMappingEdit(a, "X2", "X2").doEdit();		 //$NON-NLS-1$ //$NON-NLS-2$

		sourcePort = ((DataflowInputPort) iPorts.get(1)).getInternalOutputPort();
		Tools.getCreateAndConnectDatalinkEdit(df, sourcePort, procInp).doEdit();

		//  connect Y1 to O
		OutputPort aOP = Tools.getActivityOutputPort(a, "Y1");		 //$NON-NLS-1$
		ProcessorOutputPort procOut = edits.createProcessorOutputPort(p, aOP.getName(), 0, 0);
		edits.getAddProcessorOutputPortEdit(p, procOut).doEdit();
		edits.getAddActivityOutputPortMappingEdit(a, "Y1", "Y1").doEdit();	 //$NON-NLS-1$ //$NON-NLS-2$
		
		EventHandlingInputPort sinkPort = ((DataflowOutputPort) oPorts.get(0)).getInternalInputPort();
		Tools.getCreateAndConnectDatalinkEdit(df, procOut, sinkPort).doEdit();
		
		return df;			
	}
	
	
	
}
