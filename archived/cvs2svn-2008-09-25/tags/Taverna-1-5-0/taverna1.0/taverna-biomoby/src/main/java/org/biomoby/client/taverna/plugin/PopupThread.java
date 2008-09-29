package org.biomoby.client.taverna.plugin;

import java.awt.Component;

public class PopupThread extends Thread {

	Object object = null;

	BiomobyObjectProcessor objectProcessor = null;

	BiomobyObjectAction objectAction = null;

	boolean done = false;

	PopupThread(BiomobyObjectProcessor bop, BiomobyObjectAction boa) {
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
