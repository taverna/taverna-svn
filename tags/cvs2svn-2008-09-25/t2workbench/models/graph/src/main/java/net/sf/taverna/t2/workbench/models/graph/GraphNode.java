/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.models.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * A node of a graph that can optionally contain other graphs.
 * 
 * @author David Withers
 */

public class GraphNode extends GraphElement {

	public enum Shape {BOX, RECORD, HOUSE, INVHOUSE, DOT, CIRCLE, TRIANGLE, INVTRIANGLE}
	
	private Shape shape;
	
	private float width;
	
	private float height;
	
	private List<GraphNode> sourceNodes = new ArrayList<GraphNode>();
	
	private List<GraphNode> sinkNodes = new ArrayList<GraphNode>();

	private Graph graph;
	
	private boolean expanded;
	
	/**
	 * Constructs a new instance of Node.
	 *
	 */
	public GraphNode(GraphEventManager eventManager) {
		super(eventManager);
	}

	/**
	 * Adds a sink node.
	 * 
	 * @param sinkNode the sink node to add
	 */
	public void addSinkNode(GraphNode sinkNode) {
		sinkNode.setParent(this);
		sinkNodes.add(sinkNode);
	}

	/**
	 * Adds a source node.
	 * 
	 * @param sourceNode the source node to add
	 */
	public void addSourceNode(GraphNode sourceNode) {
		sourceNode.setParent(this);
		sourceNodes.add(sourceNode);
	}

	/**
	 * Returns the graph that this node contains.
	 *
	 * @return the graph that this node contains
	 */
	public Graph getGraph() {
		return graph;
	}

	/**
	 * Returns the height.
	 *
	 * @return the height
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * Returns the shape of the node.
	 *
	 * @return the shape of the node
	 */
	public Shape getShape() {
		return shape;
	}

	/**
	 * Returns the sinkNodes.
	 *
	 * @return the sinkNodes
	 */
	public List<GraphNode> getSinkNodes() {
		return sinkNodes;
	}
	
	/**
	 * Returns the sourceNodes.
	 *
	 * @return the sourceNodes
	 */
	public List<GraphNode> getSourceNodes() {
		return sourceNodes;
	}

	/**
	 * Returns the width.
	 *
	 * @return the width
	 */
	public float getWidth() {
		return width;
	}
	
	/**
	 * Returns true if this node is expanded to show the contained graph.
	 *
	 * @return true if this node is expanded
	 */
	public boolean isExpanded() {
		return expanded;
	}
	
	/**
	 * Removes a sink node.
	 * 
	 * @param sinkNode the node to remove
	 * @return true if the node was removed, false otherwise
	 */
	public boolean removeSinkNode(GraphNode sinkNode) {
		return sinkNodes.remove(sinkNode);
	}
	
	/**
	 * Removes a source node.
	 * 
	 * @param sourceNode the node to remove
	 * @return true if the node was removed, false otherwise
	 */
	public boolean removeSourceNode(GraphNode sourceNode) {
		return sourceNodes.remove(sourceNode);
	}

	/**
	 * Sets whether this node is expanded to show the contained graph.
	 *
	 * @param expanded true if this node is expanded
	 */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	/**
	 * Sets the graph that this node contains.
	 *
	 * @param graph the new graph
	 */
	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	/**
	 * Sets the height.
	 *
	 * @param height the new height
	 */
	public void setHeight(float height) {
		this.height = height;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.GraphElement#setSelected(boolean)
	 */
	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		if (isExpanded()) {
			getGraph().setSelected(selected);
		}
	}

	/**
	 * Sets the shape of the node.
	 *
	 * @param shape the new shape of the node
	 */
	public void setShape(Shape shape) {
		this.shape = shape;
	}

	/**
	 * Sets the width.
	 *
	 * @param width the new width
	 */
	public void setWidth(float width) {
		this.width = width;
	}

}
