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

import java.util.List;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * Represents a Literal String or a Blob containing a string
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class StringModel extends EntityModel implements
		Observable<StringModelEvent> {
	/*
	 * Sends notifications of events to registered observers
	 */
	private MultiCaster<StringModelEvent> multiCaster = new MultiCaster<StringModelEvent>(
			this);
	@SuppressWarnings("unused")
	private EntityListModel parentModel;
	private String string;

	public StringModel(EntityListModel parentModel) {
		super(parentModel);
		this.parentModel = parentModel;
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(StringModel.class);

	/**
	 * If you want to be notified of events regarding this model. Uses the
	 * {@link MultiCaster}
	 */
	public void addObserver(Observer<StringModelEvent> observer) {
		multiCaster.addObserver(observer);

	}

	/**
	 * If you no longer wish to be informed of events regarding this model. Uses
	 * the {@link MultiCaster}
	 */
	public void removeObserver(Observer<StringModelEvent> observer) {
		multiCaster.removeObserver(observer);

	}

	@Override
	public void remove() {
		super.remove();
		multiCaster.notify(new StringModelEvent(
				StringModelEvent.EventType.REMOVED, string));
	}

	/**
	 * Set the string and notify observers of the change
	 * 
	 * @param string
	 */
	public void setString(String string) {
		this.string = string;
		multiCaster.notify(new StringModelEvent(
				StringModelEvent.EventType.ADDED, string));
	}

	public String getString() {
		return string;
	}

	public List<Observer<StringModelEvent>> getObservers() {
		return multiCaster.getObservers();
	}
}
