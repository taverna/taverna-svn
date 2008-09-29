package net.sf.taverna.t2.workflowmodel.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.FailureTransmitter;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.NamedWorkflowEntity;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationTypeMismatchException;

public class DummyDataflow implements Dataflow{

	public  List<DataflowInputPort> inputPorts = new ArrayList<DataflowInputPort>();
	public  List<DataflowOutputPort> outputPorts = new ArrayList<DataflowOutputPort>();
	public List<Processor> processors = new ArrayList<Processor>();
	public List<Merge> merges = new ArrayList<Merge>();
	
	
	
	public DataflowValidationReport checkValidity() {
		// TODO Auto-generated method stub
		return null;
	}
	

	public <T extends NamedWorkflowEntity> List<? extends T> getEntities(
			Class<T> entityType) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<? extends DataflowInputPort> getInputPorts() {
		return inputPorts;
	}

	public List<? extends Datalink> getLinks() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<? extends DataflowOutputPort> getOutputPorts() {
		return outputPorts;
	}

	public List<? extends Processor> getProcessors() {
		return processors;
	}

	public List<? extends Merge> getMerges() {
		return merges;
	}
	
	public Edit<? extends Dataflow> getAddAnnotationEdit(
			AnnotationChain newAnnotation) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<? extends AnnotationChain> getAnnotations() {
		// TODO Auto-generated method stub
		return null;
	}

	public Edit<? extends Dataflow> getRemoveAnnotationEdit(
			AnnotationChain annotationToRemove) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLocalName() {
		return "test_dataflow";
	}


	public void fire(String owningProcess, InvocationContext context) {
		String newOwningProcess = owningProcess + ":" + getLocalName();
		for (Processor p : processors) {
			if (p.getInputPorts().isEmpty()) {
				p.fire(newOwningProcess, context);
			}
		}
	}


	public FailureTransmitter getFailureTransmitter() {
		// TODO Auto-generated method stub
		return null;
	}


	public boolean doTypeCheck() throws IterationTypeMismatchException {
		throw new UnsupportedOperationException("Not implemented for this class");
	}

}
