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
 * Filename           $RCSfile: WorkbenchZBasePane.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-12-06 15:50:17 $
 *               by   $Author: sowen70 $
 * Created on 10 Nov 2006
 *****************************************************************/
package org.embl.ebi.escience.scuflui.workbench;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

import net.sf.taverna.zaria.ZBasePane;

/**
 * The default ZBasePane used within the taverna Workbench
 * @author sowen
 *
 */
@SuppressWarnings("serial")
public class WorkbenchZBasePane extends ZBasePane{
	
	public JMenuItem getMenuItem(Class theClass) {
		try {
			UIComponentFactorySPI factory = (UIComponentFactorySPI) theClass
					.newInstance();
			Icon icon = factory.getIcon();
			if (icon != null) {
				return new JMenuItem(factory.getName(), factory
						.getIcon());
			} else {
				return new JMenuItem(factory.getName());
			}
		} catch (InstantiationException e) {
			return new JMenuItem("Instantiation exception!");
		} catch (IllegalAccessException e) {
			return new JMenuItem("Illegal access exception!");
		}
	}

	@Override
	public JComponent getComponent(Class theClass) {
		UIComponentFactorySPI factory;
		try {
			factory = (UIComponentFactorySPI) theClass.newInstance();
			return (JComponent) factory.getComponent();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return new JPanel();
	}

	@Override
	protected void registerComponent(JComponent comp) {
		if (comp instanceof WorkflowModelViewSPI) {
			ScuflModel model = (ScuflModel) ModelMap.getInstance()
					.getNamedModel(ModelMap.CURRENT_WORKFLOW);
			if (model != null) {
				((WorkflowModelViewSPI) comp).attachToModel(model);
			}
		}
	}

	@Override
	protected void deregisterComponent(JComponent comp) {
		if (comp instanceof WorkflowModelViewSPI) {
			((WorkflowModelViewSPI) comp).detachFromModel();
		}
	}

	public void discard() {		
		
	}
	
	

}
