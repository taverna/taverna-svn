/**
 * 
 */
package net.sf.taverna.t2.lineageService.generation.test;

import java.io.IOException;

import org.jdom.JDOMException;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

/**
 * @author paolo
 *
 */
public class LinearChainGenerator extends DataflowGenerator {


	Processor first, last;
	private String suffix;
	boolean isSerial = false;

	/**
	 * generates a linear chain with one global I and one O using the same processor template
	 * @param name
	 * @param id
	 * @param usingProcessor
	 * @param length
	 * @param target_file_linear
	 * @throws EditException 
	 * @throws DeserializationException 
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	Dataflow generateLinearWorkflow(Dataflow df, String usingProcessor, int length) throws EditException, DeserializationException, JDOMException, IOException {

		// get <length> fresh copies of processor usingProcessor from the template
		//  connect the first to the global input, the last to the output, and build the chain in between

		Processor current, previous=null;
		for (int i=0; i<length; i++) {

			// add current to dataflow
			current = addSingleProcessor(df, usingProcessor, usingProcessor+suffix+"_"+i);

			if (i==0)         first = current;
			if (i==length-1)  last = current;

			if (i>0) {
				 df = connectPorts(df, previous, "Y", current, "X"); //$NON-NLS-1$ //$NON-NLS-2$
				 if (isSerial) 
					 df = addControlLink(df, previous, current);   
			}

			if (i < length-1)  previous = current;
		}
		return df;
	}
	

	/**
	 * @return the last
	 */
	public Processor getLast() {
		return last;
	}

	/**
	 * @param last the last to set
	 */
	public void setLast(Processor last) {
		this.last = last;
	}

	/**
	 * @return the first
	 */
	public Processor getFirst() {
		return first;
	}

	/**
	 * @param first the first to set
	 */
	public void setFirst(Processor first) {
		this.first = first;
	}


	public void setPnameSuffix(String suffix) {
		this.suffix = suffix;
	}


	/**
	 * @return the isSerial
	 */
	public boolean isSerial() {
		return isSerial;
	}


	/**
	 * @param isSerial the isSerial to set
	 */
	public void setSerial(boolean isSerial) {
		this.isSerial = isSerial;
	}


}
