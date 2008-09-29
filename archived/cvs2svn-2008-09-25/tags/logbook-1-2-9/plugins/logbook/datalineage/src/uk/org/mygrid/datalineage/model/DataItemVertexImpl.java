/**
 * 
 */
package uk.org.mygrid.datalineage.model;

import uk.org.mygrid.datalineage.DataLineageConstants;

public class DataItemVertexImpl extends AbstractDataVertex implements
		DataItemVertex {

	public DataItemVertexImpl(String dataId) {
		super(dataId);
	}

	@Override
	public String getTypeAsString() {
		return DataLineageConstants.DATA_ITEM_PREFIX;
	}
}