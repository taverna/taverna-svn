/**
 * 
 */
package uk.org.mygrid.datalineage.model;

import uk.org.mygrid.datalineage.DataLineageConstants;

public class OutputCollectionVertexImpl extends AbstractDataVertex
		implements OutputCollectionVertex {

	public OutputCollectionVertexImpl(String dataId) {
		super(dataId);
	}

	@Override
	public String getTypeAsString() {
		return DataLineageConstants.DATA_COLLECTION_PREFIX;
	}
}