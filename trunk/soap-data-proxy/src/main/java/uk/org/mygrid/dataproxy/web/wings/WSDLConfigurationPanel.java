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
 * Filename           $RCSfile: WSDLConfigurationPanel.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-10 13:06:52 $
 *               by   $Author: sowen70 $
 * Created on 23 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import org.apache.log4j.Logger;
import org.wings.SButton;
import org.wings.SGridLayout;
import org.wings.SLabel;
import org.wings.STree;

import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.wsdl.SchemaParsingException;
import uk.org.mygrid.dataproxy.xml.ElementDefinition;

@SuppressWarnings("serial")
public class WSDLConfigurationPanel extends CentrePanel{
	
	private static Logger logger = Logger
			.getLogger(WSDLConfigurationPanel.class);
		
	private STree tree = new STree();
	private WSDLTreeModel model;
	private WSDLConfig config;

	public WSDLConfigurationPanel(WSDLConfig config) {
		this.config=config;
		setLayout(new SGridLayout(1));
		SButton backButton = new SButton("Back");		
		backButton.setShowAsFormComponent(true);		
		backButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				switchPanel(new WSDLListPanel());				
			}
			
		});
		
		SButton toggleButton = new SButton("Toggle");
		toggleButton.setShowAsFormComponent(true);
		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleSelectedTreeItem();				
			}			
		});
		
		SButton commitButton = new SButton("Commit");
		commitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				commit();				
			}			
		});
				
		model = new WSDLTreeModel(config);
		try {
			model.populate();			
		} catch (SchemaParsingException e1) {
			logger.error("Error parsing the wsdl:"+config.getAddress(),e1);
			setError("Error parsing the wsdl:"+e1.getMessage());
		}
		
		tree.setCellRenderer(new WSDLTreeCellRenderer());
		tree.setModel(model);		
		tree.setRootVisible(true);
		tree.getSelectionModel().setSelectionMode(STree.SINGLE_TREE_SELECTION);		
		
		add(new SLabel(config.getName()));
		add(new SLabel(config.getWSDLID()));
		add(tree);
		add(toggleButton);						
		add(backButton);
		add(commitButton);
	}	
	
	private void commit() {
		List<ElementDefinition> elementDefs = config.getElements();
		elementDefs.clear();
		elementDefs.addAll(model.getSelectedForProxy());
		try {
			ProxyConfigFactory.writeConfig();
		} catch (Exception e) {
			//TODO: report back error
			logger.error("Error writing config",e);
		}
	}
	
	private void toggleSelectedTreeItem() {
		Object selectedObj = tree.getLastSelectedPathComponent();		
		if (selectedObj instanceof TypeNode) {
			TypeNode node = (TypeNode)selectedObj;
			model.toggleNode(node);
			tree.collapseRow(0);
			tree.expandRow(0);
		}		
	}	
}
