/*
 * Copyright 2005 University of Manchester
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

package net.sf.taverna.ocula.action;

import java.util.Map;

import net.sf.taverna.ocula.Ocula;

/**
 * Simple class that implements Processor and stores the values retrieved
 * and allows others to get these values.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 *
 */
public class ProcessInput implements Processor {
    String userName;
    String email;
    
    public void process(Map map, Ocula ocula) throws Exception {
	userName = (String) map.get("userNameField");
	email = (String) map.get("emailField");
    }

    public synchronized String getEmail() {
        return email;
    }

    public synchronized String getUserName() {
        return userName;
    }

}
