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
 * Filename           $RCSfile: Configuration.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-05 13:34:16 $
 *               by   $Author: sowen70 $
 * Created on 22 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import javax.servlet.http.HttpServletRequest;

import org.wings.SBorderLayout;
import org.wings.SFrame;
import org.wings.SLabel;
import org.wings.SPanel;
import org.wings.session.SessionManager;

public class Configuration {
	public Configuration() {
		setServerInfo(SessionManager.getSession().getServletRequest());
		
		SBorderLayout layout = new SBorderLayout();
		SPanel panel = new SPanel();
		
		panel.setLayout(layout);
		SLabel title = new SLabel("Data Proxy");		
		panel.add(title,SBorderLayout.NORTH);		
		panel.add(new WSDLListPanel(),SBorderLayout.CENTER);
		
		SLabel status = new SLabel();
		panel.add(status,SBorderLayout.SOUTH);		
		
		SFrame rootFrame = new SFrame();
		rootFrame.setTitle("Data Proxy");
        rootFrame.getContentPane().add(panel);
        rootFrame.setVisible(true);
	}
	
	private void setServerInfo(HttpServletRequest request) {
		String host=request.getServerName();
		int port=request.getServerPort();
		String path=request.getContextPath();
		String scheme=request.getScheme();				
		
		String portStr="";
		if (port!=80) portStr=":"+port;
		
		String fullpath=scheme+"//"+host+portStr+path;		
	}
 }
