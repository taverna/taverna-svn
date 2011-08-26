/*
 * Created on Sep 15, 2004
 */
package org.embl.ebi.escience.scuflui.results;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.2 $
 */
public class ResultTableCell
{
	protected int startRow = 0;
	protected int endRow = 0;
	
	protected ResultTableCellCollection parent;
	
	/**
	 * @param startRow the row where the cell starts
	 */
	public ResultTableCell(int startRow)
	{
		this.startRow = startRow;
	}
}