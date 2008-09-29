/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.store.test;

import java.util.*;
import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.baclava.factory.*;
import org.embl.ebi.escience.baclava.store.*;

/**
 * Tests the MySQL jdbc backed BaclavaDataService
 * 
 * @author Tom Oinn
 */
public class JDBCStoreTest {

	public static void main(String[] args) throws Exception {

		// Initialize the proxy settings etc.
		ResourceBundle rb = ResourceBundle.getBundle("mygrid");
		Properties sysProps = System.getProperties();
		Enumeration keys = rb.getKeys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = (String) rb.getString(key);
			sysProps.put(key, value);
		}

		// Create a DataThing with some strings in it
		String[] data = new String[] { "foo", "bar", "urgle", "wibble" };
		DataThing dataThing = new DataThing(data);
		dataThing.fillLSIDValues();

		JDBCBaclavaDataService service = new JDBCBaclavaDataService();
		service.reinit();

		service.storeDataThing(dataThing, true);

		// Try to pull back DataThing objects for each of the LSIDs in the
		// original one.
		String[] lsids = dataThing.getAllLSIDs();
		for (int i = 0; i < lsids.length; i++) {
			try {
				DataThing fetchedThing = service.fetchDataThing(lsids[i]);
				System.out.println("Fetched datathing for lsid = " + lsids[i]);
				System.out.println(fetchedThing);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

}
