/**
 * 
 */
package uk.org.mygrid.datalineage.model;

import uk.org.mygrid.datalineage.DataLineageConstants;

public class InputCollectionVertexImpl extends AbstractDataVertex
		implements InputCollectionVertex {

	public InputCollectionVertexImpl(String dataId) {
		super(dataId);
	}

	@Override
	public String getTypeAsString() {
		return DataLineageConstants.DATA_COLLECTION_PREFIX;
	}
}