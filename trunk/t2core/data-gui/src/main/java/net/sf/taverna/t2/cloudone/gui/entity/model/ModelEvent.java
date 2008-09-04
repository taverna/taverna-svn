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
package net.sf.taverna.t2.cloudone.gui.entity.model;

import org.apache.log4j.Logger;

/**
 * Defines the type of events what can happen to an {@link EntityModel}.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 * @param <Model>
 *            What type of {@link EntityModel} the event will represent
 */
public class ModelEvent<Model> {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ModelEvent.class);

	protected final Model entityModel;
	protected final EventType eventType;

	/**
	 * What happened and to who
	 * 
	 * @param eventType
	 *            ADDED or REMOVED
	 * @param entityModel
	 *            the model which has the event
	 */
	public ModelEvent(EventType eventType, Model entityModel) {
		this.eventType = eventType;
		this.entityModel = entityModel;
	}

	public Model getModel() {
		return entityModel;
	}

	/**
	 * What actually happened to trigger the event
	 * 
	 * @return
	 */
	public EventType getEventType() {
		return eventType;
	}

	@Override
	public String toString() {
		return getEventType() + ": " + getModel();
	}

	/**
	 * What is the type of event
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public static enum EventType {
		ADDED, REMOVED
	}

}
