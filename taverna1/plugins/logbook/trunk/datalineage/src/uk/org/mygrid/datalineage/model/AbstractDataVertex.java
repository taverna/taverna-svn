/**
 * 
 */
package uk.org.mygrid.datalineage.model;

import java.util.List;

import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;

public abstract class AbstractDataVertex extends DirectedSparseVertex
		implements DataVertex {

	private String dataId;
	
	private List<String> inputNames;
	
	private String name;

	public String getDataId() {
		return dataId;
	}

	public String getDataLsid() {
		return getTypeAsString() + getDataId();
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<String> getInputNames() {
		return inputNames;
	}

	public void setInputNames(List<String> inputNames) {
		this.inputNames = inputNames;
	}

	public abstract String getTypeAsString();

	public AbstractDataVertex(String dataId) {
		super();
		this.dataId = dataId;
	}

	@Override
	public String toString() {
		return dataId;
	}

}