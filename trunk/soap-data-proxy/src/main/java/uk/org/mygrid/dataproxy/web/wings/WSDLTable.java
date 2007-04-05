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
 * Filename           $RCSfile: WSDLTable.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-05 13:34:12 $
 *               by   $Author: sowen70 $
 * Created on 23 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import java.util.List;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.wings.STable;
import org.wings.event.SMouseEvent;
import org.wings.event.SMouseListener;

import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;

public class WSDLTable extends STable{

	private List<WSDLConfig> wsdlList;	
	
	public WSDLTable() {
		wsdlList=ProxyConfigFactory.getInstance().getWSDLConfigs();
		setModel(new WSDLTableModel(wsdlList));				
	}	
	
	public WSDLConfig getWSDLConfigForIndex(int index) {
		return wsdlList.get(index);
	}
	
	public void update() {
		this.wsdlList=ProxyConfigFactory.getInstance().getWSDLConfigs();
		WSDLTableModel model = (WSDLTableModel)getModel();
		model.update(wsdlList);
	}
}
