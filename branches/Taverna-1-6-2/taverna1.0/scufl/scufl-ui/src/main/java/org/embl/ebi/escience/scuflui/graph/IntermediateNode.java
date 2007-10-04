/*
 * Created on Feb 24, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

public class IntermediateNode extends VirtualNode
{
	public IntermediateNode(Object edge, Object source, Object target)
	{
		super(0, edge);
		int weight = 1;
		if (source instanceof VirtualNode && target instanceof VirtualNode)
		{
			weight = 8;
		}
		else if (target instanceof VirtualNode || source instanceof VirtualNode)
		{
			weight = 2;
		}
		previous = new Edge(this, source, weight);
		next = new Edge(this, target, weight);
	}

	public Object getSource()
	{
		return previous.getTarget();
	}

	public Edge getSourceEdge()
	{
		return previous;
	}

	public Object getTarget()
	{
		return next.getTarget();
	}

	public Edge getTargetEdge()
	{
		return next;
	}

	public String toString()
	{
		return "Intermediate node (" + previous.getTarget() + "<->" + next.getTarget() + ")";
	}
}
