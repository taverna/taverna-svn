/*
 *
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
 *
 * Created on 28 December 2004, 07:21
 */

package uk.ac.man.cs.img.fetaEngine.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import uk.ac.man.cs.img.fetaEngine.store.load.FetaLoadException;

/**
 * 
 * @author alperp
 */
public class FetaEngineProperties {

	private static Properties fetaProperties;

	/** Creates a new instance of FetaEngineProperties */
	public FetaEngineProperties() throws FetaEngineException {

		try {
			fillProperties();
		} catch (IOException e1) {
			throw new FetaEngineException("Problem loading configuration", e1);
		} catch (NullPointerException npe) {
			throw new FetaEngineException("Couldn't find configuration", npe);
		}
	}

	public void fillProperties() throws IOException {

		if (fetaProperties == null) {
			fetaProperties = new Properties();
			InputStream inStr = this.getClass().getResourceAsStream(
					"/fetaEngine.properties");

			fetaProperties.load(inStr);
			inStr.close();
		}

	}

	public List getFetaOntologyLocations() throws FetaLoadException {

		List ontos = new ArrayList();
		int i = 0;

		while (fetaProperties.containsKey("fetaEngine.ontology.URL" + i)) {
			String ontologyLoc = fetaProperties
					.getProperty("fetaEngine.ontology.URL" + i);
			ontos.add(ontologyLoc);
			i++;
		}

		return ontos;
	}

	public List getLocationstoCrawlandPoll() throws FetaLoadException {

		List locs = new ArrayList();
		int i = 0;

		while (fetaProperties.containsKey("fetaEngine.housekeeper.poll_location" + i)) {
			String loc = fetaProperties.getProperty("fetaEngine.housekeeper.poll_location" + i);
			locs.add(loc);
			i++;
		}

		return locs;
	}

	
	public String getPropertyValue(String propertyName, String defaultValue) {
		return fetaProperties.getProperty(propertyName, defaultValue);
	}

	public String getPropertyValue(String propertyName) {
		return fetaProperties.getProperty(propertyName);
	}

	public static Properties getProperties() {
		if (fetaProperties == null) {
			try {
				FetaEngineProperties prop = new FetaEngineProperties();
				return prop.getProperties();

			} catch (FetaEngineException ex) {
				ex.printStackTrace();
				return null;
			}
		} else {
			return fetaProperties;
		}

	}

}
