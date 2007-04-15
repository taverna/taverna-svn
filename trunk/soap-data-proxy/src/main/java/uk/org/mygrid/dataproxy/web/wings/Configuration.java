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
 * Revision           $Revision: 1.7 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-15 18:00:54 $
 *               by   $Author: sowen70 $
 * Created on 22 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import org.wings.SBorderLayout;
import org.wings.SBoxLayout;
import org.wings.SConstants;
import org.wings.SDimension;
import org.wings.SFrame;
import org.wings.SIcon;
import org.wings.SLabel;
import org.wings.SPanel;
import org.wings.SResourceIcon;
import org.wings.header.StyleSheetHeader;

import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;

public class Configuration {
	
	public Configuration() {			
		SBorderLayout layout = new SBorderLayout();
		layout.setPreferredSize(SDimension.FULLAREA);
		SPanel panel = new SPanel();				
		panel.setLayout(layout);
		panel.setPreferredSize(SDimension.FULLAREA);		
		
		setUpLogoPanel(panel);		
		
		CentrePanel centrePanel;
		centrePanel = startCentrePanel();
		centrePanel.setPreferredSize(SDimension.FULLAREA);
		centrePanel.setVerticalAlignment(SConstants.TOP_ALIGN);
		centrePanel.setHorizontalAlignment(SConstants.CENTER_ALIGN);
		
		StatusPanel statusPanel = new StatusPanel();
		centrePanel.setStatusPanel(statusPanel);
		
		panel.add(centrePanel,SBorderLayout.CENTER);		
				
		panel.add(statusPanel,SBorderLayout.SOUTH);			
		
		SFrame rootFrame = new SFrame("Webservice Data Proxy");		
		StyleSheetHeader style = new StyleSheetHeader("../css/style.css");
		rootFrame.addHeader(style);
        rootFrame.getContentPane().add(panel);
        rootFrame.getContentPane().setPreferredSize(SDimension.FULLAREA);        
        rootFrame.show();
	}

	private void setUpLogoPanel(SPanel panel) {
		SPanel northPanel = new SPanel(new SBoxLayout(SBoxLayout.HORIZONTAL));
		northPanel.setHorizontalAlignment(SConstants.LEFT_ALIGN);
		northPanel.setPreferredSize(SDimension.FULLWIDTH);
		
		SLabel title = new SLabel("Webservice Data Proxy");
		title.setName("logotitle");
		title.setPreferredSize(SDimension.FULLWIDTH);
		
		SIcon mygridImage = new SResourceIcon("/mygridLogo.gif");
		SLabel imageLabel = new SLabel(mygridImage);
		imageLabel.setName("mygridlogo");
		imageLabel.setPreferredSize(new SDimension("30%",null));		
		
		northPanel.add(imageLabel);
		northPanel.add(title);
		panel.add(northPanel,SBorderLayout.NORTH);
	}

	private CentrePanel startCentrePanel() {
		CentrePanel centrePanel;
		if (ProxyConfigFactory.getInstance().getContextPath().length()==0) {
		 	centrePanel= new AdminPanel();
		}
		else {
			centrePanel = new WSDLListPanel();
		}
		return centrePanel;
	}	
 }
