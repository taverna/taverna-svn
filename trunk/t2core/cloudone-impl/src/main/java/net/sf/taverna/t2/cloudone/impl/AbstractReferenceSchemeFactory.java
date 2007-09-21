package net.sf.taverna.t2.cloudone.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.ReferenceSchemeFactorySPI;

/**
 * Abstract superclass for reference scheme factories. This class provides the
 * required key information to be populated by the hosting data manager through
 * a file META-INF/referencescheme/&lt;implementingclass&gt;. This file is a
 * properties-like syntax specified as &lt;contextname&gt; = &lt; space
 * separated &lt;dot separated key name&gt; &gt;
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public abstract class AbstractReferenceSchemeFactory<RS extends ReferenceScheme> implements
		ReferenceSchemeFactorySPI<RS> {

	private Map<String, Set<List<String>>> keyMap = new HashMap<String, Set<List<String>>>();

	protected AbstractReferenceSchemeFactory() {
		String className = this.getClass().getCanonicalName();
		Scanner scanner = new Scanner(this.getClass().getClassLoader()
				.getResourceAsStream(
						"META-INF/referenceschemes/" + className + ".text"));
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split("\\s*=\\s*");
			String contextname = line[0];
			String property = line[1];
			
			Set<List<String>> keySet = new HashSet<List<String>>();
			
			String[] keys = property.split("\\s+");
			for (String key : keys) {
				String[] keyParts = key.split("\\.");
				keySet.add(Arrays.asList(keyParts));
			}
			
			keyMap.put(contextname, keySet);
		}
	}

	public Map<String, Set<List<String>>> getRequiredKeys() {
		return keyMap;
	}

}
