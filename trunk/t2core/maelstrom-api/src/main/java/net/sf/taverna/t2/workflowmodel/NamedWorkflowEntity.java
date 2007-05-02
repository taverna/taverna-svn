package net.sf.taverna.t2.workflowmodel;

/**
 * Entities existing directly within a workflow such as Processors, Merge
 * operators and other potential future extensions exist within a naming scheme.
 * The local name of an entity is unique relative to the enclosing workflow but
 * in addition all such entities have a global name consisting of the global
 * name of the parent entity, a colon (:) character and the local name
 * concatenated together. This allows the unique identification of any workflow
 * level entity within a set of arbitrarily nested workflows.
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
