/*
 * Created on Sep 15, 2004
 */
package org.embl.ebi.escience.scuflui.results;

import java.util.Iterator;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.DataConstraint;


/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 */
public class ResultTableColumn extends ResultTableCompositeCell
{
	private DataConstraint link;

	public ResultTableColumn(ResultTableModel model, DataConstraint link, DataThing thing)
	{
		super(model, thing);
		this.link = link;
		System.out.println("Column created " + toString());
	}

	public String toString()
	{
		if (link.getSource().getProcessor().getName().equals("SCUFL_INTERNAL_SOURCEPORTS"))
		{
			return link.getSource().getName();
		}
		return link.getSource().getProcessor().getName() + ":" + link.getSource().getName();
	}

	public int getDepth()
	{
		if (inputs.isEmpty())
		{
			return 0;
		}
		int depth = 0;
		Iterator inputIterator = inputs.iterator();
		while(inputIterator.hasNext())
		{
			ResultTableColumn column = (ResultTableColumn)inputIterator.next();
			depth = Math.max(depth, column.getDepth());
		}
		return depth + 1;
	}

	public ResultTableColumn getColumn()
	{
		return this;
	}
}