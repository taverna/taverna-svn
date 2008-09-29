/*
 * URLReader.java
 *
 * Created on 05 March 2006, 20:12
 */

package uk.ac.man.cs.img.fetaEngine.util;

import java.io.InputStream;

/**
 * 
 * @author Pinar
 */
public class URLReader {

	/** Creates a new instance of URLReader */
	public URLReader() {
	}

	public static String getURLContentAsString(java.net.URL url) {
		try {
			InputStream urlStream = url.openStream();
			// search the input stream for links
			// first, read in the entire URL
			byte b[] = new byte[1000];
			int numRead = urlStream.read(b);
			String content = new String(b, 0, numRead);
			while (numRead != -1) {
				numRead = urlStream.read(b);
				if (numRead != -1) {
					String newContent = new String(b, 0, numRead);
					content += newContent;
				}
			}
			urlStream.close();

			return content;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

}
