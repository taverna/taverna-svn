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

package net.sf.taverna.ocula.renderer;

import javax.swing.JComponent;
import javax.swing.JLabel;
import net.sf.taverna.ocula.Ocula;

/**
 * Fallback case for when no other renderer matches, handles all
 * objects by calling the toString method and generating a JLabel
 * with the result.
 * @author Tom Oinn
 */
public class FallbackObjectRenderer implements RendererSPI {
    
    /**
     * Handles everything
     */
    public boolean canHandle(Object o, Ocula ocula) {
	return true;
    }

    /**
     * Return a new JLabel
     */
    public JComponent getRenderer(Object object, Ocula ocula) {
	return new JLabel(object.toString());
    }

}
