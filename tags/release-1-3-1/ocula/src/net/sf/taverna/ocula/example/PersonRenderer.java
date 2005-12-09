/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.ocula.example;

import net.sf.taverna.ocula.renderer.RendererSPI;
import net.sf.taverna.ocula.ui.Icons;
import net.sf.taverna.ocula.Ocula;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * Trivial renderer for the Person class used in the example
 * @author Tom Oinn
 */
public class PersonRenderer implements RendererSPI {

    public boolean canHandle(Object o, Ocula ocula) {
	return (o instanceof Person);
    }

    public JComponent getRenderer(Object object, Ocula ocula) {
	Person p = (Person)object;
	JLabel personLabel = new JLabel(Icons.getIcon("users"));
	personLabel.setText("<html><body><b>"+p.getName()+"<br>"+
			    p.getCountry()+"<br>"+p.getPhone()+
			    "</b></body></html>");
	return personLabel;
    }

}
