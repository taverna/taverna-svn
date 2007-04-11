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
 * Filename           $RCSfile: WSDLListPanel.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-11 16:43:14 $
 *               by   $Author: sowen70 $
 * Created on 22 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.wings.SBoxLayout;
import org.wings.SButton;
import org.wings.SConstants;
import org.wings.SDimension;
import org.wings.SGridLayout;
import org.wings.SImageIcon;
import org.wings.SOptionPane;
import org.wings.SSpacer;
import org.wings.STable;
import org.wings.SToolBar;

import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;


@SuppressWarnings("serial")
public class WSDLListPanel extends CentrePanel{
	
	private static Logger logger = Logger.getLogger(WSDLListPanel.class);
	
	private WSDLTable table;
	private SButton deleteButton;	
	private SButton configureButton;	
	
	public WSDLListPanel() {				
				
		setLayout(new SBoxLayout(SBoxLayout.VERTICAL));			
				
		createConfigureButton();		
		createDeleteButton();				
		disableButtons();
		createWSDLTable();
					
		SToolBar toolBar = new SToolBar();		
		toolBar.add(configureButton);
		toolBar.add(new SSpacer(5,10));
		toolBar.add(deleteButton);
		toolBar.setHorizontalAlignment(SConstants.LEFT_ALIGN);
		add(toolBar);
		
		add(table);			
		
		add(new AddWSDLPanel(table));

		SButton editPaths = new SButton("Edit Paths");
		editPaths.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchPanel(new AdminPanel());				
			}			
		});
		editPaths.setShowAsFormComponent(true);
		add(editPaths);
				
	}

	private void createWSDLTable() {
		table = new WSDLTable();
		table.setSelectionMode(STable.SINGLE_SELECTION);	
		table.setShowHorizontalLines(true);
		table.setPreferredSize(new SDimension("95%","100%"));
		
		table.addSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (table.getSelectedRow()>-1) {
					enableButtons();
				}
				else {
					disableButtons();
				}
			}			
		});
		table.setSelectedRow(0);
	}

	private void createDeleteButton() {
		deleteButton = new SButton(new SImageIcon(Icons.getIcon("delete")));
		deleteButton.setToolTipText("Delete WSDL");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final WSDLConfig config = getSelectedWSDLConfig();
				ActionListener optionListener = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand().equals(SOptionPane.YES_ACTION)) {
							deleteWSDL(config);
						}
					}					
				};
				SOptionPane.showYesNoDialog(table, "Are you sure you want to delete "+config.getName()+"?\nYou will lose any configurations you have made to this WSDL but stored data will remain.", "Delete WSDL?",optionListener);				
			}			
		});
	}

	private void createConfigureButton() {
		configureButton = new SButton(new SImageIcon(Icons.getIcon("configure")));
		configureButton.setToolTipText("Configure WSDL");
		
		configureButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				WSDLConfig config = getSelectedWSDLConfig();
				if (config!=null) {
					switchPanel(new WSDLConfigurationPanel(config));
				}
			}			
		});
	}
	
	private void deleteWSDL(WSDLConfig config) {
		logger.info("Deleting WSDL named: "+config.getName());
		ProxyConfigFactory.getInstance().deleteWSDLConfig(config);
		try {
			ProxyConfigFactory.writeConfig();
		} catch (Exception e) {
			logger.error("Error writing proxy config",e);
		}
		table.update();
	}
	
	private WSDLConfig getSelectedWSDLConfig() {
		WSDLConfig result = null;
		int index=table.getSelectedRow();
		if (index>-1) {
			result=table.getWSDLConfigForIndex(index);
			if (result==null) {				
				logger.error("Invalid index for WSDLConfig: "+index);
			}
		}
		else {
			logger.warn("Configure button was enabled when no row was selected");
		}
		return result;
	}	
	
	private void enableButtons() {		
		deleteButton.setEnabled(true);
		configureButton.setEnabled(true);
	}
	
	private void disableButtons() {		
		deleteButton.setEnabled(false);
		configureButton.setEnabled(false);
	}	
}