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
 * Filename           $RCSfile: ServiceTreePopupHandler.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-11-21 13:59:45 $
 *               by   $Author: sowen70 $
 * Created on 21 Nov 2006
 *****************************************************************/
package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.jdom.Element;

/**
 * Popup menu when right-clicking on a result node, giving the option to add to the current Scufl Model.
 * 
 * @author Stuart Owen
 */
public class ServiceTreePopupHandler extends MouseAdapter {
	
	private static Logger logger = Logger.getLogger(ServiceTreePopupHandler.class);
	
	ServiceTree theTree = null;
	
	public ServiceTreePopupHandler(ServiceTree theTree) {
		this.theTree=theTree;
	}
		
	/**
	 * Handle the mouse pressed event in case this is the platform specific
	 * trigger for a popup menu
	 */
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}

	/**
	 * Similarly handle the mouse released event
	 */
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}
	
	private void doEvent(MouseEvent e) {
		TreePath path=theTree.getPathForLocation(e.getX(), e.getY());
		if (path!=null) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) (path.getLastPathComponent());
			final ScuflModel model = TavernaFetaGUI.getInstance().getScuflModel();
			if (node.getUserObject() instanceof BasicServiceModel) {								
				JPopupMenu menu=new JPopupMenu("Query result.");
				JMenuItem item=new JMenuItem("Add to model");
				if (model==null) item.setEnabled(false);	
				
				ActionListener addToModelListener=new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Processor newProcessor;
						BasicServiceModel serviceModel = (BasicServiceModel) node.getUserObject();
						newProcessor=createProcessorFromServiceModel(model, serviceModel);
						if (newProcessor!=null)
							model.addProcessor(newProcessor);
					}

					private Processor createProcessorFromServiceModel(final ScuflModel model, BasicServiceModel serviceModel) {
						Processor newProcessor = null;
						Element wrapperElement = new Element("wrapper");						
						wrapperElement.addContent(serviceModel.getTavernaProcessorSpecAsElement().detach());
						String validName = serviceModel.getServiceName();
						validName=model.getValidProcessorName(validName);
						
						try {
							newProcessor = ProcessorHelper
							.loadProcessorFromXML(wrapperElement, model,
									validName);							
						} catch (ProcessorCreationException e1) {
							logger.error("Error creating processor",e1);
						} catch (DuplicateProcessorNameException e1) {
							logger.error("Duplicate processor name",e1);
						} catch (XScuflFormatException e1) {
							logger.error("Error with scufl xml for processor",e1);
						}
						return newProcessor;
					}					
				};
				item.addActionListener(addToModelListener);
				menu.add(item);
				menu.show(theTree,e.getX(), e.getY());
			}
		}
	}	
}
