/**
 * CVS
 * $Author: davidwithers $
 * $Date: 2008-03-19 15:48:10 $
 * $Revision: 1.1 $
 */
package net.sf.taverna.t2.activities.rshell;

import org.rosuda.JRclient.RSrvException;
import org.rosuda.JRclient.Rconnection;

/**
 * Class for rshell connections
 * 
 * @author Ingo Wassink
 * 
 */
public class RshellConnection extends Rconnection {
	private boolean keepSessionAlive;

	/**
	 * Constructor of the Rshell Connection
	 * 
	 * @param connectionSettings
	 *            the connection settings for the connection
	 * @throws RSrvException
	 */
	public RshellConnection(RshellConnectionSettings connectionSettings)
			throws RSrvException {
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
