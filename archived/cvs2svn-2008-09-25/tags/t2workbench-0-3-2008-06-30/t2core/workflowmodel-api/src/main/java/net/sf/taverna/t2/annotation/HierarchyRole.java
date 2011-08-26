package net.sf.taverna.t2.annotation;

/**
 * Possible relationships between entities in a hierarchical context. This is
 * used as a property of the HierarchyTraversal annotation on members which
 * traverse a conceptual object hierarchy such as a parent-child containment
 * relationship. As an example the getProcessors() method in Dataflow is
 * annotated with <code>&amp;HierarchyRole(role=CHILD)</code> to indicate that
 * it accesses child members of the workflow model containment hierarchy.
 * 
 * @author Tom Oinn
 * 
 */
public enum HierarchyRole {

	CHILD,

	PARENT;

}
