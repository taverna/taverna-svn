/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.activities.sadi.servicedescriptions;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.sadi.SADIActivity;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconSPI;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * 
 *
 * @author David Withers
 */
public class SADIActivityIcon implements ActivityIconSPI{

	public static final String COLOUR_HTML = "#a3c88d";
	public static final Color COLOUR = Color.decode(COLOUR_HTML);

	static {
		ColourManager.getInstance().setPreferredColour(
				"net.sf.taverna.t2.activities.sadi.SADIActivity", COLOUR);
	}
	
	static Icon icon = null;

	public int canProvideIconScore(Activity<?> activity) {
		if (activity.getClass().getName().equals(SADIActivity.class.getName()))
			return DEFAULT_ICON + 1;
		else
			return NO_ICON;
	}

	public Icon getIcon(Activity<?> activity) {
		return getSADIIcon();
	}
	
	public static Icon getSADIIcon() {
		if (icon == null) {
			icon = new ImageIcon(SADIActivityIcon.class.getResource("/sadi-logo16x16.png"));
		}
		return icon;
	}
	
}
