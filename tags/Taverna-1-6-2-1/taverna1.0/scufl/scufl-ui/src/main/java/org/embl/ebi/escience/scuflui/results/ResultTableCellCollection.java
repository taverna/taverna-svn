/*
 * Created on Sep 15, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.3 $
 */
public class ResultTableCellCollection
{
	protected HashMap cells = new HashMap();
	protected boolean expanded = true;

	public void add(ResultTableCell cell, ResultThing thing)
	{
		cells.put(cell, thing);
		cell.parent = this;
	}

	public ResultTableCell getCell(int row)
	{
		Iterator cellIterator = cells.keySet().iterator();
		while(cellIterator.hasNext())
		{
			ResultTableCell cell = (ResultTableCell) cellIterator.next();
			if (cell.startRow <= row && cell.endRow >= row)
			{
				return cell;
			}
		}
		return null;
	}
	
	public ResultThing getValue(int row)
	{
		return (ResultThing)cells.get(getCell(row));
	}
}
