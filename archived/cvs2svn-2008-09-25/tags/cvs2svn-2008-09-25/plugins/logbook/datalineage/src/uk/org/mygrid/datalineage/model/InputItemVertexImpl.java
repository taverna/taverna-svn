/**
 * 
 */
package uk.org.mygrid.datalineage.model;

import uk.org.mygrid.datalineage.DataLineageConstants;

public class InputItemVertexImpl extends AbstractDataVertex implements
		InputItemVertex {

	public InputItemVertexImpl(String dataId) {
		super(dataId);
	}

	@Override
	public String getTypeAsString() {
		return DataLineageConstants.DATA_ITEM_PREFIX;
	}

}