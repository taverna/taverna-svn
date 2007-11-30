package net.sf.taverna.t2.workflowmodel.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.annotation.WorkflowAnnotation;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorHealthReport;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityHealthReport;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyStack;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationTypeMismatchException;

public class DummyProcessor implements Processor{

	public ProcessorHealthReport checkProcessorHealth() {
		return new ProcessorHealthReportImpl(new ArrayList<ActivityHealthReport>());
	}

	public String firedOwningProcess = null;
	
	public List<Condition> preConditionList = new ArrayList<Condition>();
	public List<ProcessorInputPort> inputPorts = new ArrayList<ProcessorInputPort>();
	
	public boolean doTypeCheck() throws IterationTypeMismatchException {
		// TODO Auto-generated method stub
		return false;
	}

	public void fire(String owningProcess, InvocationContext context) {
		firedOwningProcess=owningProcess;
		
	}

	public List<? extends Activity<?>> getActivityList() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<? extends Condition> getControlledPreconditionList() {
		// TODO Auto-generated method stub
		return null;
	}

	public DispatchStack getDispatchStack() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<? extends ProcessorInputPort> getInputPorts() {
		return inputPorts;
	}

	public IterationStrategyStack getIterationStrategy() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<? extends ProcessorOutputPort> getOutputPorts() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<? extends Condition> getPreconditionList() {
		return preConditionList;
	}

	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Edit<? extends Processor> getAddAnnotationEdit(
			WorkflowAnnotation newAnnotation) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<? extends WorkflowAnnotation> getAnnotations() {
		// TODO Auto-generated method stub
		return null;
	}

	public Edit<? extends Processor> getRemoveAnnotationEdit(
			WorkflowAnnotation annotationToRemove) {
		// TODO Auto-generated method stub
		return null;
	}

	public Edit<? extends Processor> getReplaceAnnotationEdit(
			WorkflowAnnotation oldAnnotation, WorkflowAnnotation newAnnotation) {
		// TODO Auto-generated method stub
		return null;
	}

}
