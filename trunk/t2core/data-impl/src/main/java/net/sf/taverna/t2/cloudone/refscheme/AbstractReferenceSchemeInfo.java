/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.cloudone.refscheme;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceSchemeInfoSPI;

/**
 * Abstract superclass for reference scheme information. This class provides the
 * required key information to be populated by the hosting data manager through
 * a file META-INF/referencescheme/&lt;implementingclass&gt;. This file is a
 * properties-like syntax specified as &lt;contextname&gt; = &lt; space
 * separated &lt;dot separated key name&gt; &gt;
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
@SuppressWarnings("unchecked")
public abstract class AbstractReferenceSchemeInfo<RS extends ReferenceScheme> implements
		ReferenceSchemeInfoSPI<RS> {

	private Map<String, Set<List<String>>> keyMap = new HashMap<String, Set<List<String>>>();

	protected AbstractReferenceSchemeInfo() {
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
