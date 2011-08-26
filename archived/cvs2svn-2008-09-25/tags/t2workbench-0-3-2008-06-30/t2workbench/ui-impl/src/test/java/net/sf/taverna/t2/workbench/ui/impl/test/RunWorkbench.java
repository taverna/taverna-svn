package net.sf.taverna.t2.workbench.ui.impl.test;

import java.io.IOException;

import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.spi.ProfileFactory;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;

public class RunWorkbench {
	
	public static void main(String[] args) throws IOException {
		System.setProperty("taverna.dotlocation", "/Applications/Taverna-1.7.1.app/Contents/MacOS/dot");
		ProfileFactory.getInstance().getProfile().addArtifact(new BasicArtifact("net.sf.taverna.t2.workbench","contextual-views-api","0.1-SNAPSHOT"));
		ProfileFactory.getInstance().getProfile().addArtifact(new BasicArtifact("net.sf.taverna.t2.workbench.views","graph","0.0.1-SNAPSHOT"));
		ProfileFactory.getInstance().getProfile().addArtifact(new BasicArtifact("net.sf.taverna.t2.workbench","activity-palette-ui","0.1-SNAPSHOT"));
		ProfileFactory.getInstance().getProfile().addArtifact(new BasicArtifact("net.sf.taverna.t2.workbench","run-ui","0.1-SNAPSHOT"));
		Workbench.main(args);		
	}
	
}
