/**
 * 
 */
package net.sf.taverna.t2.activities.interaction.hook;

import net.sf.taverna.t2.activities.interaction.InteractionRecorder;
import net.sf.taverna.t2.activities.interaction.InteractionRunDeletionListener;
import net.sf.taverna.t2.workbench.ShutdownSPI;
import net.sf.taverna.t2.workbench.StartupSPI;

/**
 * @author alanrw
 *
 */
public class InteractionStartupHook implements StartupSPI {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ShutdownSPI#positionHint()
	 */
	@Override
	public int positionHint() {
		return 900;
	}

	@Override
	public boolean startup() {
		InteractionRecorder.load();
		return true;
	}

}
