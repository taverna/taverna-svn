/*
 * Created on Feb 10, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.util.Map;

import org.jgraph.graph.GraphModel;

/**
 * COMMENT 
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1 $
 */
public class LayoutConstants
{
	private final static String LEFT_EDGE = "left edge";
	private final static String RIGHT_EDGE = "right edge";	
	
	private static final String CUT_VALUE = "cut edge";
	private static final String CUT_TIME_STAMP = "cut time stamp";	
	
	private static final String ROW = "row";		
	
	public static Integer getRow(Map map)
	{
		return (Integer)map.get(ROW);
	}
	
	public static void setRow(Map map, int row)
	{
		map.put(ROW, new Integer(row));
	}
		
	public static Map getAttributes(GraphModel model, Object node)
	{
		if(node instanceof VirtualNode)
		{
			return ((VirtualNode)node).getAttributes();
		}
		return model.getAttributes(node);		
	}
	
	public static Integer getCutValue(Map map)
	{
		return (Integer)map.get(CUT_VALUE);		
	}
	
	public static void setCutValue(Map map, int cutValue)
	{
		map.put(CUT_VALUE, new Integer(cutValue));
	}
	
	public static Object getCutValueTimeStamp(Map map)
	{
		return map.get(CUT_TIME_STAMP);
	}
	
	public static void setCutValueTimeStamp(Map map, Object timeStamp)
	{
		map.put(CUT_TIME_STAMP, timeStamp);
	}
	
	public static RowEdge getLeftEdge(Map map)
	{
		return (RowEdge)map.get(LEFT_EDGE);
	}
	
	public static RowEdge getRightEdge(Map map)
	{
		return (RowEdge)map.get(RIGHT_EDGE);
	}
	
	public static void setLeftEdge(Map map, RowEdge edge)
	{
		if(map != null)
		{
			if(edge != null)
			{
				map.put(LEFT_EDGE, edge);
			}
			else
			{
				map.remove(LEFT_EDGE);
			}
		}		
	}
	
	public static void setRightEdge(Map map, RowEdge edge)
	{
		if(map != null)
		{
			if(edge != null)
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
