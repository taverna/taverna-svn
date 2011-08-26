/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

// IO Imports
import java.io.PrintStream;

/**
 * Trivially implements the ScuflModelEventListener by printing a textual
 * representation of incoming events to a given PrintStream
 * 
 * @author Tom Oinn
 */
public class ScuflModelEventPrinter implements ScuflModelEventListener,
		java.io.Serializable {

	private PrintStream writer = null;

	/**
	 * The listener will print events out onto the supplied PrintWriter. If
	 * null, this defaults to the stdout.
	 */
	public ScuflModelEventPrinter(PrintStream the_writer) {
		if (the_writer == null) {
			this.writer = System.out;
		} else {
			this.writer = the_writer;
		}
	}

	/**
	 * Implements ScuflModelEventListener
	 */
	public void receiveModelEvent(ScuflModelEvent event) {
		String source = ((Object) (event.getSource())).toString();
		String message = event.getMessage();
		writer.println("Event from " + source + " : " + message);
	}

}
