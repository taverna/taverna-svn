/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copies an InputStream to an OutputStream
 * 
 * @author Tom Oinn
 */
public class StreamCopier extends Thread {

	InputStream is;

	OutputStream os;

	/**
	 * Create a new StreamCopier which will, when started, copy the specified
	 * InputStream to the specified OutputStream
	 */
	public StreamCopier(InputStream is, OutputStream os) {
		this.is = is;
		this.os = os;
	}

	/**
	 * Start copying the stream, exits when the InputStream runs out of data
	 */
	public void run() {
		try {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.flush();
			os.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
