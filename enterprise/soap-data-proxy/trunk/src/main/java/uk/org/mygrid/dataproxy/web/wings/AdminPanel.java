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
 * Filename           $RCSfile: AdminPanel.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-16 16:37:15 $
 *               by   $Author: sowen70 $
 * Created on 5 Apr 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.wings.SBorderLayout;
import org.wings.SBoxLayout;
import org.wings.SButton;
import org.wings.SConstants;
import org.wings.SDimension;
import org.wings.SForm;
import org.wings.SImageIcon;
import org.wings.SLabel;
import org.wings.STextField;
import org.wings.SToolBar;
import org.wings.border.STitledBorder;
import org.wings.session.SessionManager;

import uk.org.mygrid.dataproxy.configuration.ProxyConfig;
import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;

@SuppressWarnings("serial")
public class AdminPanel extends CentrePanel {

	private static Logger logger = Logger.getLogger(AdminPanel.class);
	
	private STextField contextTextField = new STextField();
	private STextField dataPathTextField = new STextField();
	private ProxyConfig config = ProxyConfigFactory.getInstance();
	
	public AdminPanel() {
		setLayout(new SBorderLayout());
		SForm form = new SForm(new SBoxLayout(SBoxLayout.VERTICAL));		
		
		contextTextField.setText(defaultContextPath());
		dataPathTextField.setText(config.getStoreBaseURL().toExternalForm().substring(5));			
		
		contextTextField.setPreferredSize(SDimension.FULLWIDTH);
		dataPathTextField.setPreferredSize(SDimension.FULLWIDTH);
		
		form.add(new SLabel("Server Context"));
		form.add(contextTextField);
		form.add(new SLabel(" "));
		form.add(new SLabel("Data Storage Location"));
		form.add(dataPathTextField);
		
		form.add(new SLabel());
		
		SButton okButton = new SButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				saveData();
			}			
		});
		okButton.setHorizontalAlignment(SConstants.RIGHT_ALIGN);
		form.add(okButton);
		
		if (ProxyConfigFactory.getInstance().getContextPath().length()==0) {
			SLabel label = new SLabel("Some basic settings need configuring before using the data proxy...");
			label.setPreferredSize(SDimension.FULLWIDTH);
			label.setHorizontalAlignment(SConstants.CENTER_ALIGN);
			add(label,SBorderLayout.NORTH);
		}
		else {
			SToolBar toolbar = new SToolBar();
			toolbar.setHorizontalAlignment(SConstants.LEFT);
			SButton backButton = new SButton(new SImageIcon(Icons.getIcon("back")));
			backButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					switchPanel(new WSDLListPanel());
				}				
			});
			toolbar.add(backButton);
			add(toolbar,SBorderLayout.NORTH);
		}
		
		form.setPreferredSize(new SDimension("450px","100%"));
		form.setBorder(new STitledBorder("Server configuration"));
		form.setHorizontalAlignment(SConstants.CENTER_ALIGN);
		form.setVerticalAlignment(SConstants.TOP_ALIGN);
		add(form,SBorderLayout.CENTER);
		
	}
	
	private String defaultContextPath() {
		if (config.getContextPath().length()==0) {
			HttpServletRequest request = SessionManager.getSession().getServletRequest();
			String host=request.getServerName();
			int port=request.getServerPort();
			String path=request.getContextPath();
			String scheme=request.getScheme();				
			
			String portStr="";
			if (port!=80) portStr=":"+port;
			
			String fullpath=scheme+ "://" +host+portStr+path;	
			if (!fullpath.endsWith("/")) fullpath+="/";
		
			return fullpath;
		}
		else {
			return config.getContextPath();
		}
	}
	
	private void saveData() {
		try {
			URL storeURL = new URL("file:"+dataPathTextField.getText());			
			config.setStoreBaseURL(storeURL);
			config.setContextPath(contextTextField.getText());
			ProxyConfigFactory.writeConfig();
			switchPanel(new WSDLListPanel());
		}
		catch(Exception e) {
			logger.error("Error setting admin data");
			reportError("An error occurred setting the data: "+e.getMessage());
		}		
	}
}
