/*******************************************************************************
 * Copyright (C) 2009 Ingo Wassink of University of Twente, Netherlands and
 * The University of Manchester   
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

/**
 * @author Ingo Wassink
 * @author Ian Dunlop
 * @author Alan R Williams
 * @author Stuart Owen
 */
package net.sf.taverna.t2.activities.rshell;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * Class for rshell connections
 * 
 */
public class RshellConnection extends RConnection {
	private boolean keepSessionAlive;

	/**
	 * Constructor of the Rshell Connection
	 * 
	 * @param connectionSettings
	 *            the connection settings for the connection
	 * @throws RserveException 
	 * @throws RSrvException
	 */
	public RshellConnection(RshellConnectionSettings connectionSettings) throws RserveException {
		super(connectionSettings.getHost(), connectionSettings.getPort());

		this.keepSessionAlive = connectionSettings.isKeepSessionAlive();

		String username = connectionSettings.getUsername();
		if (!username.equals("")) {
			this.login(username, connectionSettings.getPassword());
		}
	}

	/**
	 * Method for getting the keep session alive value
	 * 
	 * @return session alive value
	 */
	public boolean isKeepSessionAlive() {
		return keepSessionAlive;
	}
}
