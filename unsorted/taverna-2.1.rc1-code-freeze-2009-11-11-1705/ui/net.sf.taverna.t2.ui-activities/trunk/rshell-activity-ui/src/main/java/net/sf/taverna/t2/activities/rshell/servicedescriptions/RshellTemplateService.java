/*******************************************************************************
 * Copyright (C) 2009 Ingo Wassink of University of Twente, Netherlands and
 * The University of Manchester   
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

/**
 * @author Ingo Wassink
 * @author Ian Dunlop
 * @author Alan R Williams
 */
package net.sf.taverna.t2.activities.rshell.servicedescriptions;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.rshell.RshellActivity;
import net.sf.taverna.t2.activities.rshell.RshellActivityConfigurationBean;
import net.sf.taverna.t2.servicedescriptions.AbstractTemplateService;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;

public class RshellTemplateService extends AbstractTemplateService<RshellActivityConfigurationBean>{

	private static final String RSHELL = "Rshell";
	
	@Override
	public Class<RshellActivity> getActivityClass() {
		return RshellActivity.class;
	}

	@Override
	public RshellActivityConfigurationBean getActivityConfiguration() {
		return new RshellActivityConfigurationBean();
	}

	@Override
	public Icon getIcon() {
		return RshellActivityIcon.getRshellIcon();
	}

	public String getName() {
		return RSHELL;
	}
	
	@Override
	public String getDescription() {
		return "A service that allows the calling of R scripts on an R server";	
	}
	
	public static ServiceDescription getServiceDescription() {
		RshellTemplateService rts = new RshellTemplateService();
		return rts.templateService;
	}
}
