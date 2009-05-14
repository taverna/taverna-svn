package net.sf.taverna.t2.activities.dataflow;

import net.sf.taverna.t2.platform.taverna.Enactor;

/**
 * Class for accessing platform components. The components are set when the
 * plugin is loaded by the platform.
 * <p>
 * This class must be listed in META_INF/platform.init
 * 
 * @author David Withers
 */
public class PlatformComponents {

	private static Enactor enactor;

	public static void setEnactor(Enactor theEnactor) {
		enactor = theEnactor;
	}

	public static Enactor getEnactor() {
		if (enactor == null) {
			throw new IllegalStateException("Dataflow plug-in not activated: Enactor not set");
		}
		return enactor;
	}

}
