/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package net.sf.taverna.t2.lang.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Devours an input stream and allows the contents to be read as a String once
 * the stream has completed.
 * 
 * @author Tom Oinn
 */
public class StreamDevourer extends Thread {

	BufferedReader br;

	ByteArrayOutputStream output;

	/**
	 * Returns the current value of the internal ByteArrayOutputStream
	 */
	public String toString() {
		return output.toString();
	}

	/**
	 * Waits for the stream to close then returns the String representation of
	 * its contents (this is equivalent to doing a join then calling toString)
	 */
	public String blockOnOutput() {
		try {
			this.join();
			return output.toString();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
			return "Interrupted!";
		}
	}

	/**
	 * Create the StreamDevourer and point it at an InputStream to consume
	 */
	public StreamDevourer(InputStream is) {
		super("StreamDevourer");
		this.br = new BufferedReader(new InputStreamReader(is));
		this.output = new ByteArrayOutputStream();
	}

	/**
	 * When started this Thread will copy all data from the InputStream into a
	 * ByteArrayOutputStream via a BufferedReader. Because of the use of the
	 * BufferedReader this is only really appropriate for streams of textual
	 * data
	 */
	public void run() {
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				// && line.endsWith("</svg>") == false) {
				output.write(line.getBytes());
			}
			br.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
