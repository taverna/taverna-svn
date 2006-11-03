/*
 * Copyright (C) 2003 The University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ***/
package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FetaClientProperties {

	private static Properties fetaProperties;

	public static Properties getProperties() throws IOException {
		if (fetaProperties == null) {
			fetaProperties = new Properties();
			InputStream stream = FetaClientProperties.class.getClassLoader()
					.getResourceAsStream("fetaClient.properties");
			fetaProperties.load(stream);
		}
		return fetaProperties;
	}

	public static boolean isAnnotator() throws IOException {
		return getProperties().getProperty("annotator") != null;
	}

	public static String getPropertyValue(String propertyName,
			String defaultValue) throws IOException {
		return getProperties().getProperty(propertyName, defaultValue);
	}

	public static String getPropertyValue(String propertyName)
			throws IOException {
		return getProperties().getProperty(propertyName);
	}

	public static String getPropertiesLocation() {
		String directory;

		if (System.getProperty("taverna.home") != null) {
			System.out.println("using taverna.home"); // inserted by jreport
			directory = System.getProperty("taverna.home");
		} else {
			System.out.println("using user.dir"); // inserted by jreport
			directory = System.getProperty("user.dir");
		}

		return directory + File.separator + "conf" + File.separator
				+ "fetaClient.properties";
	}

	public static String getPropertiesDir() {
		String directory;

		if (System.getProperty("taverna.home") != null) {
			System.out.println("using taverna.home"); // inserted by jreport
			directory = System.getProperty("taverna.home");
		} else {
			System.out.println("7 " + "using user.dir"); // inserted by
															// jreport
			directory = System.getProperty("user.dir");
		}

		return directory;
	}

} // FetaClientProperties
