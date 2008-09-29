package net.sourceforge.taverna.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

/**
 * This class
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class GetStreamProcessor extends AbstractStreamProcessor implements StreamProcessor {

	public GetStreamProcessor() {

	}

	/**
	 * @see net.sourceforge.taverna.io.StreamProcessor#processStream(java.io.InputStream)
	 */
	public Map processStream(InputStream stream) throws IOException {
		HashMap outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
		StringBuffer sb = new StringBuffer(2000);

		BufferedReader rd = new BufferedReader(new InputStreamReader(stream));

		String str;

		while ((str = rd.readLine()) != null) {
			sb.append(str);
			sb.append(NEWLINE);
		}
		rd.close();

		outAdapter.putString("outputText", sb.toString());

		return outputMap;
	}

}
