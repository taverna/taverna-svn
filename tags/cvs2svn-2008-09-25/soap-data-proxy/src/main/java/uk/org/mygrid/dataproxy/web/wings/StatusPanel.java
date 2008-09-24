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
 * Filename           $RCSfile: StatusPanel.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-12 15:46:01 $
 *               by   $Author: sowen70 $
 * Created on 12 Apr 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import java.awt.Color;

import org.wings.SBoxLayout;
import org.wings.SConstants;
import org.wings.SDimension;
import org.wings.SLabel;
import org.wings.SPanel;
import org.wings.SProgressBar;

@SuppressWarnings("serial")
public class StatusPanel extends SPanel {

	private SLabel statusLabel = new SLabel(" ");	
	private SProgressBar progressBar = new SProgressBar();
	
	public StatusPanel() {
		setLayout(new SBoxLayout(SBoxLayout.HORIZONTAL));
		setPreferredSize(SDimension.FULLWIDTH);
		
		statusLabel.setHorizontalAlignment(SConstants.LEFT_ALIGN);
		
		statusLabel.setPreferredSize(new SDimension("70%","100%"));
		progressBar.setPreferredSize(new SDimension("30%","100%"));
		progressBar.setFilledColor(Color.BLUE);		
		
		
		add(statusLabel);
		add(progressBar);				
		
		updateProgress(0);
	}
	
	public void reportError(String error) {
		if (error.length()==0) error=" ";
		statusLabel.setForeground(Color.RED);
		statusLabel.setText(error);
	}
	
	public void reportStatus(String status) {
		if (status.length()==0) status=" ";
		statusLabel.setForeground(Color.BLACK);
		statusLabel.setText(status);
	}
	
	public void updateProgress(int percent) {
		if (percent<=0) progressBar.setVisible(false);
		else progressBar.setVisible(true);
		progressBar.setValue(percent);		
	}
}
