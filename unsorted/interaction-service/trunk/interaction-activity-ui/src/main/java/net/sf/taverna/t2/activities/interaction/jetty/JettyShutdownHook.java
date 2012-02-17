/**
 * 
 */
package net.sf.taverna.t2.activities.interaction.jetty;

import net.sf.taverna.t2.workbench.ShutdownSPI;

/**
 * @author alanrw
 *
 */
public class JettyShutdownHook implements ShutdownSPI {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ShutdownSPI#positionHint()
	 */
	@Override
	public int positionHint() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ShutdownSPI#shutdown()
	 */
	@Override
	public boolean shutdown() {
		try {
			JettyStartupHook.getServer().stop();
		} catch (Exception e) {
			return true;
		}
		return true;
	}

}
