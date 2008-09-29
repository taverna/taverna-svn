/**
 * CVS
 * $Author: sowen70 $
 * $Date: 2006-07-20 14:51:32 $
 * $Revision: 1.1 $
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.rshell;

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
