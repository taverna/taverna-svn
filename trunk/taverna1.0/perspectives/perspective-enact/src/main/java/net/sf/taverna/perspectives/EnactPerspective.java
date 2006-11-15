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
 * Filename           $RCSfile: EnactPerspective.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-11-15 12:44:53 $
 *               by   $Author: sowen70 $
 * Created on 8 Nov 2006
 *****************************************************************/
package net.sf.taverna.perspectives;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.shared.ModelMap.ModelChangeListener;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelInvokeSPI;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.scuflui.workbench.Workbench;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Perspective for enacting a workflow and keeping the results
 * 
 * @author Stuart Owens
 *
 */
public class EnactPerspective extends AbstractPerspective {	
	
	static {
		ModelMap.getInstance().addModelListener(new InvokeWorkflowListener());
	}
	
	public ImageIcon getButtonIcon() {
		return TavernaIcons.runIcon;
	}
	

	public String getText() {
		return "Run";
	}


	@Override
	protected InputStream getLayoutResourceStream() {
		return EnactPerspective.class.getResourceAsStream("/perspective-enact.xml");		
	}
		
}

/**
 * When ModelMap.INVOKE_WORKFLOW is set, activate this EnactPerspective 
 * and attachToModel() on all WorkflowModelInvokeSPI instances.
 * 
 * @author Stian Soiland
 *
 */
class InvokeWorkflowListener implements ModelChangeListener {

	private static Logger logger = Logger.getLogger(InvokeWorkflowListener.class);
	
	private ModelMap modelmap = ModelMap.getInstance();
	
	public boolean canHandle(String modelName, Object model) {
		if (! modelName.equals(ModelMap.INVOKE_WORKFLOW)) {
			return false;
		}
		if (! (model instanceof ScuflModel)) {
			logger.error(ModelMap.INVOKE_WORKFLOW + 
					" is not an ScuflModel instance");
			return false;
		}
		return true;
	}
	
	void setWorkflow(ScuflModel workflow) {
		// important to change the perspective *before* we attach
		modelmap.setModel(ModelMap.CURRENT_PERSPECTIVE, 
				new EnactPerspective());
		for (WorkflowModelViewSPI view : Workbench.getInstance().getWorkflowViews()) {
			if (! (view instanceof WorkflowModelInvokeSPI)) {
				continue;
			}
			view.detachFromModel();
			if (workflow != null) {
				view.attachToModel(workflow);
			}
		}
		logger.info("Changed to invoke perspective");
	}
	
	public void modelCreated(String modelName, Object model) {
		setWorkflow((ScuflModel)model);
	}

	public void modelChanged(String modelName, Object oldModel, Object newModel) {
		setWorkflow((ScuflModel)newModel);
	}

	public void modelDestroyed(String modelName, Object oldModel) {
		ScuflModel workflow = (ScuflModel) modelmap.getNamedModel(ModelMap.CURRENT_WORKFLOW);
		setWorkflow(workflow);
	}	
}
