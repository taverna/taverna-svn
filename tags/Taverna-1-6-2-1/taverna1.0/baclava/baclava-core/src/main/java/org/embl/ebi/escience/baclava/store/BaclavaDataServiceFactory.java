/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */

package org.embl.ebi.escience.baclava.store;

import org.apache.log4j.Logger;

/*
 * Factory class for creating instances of BaclavaDataService according to the
 * property taverna.datastore.class, if set.
 */

public class BaclavaDataServiceFactory {
	static Logger log = Logger.getLogger(BaclavaDataServiceFactory.class.getName());

	/*
	 * Returns an instance of BaclavaDataService, according to the property
	 * taverna.datastore.class returns null if this property is not set, or if
	 * there is a problem creating the store
	 */
	public static BaclavaDataService getStore() {
		BaclavaDataService result = null;
		String storageClassName = System.getProperty("taverna.datastore.class");
		if (storageClassName != null) {
			try {
				Class c = Class.forName(storageClassName);
				result = (BaclavaDataService) c.newInstance();
			} catch (ClassNotFoundException e) {
				log.error("Unable to find class: " + storageClassName + " defined by taverna.datastore.class");
			} catch (InstantiationException e) {
				log.error("InstatiationException when creating " + storageClassName, e);
			} catch (IllegalAccessException e) {
				log.error("IllegalAccessException when creating " + storageClassName, e);
			}
		}
		return result;
	}
}
