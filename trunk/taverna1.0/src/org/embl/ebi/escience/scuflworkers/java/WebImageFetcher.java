/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Fetch a single image from URL
 * 
 * @author Tom Oinn
 */
public class WebImageFetcher implements LocalWorker {

	private Logger logger = Logger.getLogger(WebImageFetcher.class);

	public String[] inputNames() {
		return new String[] { "url", "base" };
	}

	public String[] inputTypes() {
		return new String[] { "'text/x-taverna-web-url'", "'text/x-taverna-web-url'" };
	}

	public String[] outputNames() {
		return new String[] { "image" };
	}

	public String[] outputTypes() {
		return new String[] { "'image/*'" };
	}

	/**
	 * Fetch the web page pointed to by the URL supplied as the 'url' parameter
	 * into the service, the 'base' parameter specifies a URL to use as the base
	 * for relative URL resolution.
	 */
	public Map execute(Map inputs) throws TaskExecutionException {
		BufferedReader reader = null;
		try {
			URL inputURL = null;
			String inputURLString = (String) ((DataThing) inputs.get("url")).getDataObject();
			// Was a base URL supplied?
			if (inputs.get("base") != null) {
				inputURL = new URL(new URL((String) ((DataThing) inputs.get("base")).getDataObject()), inputURLString);
			} else {
				inputURL = new URL(inputURLString);
			}
			logger.info("Content length is " + inputURL.openConnection().getContentLength());
			byte[] contents;
			if (inputURL.openConnection().getContentLength() == -1) {
				// Content size unknown, must read first...
				byte[] buffer = new byte[1024];
				int bytesRead = 0;
				int totalBytesRead = 0;
				InputStream is = inputURL.openStream();
				while (bytesRead != -1) {
					totalBytesRead += bytesRead;
					bytesRead = is.read(buffer, 0, 1024);
				}
				contents = new byte[totalBytesRead];
			} else {
				contents = new byte[inputURL.openConnection().getContentLength()];
			}
			int bytesRead = 0;
			int totalBytesRead = 0;
			InputStream is = inputURL.openStream();
			while (bytesRead != -1) {
				bytesRead = is.read(contents, totalBytesRead, contents.length - totalBytesRead);
				totalBytesRead += bytesRead;
			}
			logger.info("Read " + totalBytesRead + " from input stream");
			Map outputMap = new HashMap();
			outputMap.put("image", new DataThing(contents));
			return outputMap;
		} catch (IOException ioe) {
			TaskExecutionException tee = new TaskExecutionException("Error fetching web image!");
			tee.initCause(ioe);
			throw tee;
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException ex) {
			}
		}
	}

}
