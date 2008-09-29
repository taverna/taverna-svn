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
 * Filename           $RCSfile: WSDLTreeCellRenderer.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-15 15:17:34 $
 *               by   $Author: sowen70 $
 * Created on 4 Apr 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.web.wings;

import javax.swing.ImageIcon;

import org.dom4j.Element;
import org.wings.SComponent;
import org.wings.SImageIcon;
import org.wings.SLabel;
import org.wings.SPanel;
import org.wings.STree;
import org.wings.tree.STreeCellRenderer;

public class WSDLTreeCellRenderer implements STreeCellRenderer{
	
	public SComponent getTreeCellRendererComponent(STree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		String text="";
		ImageIcon icon=null;
		if (value instanceof OperationNode) {
			OperationNode node = (OperationNode)value;			
			Element el = (Element)node.getUserObject();
			if (el.getName().equals("operation")) {
				text=el.elementTextTrim("name");
				icon=Icons.getIcon("tree-op");
			}			
		}
		else if (value instanceof TypeNode) {
			TypeNode node = (TypeNode)value;
			Element el = (Element)node.getUserObject();
			String name=el.attributeValue("name");
			if (name==null) name="";
			String type=el.getName();
			text=name+":"+type;
			if (node.isSelected()) icon=Icons.getIcon("selected");
		}
		
		SPanel result = new SPanel();
		if (icon!=null)
			result.add(new SLabel(new SImageIcon(icon)));		
		result.add(new SLabel(text));	
		return result;
	}
	
}
