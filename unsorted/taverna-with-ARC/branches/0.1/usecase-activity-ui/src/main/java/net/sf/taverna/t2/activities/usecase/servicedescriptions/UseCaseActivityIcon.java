/*******************************************************************************
 * Copyright (C) 2009 Hajo Nils Krabbenh�ft, INB, University of Luebeck   
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


package net.sf.taverna.t2.activities.usecase.servicedescriptions;

import javax.swing.Icon;

import net.sf.taverna.t2.activities.usecase.KnowARCConfigurationFactory;
import net.sf.taverna.t2.activities.usecase.UseCaseActivity;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconSPI;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * This class provides an icon for the use case activity.
 * 
 * @author Hajo Nils Krabbenh�ft
 */
public class UseCaseActivityIcon implements ActivityIconSPI{
	
	public int canProvideIconScore(Activity<?> activity) {
		if (activity.getClass().getName().equals(UseCaseActivity.class.getName()))
			return DEFAULT_ICON + 1;
		else
			return NO_ICON;
	}

	public Icon getIcon(Activity<?> activity) {
		return KnowARCConfigurationFactory.getConfiguration().getIcon();
	}
}



