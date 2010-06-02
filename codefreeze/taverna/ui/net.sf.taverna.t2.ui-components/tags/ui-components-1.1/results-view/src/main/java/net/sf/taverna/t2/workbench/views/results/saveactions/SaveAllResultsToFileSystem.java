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
package net.sf.taverna.t2.workbench.views.results.saveactions;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.embl.ebi.escience.baclava.DataThing;
//import org.jdom.Namespace;

@SuppressWarnings("serial")
public class SaveAllResultsToFileSystem extends SaveAllResultsSPI {


	//private static Namespace namespace = Namespace.getNamespace("b","http://org.embl.ebi.escience/baclava/0.1alpha");

	public SaveAllResultsToFileSystem(){
		super();
		putValue(NAME, "Save as directory");
		putValue(SMALL_ICON, WorkbenchIcons.saveAllIcon);
	}
	
	public AbstractAction getAction() {
		return new SaveAllResultsToFileSystem();
	}
	
	
	/**
	 * Saves the result data as a file structure 
	 */
	protected void saveData(File file) throws Exception{
		

	    
		// Build the DataThing map from the resultReferencesMap
		// First convert map of references to objects into a map of real result objects
		Map<String, Object> resultMap = new HashMap<String, Object>();
		for (Iterator<String> i = chosenReferences.keySet().iterator(); i.hasNext();) {
			String portName = (String) i.next();
  			resultMap.put(portName, getObjectForName(portName));
  		}
		Map<String, DataThing> dataThings = bakeDataThingMap(resultMap);
		
		for (String portName : dataThings.keySet()) {
			DataThing thing = dataThings.get(portName);
			thing.writeToFileSystem(file, portName);
		}
	}

	@Override
	protected String getFilter() {
		return null;
	}
}
