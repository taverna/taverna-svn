package net.sf.taverna.t2.workflowmodel;

import net.sf.taverna.t2.annotation.Annotated;

/**
 * Defines the base interface for a condition which must be satisfied before a
 * processor can commence invocation. Conditions are expressed in terms of a
 * relationship between a controlling and a target processor where the target
 * processor may not commence invocation until all conditions for which it is a
 * target are satisfied in the context of a particular owning process
 * identifier.
 * 
 * @author Tom Oinn
 * 
 */
public interface Condition extends Annotated<Condition> {

	/**
	 * @return the Processor constrained by this condition
	 */
	public Processor getControl();

	/**
	 * @return the Processor acting as the controller for this condition
	 */
	public Processor getTarget();

	/**
	 * @param owningProcess
	 *            the context in which the condition is to be evaluated
	 * @return whether the condition is satisfied
	 */
	public boolean isSatisfied(String owningProcess);

}
