package net.sf.taverna.t2.workflowmodel;

import static net.sf.taverna.t2.annotation.HierarchyRole.CHILD;

import java.util.List;

import net.sf.taverna.t2.annotation.HierarchyTraversal;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationTypeMismatchException;

/**
 * Superinterface for all classes within the workflow model which consume and
 * emit workflow data tokens.
 * 
 * @author Tom Oinn
 * 
 */
public interface TokenProcessingEntity extends NamedWorkflowEntity {

	@HierarchyTraversal(hierarchies = { "workflowStructure" }, role = { CHILD })
	public List<? extends EventHandlingInputPort> getInputPorts();

	@HierarchyTraversal(hierarchies = { "workflowStructure" }, role = { CHILD })
	public List<? extends EventForwardingOutputPort> getOutputPorts();

	/**
	 * Run a collection level based type check on the token processing entity
	 * 
	 * @return true if the typecheck was successful or false if the check failed
	 *         because there were preconditions missing such as unsatisfied
	 *         input types
	 * @throws IterationTypeMismatchException
	 *             if the typing occurred but didn't match because of an
	 *             iteration mismatch
	 * @throws InvalidDataflowException 
	 * 			 	if the entity depended on a dataflow that was not valid
	 */
	public boolean doTypeCheck() throws IterationTypeMismatchException, InvalidDataflowException;
	
}
