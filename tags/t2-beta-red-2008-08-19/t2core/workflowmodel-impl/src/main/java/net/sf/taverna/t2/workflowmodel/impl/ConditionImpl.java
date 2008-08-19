package net.sf.taverna.t2.workflowmodel.impl;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.annotation.AbstractAnnotatedThing;
import net.sf.taverna.t2.workflowmodel.Condition;

public class ConditionImpl extends AbstractAnnotatedThing<Condition> implements Condition {

	private ProcessorImpl control, target;

	private Map<String, Boolean> stateMap = new HashMap<String, Boolean>();

	protected ConditionImpl(ProcessorImpl control, ProcessorImpl target) {
		this.control = control;
		this.target = target;
	}

	public ProcessorImpl getControl() {
		return this.control;
	}

	public ProcessorImpl getTarget() {
		return this.target;
	}

	public boolean isSatisfied(String owningProcess) {
		//System.out.println("Condition check for : "+owningProcess);
		if (stateMap.containsKey(owningProcess)) {
			//System.out.println("  - "+stateMap.get(owningProcess));
			return stateMap.get(owningProcess);
		} else {
			//System.out.println("  - not defined -> false");
			return false;
		}
	}

	protected void satisfy(String owningProcess) {
		stateMap.put(owningProcess, Boolean.TRUE);
		// TODO - poke target processor here
	}

}
