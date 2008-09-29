package net.sf.taverna.t2.workflowmodel.processor.activity;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * Nested workflows/dataflows can come in many shapes and sizes - in-line, url
 * etc. However, they are all {@link Dataflow}s. Implement this in any
 * implementation of a Nested dataflow
 * 
 * @author Ian Dunlop
 * 
 */
public interface NestedDataflow {

	public Dataflow getNestedDataflow();

}
