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
 * Filename           $RCSfile: CentrePanel.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-15 18:00:54 $
 *               by   $Author: sowen70 $
 * Created on 22 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import org.wings.SBorderLayout;
import org.wings.SConstants;
import org.wings.SDimension;
import org.wings.SPanel;

@SuppressWarnings("serial")
public class CentrePanel extends SPanel{
	
	protected StatusPanel statusPanel;
	
	public CentrePanel(StatusPanel statusPanel) {
		this.statusPanel = statusPanel;
	}
	
	public CentrePanel() {
		this.statusPanel=null;
	}
	
	protected void setStatusPanel(StatusPanel statusPanel) {
		this.statusPanel = statusPanel;
	}

	protected void switchPanel(CentrePanel panel) {
		panel.setStatusPanel(statusPanel);
		statusPanel.reportStatus(""); //reset any previous message
		panel.setPreferredSize(SDimension.FULLAREA);
		panel.setHorizontalAlignment(SConstants.CENTER_ALIGN);
		getParent().add(panel,SBorderLayout.CENTER);
	}
	
	protected void reportError(String errorMsg) {
		if (statusPanel!=null) statusPanel.reportError(errorMsg);
	}
	
	protected void reportStatus(String statusMsg) {
		if (statusPanel!=null) statusPanel.reportStatus(statusMsg);
	}
}
