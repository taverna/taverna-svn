/**
 * 
 */
package uk.org.mygrid.sogsa.sbs.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import uk.org.mygrid.sogsa.sbs.SOGSAClient;

/**
 * @author paolo
 *
 */
public class Util {

	
	public static String textFileToContent(String file) {
		
		InputStream resourceAsStream = SOGSAClient.class.getClassLoader()
				.getResourceAsStream(file);
		String rdfString = new String();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				resourceAsStream));
		String inputLine;

		try {
			while ((inputLine = in.readLine()) != null)
				rdfString = rdfString + inputLine;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rdfString;
	}

	
}
