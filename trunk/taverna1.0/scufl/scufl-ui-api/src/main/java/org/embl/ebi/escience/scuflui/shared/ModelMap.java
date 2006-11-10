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
 * Filename           $RCSfile: ModelMap.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-11-10 12:06:30 $
 *               by   $Author: sowen70 $
 * Created on 27 Oct 2006
 *****************************************************************/
package org.embl.ebi.escience.scuflui.shared;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Map of the models present in the workbench associated with their names, together with
 * the ability to manipulate this.
 * Contains, from version 1.5 onwards, methods to set and notify
 * components of changes to the underlying set of named models.
 * 
 * A 'model' can be any Object that has an effect on the UI
 * @author Stuart Owen
 * @author Tom Oinn
 *
 */
public class ModelMap {
	
	private static Logger logger = Logger.getLogger(ModelMap.class);
	
	private static ModelMap instance = new ModelMap();
	
	private ModelMap() {
		
	}
	
	public static ModelMap getInstance() {
		return instance;
	}
	
	/**
	 * At any given time there are zero or more named model objects over
	 * which the workbench UI is acting. 
	 */
	private static Map<String,Object> modelMap = 
		Collections.synchronizedMap(new HashMap<String,Object>());
	
	/**
	 * Used as a modelName for setModel() and getNamedModel() - notes
	 * the current active workflow in the GUI.
	 *  
	 */
	public final static String CURRENT_WORKFLOW = "currentWorkflow";
	
	/**
	 * Used as a modelName for setModel() and getNamedModel() - notes
	 * the current active perspective in the GUI.
	 */
	public final static String CURRENT_PERSPECTIVE = "currentPerspective";
	
	/**
	 * Set this to be notified of changes to the model map
	 */
	public static ModelChangeListener DEFAULT_MODEL_LISTENER = null;
	
	/**
	 * returns the model set
	 */
	public Set<Object> getModels() {
		Set<Object> models=new HashSet<Object>();
		models.addAll(modelMap.values());
		return models;
	}
	
	/**
	 * Manipulate the current model map
	 * @param modelName name of the model to act on
	 * @param model null to destroy the model or a reference to the
	 * new model to set. If it didn't already exist a modelCreated
	 * event will be fired otherwise modelChanged is called.
	 */
	public synchronized void setModel(String modelName, Object model) {
		logger.debug("SetModel called, modelName="+modelName+", model="+model);
		if (DEFAULT_MODEL_LISTENER != null) {
			if (modelMap.containsKey(modelName) == false) {
				if (model != null) {
					// Create new model object
					modelMap.put(modelName,model);
					DEFAULT_MODEL_LISTENER.modelCreated(modelName, model);
				}
			}
			else {
				if (model == null) {
					// Destroy model object
					Object oldModel = modelMap.get(modelName);
					modelMap.remove(modelMap.get(modelName));
					DEFAULT_MODEL_LISTENER.modelDestroyed(modelName,oldModel);
				}
				else {
					Object oldModel = modelMap.get(modelName);
					if (oldModel != model) {
						// Update model object
						modelMap.put(modelName, model);
						DEFAULT_MODEL_LISTENER.modelChanged(modelName, oldModel, model);
					}
				}
			}
		}
	}
	
	/**
	 * Register with the static class to inform workbench like
	 * systems that the underlying map of named model objects has been
	 * altered in some way.
	 * @author Tom Oinn
	 */
	public interface ModelChangeListener {

		/**
		 * Called when the named model is updated
		 * @param modelName name of the model that changed
		 * @param newModel new model object
		 * @param oldModel old model object it replaces
		 */
		public void modelChanged(String modelName, Object oldModel, Object newModel);

		/**
		 * Called when the named model is removed from the
		 * model map
		 * @param modelName
		 */
		public void modelDestroyed(String modelName, Object oldModel);

		/**
		 * Called when a new model is created or inserted into the
		 * model map under a previously absent key
		 * @param modelName name of the new model
		 * @param model the new model object
		 */
		public void modelCreated(String modelName, Object model);
	
	}
	
	public Object getNamedModel(String string) {
		return modelMap.get(string);
	}
	

}
