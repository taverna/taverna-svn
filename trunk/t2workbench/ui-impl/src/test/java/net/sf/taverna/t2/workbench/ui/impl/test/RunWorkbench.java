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
package net.sf.taverna.t2.workbench.ui.impl.test;

import java.io.IOException;

import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.spi.ProfileFactory;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;

public class RunWorkbench {
	
	public static void main(String[] args) throws IOException {
		System.setProperty("taverna.dotlocation", "/Applications/Taverna-1.7.1.app/Contents/MacOS/dot");
		ProfileFactory.getInstance().getProfile().addArtifact(new BasicArtifact("net.sf.taverna.t2.workbench","contextual-views-api","0.1-SNAPSHOT"));
		ProfileFactory.getInstance().getProfile().addArtifact(new BasicArtifact("net.sf.taverna.t2.workbench.views","graph","0.0.1-SNAPSHOT"));
		ProfileFactory.getInstance().getProfile().addArtifact(new BasicArtifact("net.sf.taverna.t2.workbench","activity-palette-ui","0.1-SNAPSHOT"));
		ProfileFactory.getInstance().getProfile().addArtifact(new BasicArtifact("net.sf.taverna.t2.workbench","run-ui","0.1-SNAPSHOT"));
		Workbench.main(args);		
	}
	
}
