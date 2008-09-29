/*
 * Created on Feb 10, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.util.Map;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1.2.1 $
 */
public class LayoutConstants
{
	private final static String LEFT_EDGE = "left edge";
	private final static String RIGHT_EDGE = "right edge";

	private static final String ROW = "row";

	public static PositionLayout.Row getRow(Map map)
	{
		return (PositionLayout.Row) map.get(ROW);
	}

	public static void setRow(Map map, PositionLayout.Row row)
	{
		map.put(ROW, row);
	}

	public static Edge getLeftEdge(Map map)
	{
		return (Edge) map.get(LEFT_EDGE);
	}

	public static Edge getRightEdge(Map map)
	{
		return (Edge) map.get(RIGHT_EDGE);
	}

	public static void setLeftEdge(Map map, Edge edge)
	{
		if (map != null)
		{
			if (edge != null)
			{
				map.put(LEFT_EDGE, edge);
			}
			else
			{
				map.remove(LEFT_EDGE);
			}
		}
	}

	public static void setRightEdge(Map map, Edge edge)
	{
		if (map != null)
		{
			if (edge != null)
			{
				map.put(RIGHT_EDGE, edge);
			}
			else
			{
				map.remove(RIGHT_EDGE);
			}
		}
	}
}
