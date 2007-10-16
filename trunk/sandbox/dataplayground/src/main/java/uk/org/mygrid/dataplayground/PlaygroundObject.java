package uk.org.mygrid.dataplayground;

import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;

public abstract class PlaygroundObject extends DirectedSparseVertex {

	String name;
	Boolean interesting;

	public PlaygroundObject() {
		super();
	}

	public String getName() {
		return name;
	}

	public Boolean getInteresting() {
		return interesting;
	}

	public void setInteresting(Boolean interesting) {
		this.interesting = interesting;
	}

	public void setName(String name) {
		this.name = name;
	}
}
