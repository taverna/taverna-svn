/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.ncbi.query;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
/**
 * Represents a draggable NCBI service
 * @author Ian Dunlop
 *
 */
public class NCBIActivityItem extends AbstractActivityItem {
	
	private String url;
	private String category;
	private String operation;
	private String wsdlOperation;

	public Object getType() {
		return "Localworker";
	}

	@Override
	public Object getConfigBean() {
		WSDLActivityConfigurationBean bean = new WSDLActivityConfigurationBean();
		bean.setWsdl(this.url);
		bean.setOperation(this.wsdlOperation);
		return bean;
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(NCBIActivityItem.class
				.getResource("/wsdl.png"));
	}

	@Override
	public Activity<?> getUnconfiguredActivity() {
		return new WSDLActivity();
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCategory() {
		return category;
	}
	
	@Override
	public String toString() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getOperation() {
		return operation;
	}

	public String getWsdlOperation() {
		return wsdlOperation;
	}

	public void setWsdlOperation(String wsdlOperation) {
		this.wsdlOperation = wsdlOperation;
	}

}
