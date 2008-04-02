/**
 * 
 */
package uk.org.mygrid.datalineage.model;

import uk.org.mygrid.datalineage.DataLineageConstants;

public class OutputItemVertexImpl extends AbstractDataVertex implements
		OutputItemVertex {

	public OutputItemVertexImpl(String dataId) {
		super(dataId);
	}

	@Override
	public String getTypeAsString() {
		return DataLineageConstants.DATA_ITEM_PREFIX;
	}

}