/**
 * 
 */
package uk.org.mygrid.datalineage.model;

import uk.org.mygrid.datalineage.DataLineageConstants;

public class DataCollectionVertexImpl extends AbstractDataVertex implements
		DataCollectionVertex {

	public DataCollectionVertexImpl(String dataId) {
		super(dataId);
	}

	@Override
	public String getTypeAsString() {
		return DataLineageConstants.DATA_COLLECTION_PREFIX;
	}
}