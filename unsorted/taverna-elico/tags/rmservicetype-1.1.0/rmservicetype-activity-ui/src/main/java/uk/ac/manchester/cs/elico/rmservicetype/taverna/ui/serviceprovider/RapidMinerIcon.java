package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.serviceprovider;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivityConfigurationBean;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;

import net.sf.taverna.t2.workbench.activityicons.ActivityIconSPI;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Rishi Ramgolam<br>
 * Date: Jul 13, 2011<br>
 * The University of Manchester<br>
 **/

public class RapidMinerIcon implements ActivityIconSPI {

	private static Icon icon;
	
	private static final Color COLOUR = Color.decode("#E6E6FA");

	static {
		ColourManager.getInstance().setPreferredColour(
				"uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity", COLOUR);
	}
	
	public int canProvideIconScore(Activity<?> activity) {
		if (activity instanceof RapidMinerExampleActivity) {
			
	            return DEFAULT_ICON + 100;
	        
	    }
	    return NO_ICON;

	}

	public Icon getIcon(Activity<?> arg0) {
		if (icon == null) {
	        icon = new ImageIcon(RapidMinerIcon.class.getResource("/rapidMiner.png"));
	    }
	    return icon;
	    
	}
	public static Icon getIcon() {
		if (icon == null) {
	        icon = new ImageIcon(RapidMinerIcon.class.getResource("/rapidMiner.png"));
	    }
	    return icon;
	    
	}


}
