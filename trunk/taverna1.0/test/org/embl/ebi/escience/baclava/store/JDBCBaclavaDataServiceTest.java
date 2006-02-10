package org.embl.ebi.escience.baclava.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import junit.framework.TestCase;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.StupidLSIDProvider;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;

public class JDBCBaclavaDataServiceTest extends TestCase {
    private JDBCBaclavaDataService dataService;
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(JDBCBaclavaDataServiceTest.class);
	}

	public JDBCBaclavaDataServiceTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
        // Initialize the proxy settings etc.
        ResourceBundle rb = ResourceBundle.getBundle("mygrid");
            Properties sysProps = System.getProperties();
            Enumeration keys = rb.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) rb.getString(key);
            sysProps.put(key, value);
        }
        dataService = new JDBCBaclavaDataService();
        dataService.reinit();
        DataThing.SYSTEM_DEFAULT_LSID_PROVIDER = new StupidLSIDProvider();
	}

	protected void tearDown() throws Exception {
        dataService = null;
	}

	/*
	 * Test method for 'JDBCBaclavaDataService.storeDataThing(DataThing)'
     * and 'JDBCBaclavaDataService.fetchDataThing(String)'
	 */
	public final void testStoreAndFetchDataThing() throws Exception {
        String simpleData = "test string";
        DataThing dataThing = new DataThing(simpleData);
        dataThing.fillLSIDValues();
        dataThing.setLSID(dataThing, "1");
        dataService.storeDataThing(dataThing, true);
        DataThing fetchedThing = dataService.fetchDataThing("1");
        Object fetchedData = fetchedThing.getDataObject();
        assertEquals(simpleData, fetchedData);
        
        List collectionData = Arrays.asList(new String[] {"one", "two", "three", "four"});
        dataThing = new DataThing(collectionData);
        dataThing.fillLSIDValues();
        dataThing.setLSID(dataThing, "2");
        dataService.storeDataThing(dataThing, true);
        fetchedThing = dataService.fetchDataThing("2");
        fetchedData = fetchedThing.getDataObject();
        assertEquals(collectionData, fetchedData);
        
        collectionData = new ArrayList();
        collectionData.add(Arrays.asList(new String[] {"five", "six"}));
        collectionData.add(Arrays.asList(new String[] {"seven"}));
        dataThing = new DataThing(collectionData);
        dataThing.fillLSIDValues();
        dataThing.setLSID(dataThing, "3");
        dataService.storeDataThing(dataThing, true);
        fetchedThing = dataService.fetchDataThing("3");
        fetchedData = fetchedThing.getDataObject();
        assertEquals(collectionData, fetchedData);
        
        collectionData = new ArrayList();
        collectionData.add(Arrays.asList(new String[] {"eight", "nine"}));
        collectionData.add("ten");
        dataThing = new DataThing(collectionData);
        dataThing.fillLSIDValues();
        dataThing.setLSID(dataThing, "4");
        dataService.storeDataThing(dataThing, true);
        fetchedThing = dataService.fetchDataThing("4");
        fetchedData = fetchedThing.getDataObject();
        assertEquals(collectionData, fetchedData);
	}

}
