package net.sf.taverna.tools;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Map.Entry;

/**
 * Class responsible for determining the raven repositories during the Bootsrap process
 * @author sowen
 *
 */
public class Repositories {
	
	public URL [] find() {
		Properties properties = RavenProperties.getInstance().getProperties();
//		 entries are named raven.repository.2 = http:// ..
		// We'll add these in order as stated (not as in property file)
		String prefix = "raven.repository.";
		ArrayList<URL> urls = new ArrayList<URL>();
		for (Entry property : properties.entrySet()) {
			String propName = (String) property.getKey();
			if (!propName.startsWith(prefix)) {
				continue;
			}
			String propValue = (String) property.getValue();
			URL url;
			try {
				url = new URL(propValue);
			} catch (MalformedURLException e1) {
				System.err.println("Ignoring invalid URL " + propValue);
				continue;
			}
			int position;
			try {
				position = Integer.valueOf(propName.replace(prefix, ""));
			} catch (NumberFormatException e) {
				// Just ignore the position
				System.err.println("Invalid URL position " + propName);
				urls.add(url);
				continue;
			}
			// Fill up with null's if we are to insert way out there
			while (position >= urls.size()) {
				urls.add(null);
			}
			// .add(pos, url) makes sure we don't overwrite anything
			urls.add(position, url);
		}
		
		// Remove nulls and export as URL[]
		while (urls.remove(null)) {
			// nothing
		}
		return urls.toArray(new URL[0]);
	}
	
}
