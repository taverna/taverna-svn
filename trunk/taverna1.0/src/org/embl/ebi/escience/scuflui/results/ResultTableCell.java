/*
 * Created on Sep 15, 2004
 */
package org.embl.ebi.escience.scuflui.results;



public class ResultTableCell
{
	protected int startRow = 0;
	protected int endRow = 0;
	
	protected ResultTableCellCollection parent;
	
	public ResultTableCell(int startRow)
	{
		this.startRow = startRow;
	}
}