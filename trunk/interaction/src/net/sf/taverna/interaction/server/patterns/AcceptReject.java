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

package net.sf.taverna.interaction.server.patterns;

import net.sf.taverna.interaction.server.ServerInteractionPattern;

/**
 * Simple pattern, user accepts or rejects a single item of data
 * @author Tom Oinn
 */
public class AcceptReject implements ServerInteractionPattern {

    public String getName() {
	return "base.AcceptReject";
    }
    public String[] getInputNames() {
	return new String[]{"data"};
    }
    public String[] getInputTypes() {
	return new String[]{"'text/plain'"};
    }
    public String getDescription() {
	return "Accept or reject a single item of data.";
    }
    public String[] getOutputNames() {
	return new String[]{"decision"};
    }
    public String[] getOutputTypes() {
	return new String[]{"'text/plain'"};
    }   

}
