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
 * Filename           $RCSfile: WSDLTableModel.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-05 13:34:12 $
 *               by   $Author: sowen70 $
 * Created on 23 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.wings.SAnchor;
import org.wings.SButton;
import org.wings.SImageIcon;
import org.wings.SLabel;
import org.wings.session.SessionManager;

import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.web.ServerInfo;


@SuppressWarnings("serial")
public class WSDLTableModel extends AbstractTableModel {

	private List<WSDLConfig> wsdlList;
	private Object[][]data;	
	
	public WSDLTableModel(List<WSDLConfig> wsdlList) {
		update(wsdlList);		
	}
	
	public void update(List<WSDLConfig> wsdlList) {
		this.wsdlList=wsdlList;
		setUpData();
		fireTableDataChanged();
	}

	public int getColumnCount() {
		return 3;
	}

	public int getRowCount() {
		return wsdlList.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return data[rowIndex][columnIndex];	
	}
	
	private void setUpData() {
		data=new Object[wsdlList.size()][getColumnCount()];
		int row=0;
		for (WSDLConfig config : wsdlList) {
			SLabel label = new SLabel(config.getName());
			data[row][0]=label;
			data[row][1]=config.getAddress();
			SAnchor anchor = new SAnchor("../viewwsdl?id="+config.getWSDLID());
			anchor.add(new SLabel(new SImageIcon(Icons.getIcon("configure"))));
			data[row][2]=anchor;
			
						
			row++;
		}
	}
	
}
