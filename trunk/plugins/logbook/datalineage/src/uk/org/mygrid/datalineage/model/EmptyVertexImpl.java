/**
 * 
 */
package uk.org.mygrid.datalineage.model;


public class EmptyVertexImpl extends AbstractDataVertex implements
		EmptyVertex {

	private String dataType;

	public EmptyVertexImpl(String dataId, String dataType) {
		super(dataId);
		this.dataType = dataType;
	}

	@Override
	public String getTypeAsString() {
		return dataType;
	}

}