/**
 * 
 */
package uk.org.mygrid.datalineage.model;

import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;

public abstract class AbstractProcessorVertex extends DirectedSparseVertex
		implements ProcessorVertex {

	private String name;

	public String getName() {
		return name;
	}

	public AbstractProcessorVertex(String name) {
		super();
		this.name = name;
	}

}