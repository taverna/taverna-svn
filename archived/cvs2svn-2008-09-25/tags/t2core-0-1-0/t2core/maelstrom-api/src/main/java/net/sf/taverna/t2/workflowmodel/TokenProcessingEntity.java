package net.sf.taverna.t2.workflowmodel;

import static net.sf.taverna.t2.annotation.HierarchyRole.CHILD;

import java.util.List;

import net.sf.taverna.t2.annotation.HierarchyTraversal;

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

}
