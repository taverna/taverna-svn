package net.sf.taverna.t2.workflowmodel;

/**
 * Entities existing directly within a workflow such as Processors, Merge
 * operators and other potential future extensions exist within a naming scheme.
 * The local name of an entity is unique relative to the enclosing workflow.
 * Global names are not defined outside of the context of a given instance of a
 * workflow as the same workflow may be re-used in multiple other workflows,
 * there is therefore no single parent defined for some entities and the
 * approach of traversing the hierarchy to build a fully qualified name cannot
 * be applied. A given instance can be treated this way but this depends on
 * dataflow rather than inherent workflow structure.
 * <p>
 * All named workflow entities support the sticky note annotation type
 * 
 * @author Tom Oinn
 * 
 */
public interface NamedWorkflowEntity {

	/**
	 * Every workflow level entity has a name which is unique within the
	 * workflow in which it exists. This only applies to the immediate parent
	 * workflow, names may be duplicated in child workflows etc.
	 */
	public String getLocalName();

}
