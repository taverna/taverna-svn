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

import java.awt.Color;

import net.sf.taverna.t2.workbench.models.graph.Graph.LineStyle;

/**
 * An element of a graph.
 * 
 * @author David Withers
 */
public abstract class GraphElement {

	private String id;
	
	private String label;
	
	private LineStyle lineStyle;
	
	private Color color;
	
	private Color fillColor;
	
	private GraphElement parent;
	
	private boolean selected;
	
	private Object dataflowObject;
	
	private GraphEventManager eventManager;
	
	protected float complete;

	protected GraphElement(GraphEventManager eventManager) {
		this.eventManager = eventManager;
	}
	
	/**
	 * Returns the eventManager.
	 *
	 * @return the eventManager
	 */
	public GraphEventManager getEventManager() {
		return eventManager;
	}

	/**
	 * Returns the dataflowObject.
	 *
	 * @return the dataflowObject
	 */
	public Object getDataflowObject() {
		return dataflowObject;
	}

	/**
	 * Sets the dataflowObject.
	 *
	 * @param dataflowObject the new dataflowObject
	 */
	public void setDataflowObject(Object dataflowObject) {
		this.dataflowObject = dataflowObject;
	}

	/**
	 * Returns the parent.
	 *
	 * @return the parent
	 */
	public GraphElement getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent the new parent
	 */
	protected void setParent(GraphElement parent) {
		this.parent = parent;
	}

	/**
	 * Returns the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Returns the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the colour.
	 *
	 * @return the colour
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the colour.
	 *
	 * @param color the new colour
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Returns the fillColor.
	 *
	 * @return the fillColor
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * Sets the fillColor.
	 *
	 * @param fillColor the new fillColor
	 */
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	/**
	 * Returns the lineStyle.
	 *
	 * @return the lineStyle
	 */
	public LineStyle getLineStyle() {
		return lineStyle;
	}

	/**
	 * Sets the lineStyle.
	 *
	 * @param lineStyle the new lineStyle
	 */
	public void setLineStyle(LineStyle lineStyle) {
		this.lineStyle = lineStyle;
	}

	public String toString() {
		return id + "[" + label + "]";
	}

	/**
	 * Returns the selected.
	 *
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Sets the selected.
	 *
	 * @param selected the new selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	/**
	 * Sets the completed value.
	 * 
	 * @param complete
	 */
	public void setCompleted(float complete) {
		this.complete = complete;
	}

}
