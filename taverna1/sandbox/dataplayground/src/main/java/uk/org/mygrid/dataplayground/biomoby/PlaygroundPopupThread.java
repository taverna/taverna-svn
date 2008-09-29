package uk.org.mygrid.dataplayground.biomoby;



/*
 * Originally part of the Taverna BioMoby Plugin. 
 * 
 * EDITED FOR USE BY THE DATA PLAYGROUND
 * 
 * This file is a component of the Taverna project, and is licensed under the
 * GNU LGPL. Copyright Edward Kawas, The BioMoby Project
 */


import java.awt.Component;

import org.biomoby.client.taverna.plugin.BiomobyObjectProcessor;

public class PlaygroundPopupThread extends Thread {

	Object object = null;

	BiomobyObjectProcessor objectProcessor = null;

	PlaygroundBiomobyObjectAction objectAction = null;

	boolean done = false;

	public PlaygroundPopupThread(BiomobyObjectProcessor bop, PlaygroundBiomobyObjectAction boa) {
		this.objectAction = boa;
		this.objectProcessor = bop;
		setDaemon(true);
	}

	public void run() {
		object = objectAction.getComponent(objectProcessor);
		this.done = true;
	}

	// call after you check if done!
	public Component getComponent() {
		return (Component) object;
	}
}
