package org.embl.ebi.escience.scuflui.workbench;

import java.net.URL;

public abstract class URLBasedScavenger extends Scavenger {

	public URLBasedScavenger(Object userObject) {
		super(userObject);
	}
	
	public Scavenger fromURL(URL theURL) throws ScavengerCreationException {
		return null;
	}

}
