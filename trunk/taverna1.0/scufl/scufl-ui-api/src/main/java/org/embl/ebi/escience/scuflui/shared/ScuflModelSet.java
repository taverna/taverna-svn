/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: ScuflModelSet.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-10-27 15:43:25 $
 *               by   $Author: sowen70 $
 * Created on 27 Oct 2006
 *****************************************************************/
package org.embl.ebi.escience.scuflui.shared;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * A collection of open workflow models. A listener can be provided to make is possible to detect
 * the addition and removal of models.
 * 
 * @author Stuart Owen
 */

public class ScuflModelSet {
	private Set<ScuflModel> modelSet = Collections.synchronizedSet(new HashSet<ScuflModel>());
	private Set<ScuflModelSetListener> listeners=Collections.synchronizedSet(new HashSet<ScuflModelSetListener>());
	
	private static ScuflModelSet instance = new ScuflModelSet();
	
	private ScuflModelSet() {
		
	}
	
	public static ScuflModelSet instance() {
		return instance;
	}
	
	public boolean isEmpty() {
		return modelSet.isEmpty();
	}
	
	public int size() {
		return modelSet.size();
	}
	
	public Set<ScuflModel> getModels() {
		return modelSet;
	}
	
	public void addListener(ScuflModelSetListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(ScuflModelSetListener listener) {
		listeners.remove(listener);
	}

	public void addModel(ScuflModel model) {
		modelSet.add(model);
		for (ScuflModelSetListener l : listeners) {
			l.modelAdded(model);
		}
	}
	
	public void removeModel(ScuflModel model) {
		modelSet.remove(model);
		for (ScuflModelSetListener l : listeners) {
			l.modelRemoved(model);
		}
	}
	
	public interface ScuflModelSetListener {
		public void modelAdded(ScuflModel model);
		public void modelRemoved(ScuflModel model);
	}
}



