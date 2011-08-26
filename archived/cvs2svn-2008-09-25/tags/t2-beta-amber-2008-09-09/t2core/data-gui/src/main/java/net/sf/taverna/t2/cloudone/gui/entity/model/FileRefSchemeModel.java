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

import java.io.File;
import java.util.List;

import net.sf.taverna.t2.cloudone.gui.entity.view.FileRefSchemeView;
import net.sf.taverna.t2.cloudone.refscheme.file.FileReferenceScheme;
import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observer;

/**
*  Model (in MVC terms) for the {@link FileReferenceScheme} being added or
 * removed from a {@link FileRefSchemeView}. Interested parties can register
 * with it (delegated to the {@link MultiCaster}) to receive notifications when
 * this model changes
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class FileRefSchemeModel extends ReferenceSchemeModel<File> {
	/*
	 * Proxy for sending notifications about events
	 */
	private MultiCaster<File> multiCaster = new MultiCaster<File>(this);
	private File file = null;
	/*
	 * The parent DataDocumentModel
	 */
	private final DataDocumentModel parentModel;

	public FileRefSchemeModel(DataDocumentModel parentModel) {
		this.parentModel = parentModel;
	}

	/**
	 * If you want to be notified about events happening to this
	 * {@link FileRefSchemeModel} then register. Uses the {@link MultiCaster}
	 */
	public void addObserver(Observer<File> observer) {
		multiCaster.addObserver(observer);
	}

	/**
	 * If you no longer want to be notified about events then remove yourself.
	 * Uses the {@link MultiCaster}
	 */
	public void removeObserver(Observer<File> observer) {
		multiCaster.removeObserver(observer);
	}

	/**
	 * Remove this {@link FileRefSchemeModel} from the model and inform the
	 * parent model that it has happened
	 */
	@Override
	public void remove() {
		parentModel.removeReferenceScheme(this);
	}

	/**
	 * The {@link File} represented by this {@link FileRefSchemeModel}
	 * 
	 * @return
	 */
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
		multiCaster.notify(file);
	}

	@Override
	public String getStringRepresentation() {
		if (file == null) {
			return "(none)";
		}
		return file.toString();
	}

	public List<Observer<File>> getObservers() {
		return multiCaster.getObservers();
	}

}
