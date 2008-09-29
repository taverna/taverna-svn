/*
 * RemoveWSTest.java
 *
 * Created on 09 March 2006, 17:16
 */

package uk.ac.man.cs.img.fetaEngine.webservice;

/**
 * 
 * @author Pinar
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

public class RemoveWSTest extends TestCase {
	private String fetaURL = "http://localhost:8080/fetaEngine1.0/services/feta";

	/*
	 * @see TestCase#setUp()
	 */
	public void setUp() throws Exception {
		super.setUp();
		addProperties();
		String tmp = System.getProperty("fetaEngine.test.ws.url");
		if (tmp != null)
			fetaURL = tmp;

	}

	/*
	 * @see TestCase#tearDown()
	 */

	public void tearDown() throws Exception {

		super.tearDown();
	}

	public void testRemove() throws Exception {

		try {
			FetaCannedRequestType req1 = new FetaCannedRequestType(
					CannedQueryType.GetAll, "");
			FetaCannedRequestType[] tmp = { req1 };
			FetaCompositeSearchRequestType reqs = new FetaCompositeSearchRequestType(
					tmp);
			Set descLocSet = copyArrayToSet(executeSearchRequest(tmp));

			if (descLocSet.isEmpty()) {
				System.out.println("The set is empty . . . . ");
			} else {
				System.out.println(descLocSet.toString());
			}
			for (Iterator iter = descLocSet.iterator(); iter.hasNext();) {
				String operURI = (String) iter.next();
				System.out.println("The service to be removed is  --> "
						+ operURI);
				assertTrue(executeRemoveRequest(operURI).getRemoveResult()
						.getValue().toString().equalsIgnoreCase("Success"));
			}

			/*
			 * assertEquals(executeSearchRequest(tmp), null);
			 */

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	private Set copyArrayToSet(String[] arr) {

		Set descLocSet = new HashSet();
		for (int i = 0; i < arr.length; i++) {

			int ind = arr[i].indexOf('$');
			String operURI = arr[i].substring(0, ind);
			descLocSet.add(operURI);

		}

		return descLocSet;
	}

	private FetaRemoveResponseType executeRemoveRequest(String operURLString) {

		FetaRemoveResponseType response;
		try {

			FetaPortType port = new FetaLocator().getfeta(new java.net.URL(
					fetaURL));

			System.out.println("Got the service binding with time out of "
					+ ((FetaPortTypeBindingStub) port).getTimeout());

			response = (FetaRemoveResponseType) ((FetaPortTypeBindingStub) port)
					.removeDescription(operURLString);

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public void addProperties() throws Exception {
		try {
			InputStream inStr = this.getClass().getResourceAsStream(
					"/fetaEngine.properties");
			Properties props = new Properties();
			// load configuration properties
			props.load(inStr);
			inStr.close();
			for (Enumeration iter = props.keys(); iter.hasMoreElements();) {
				String key = (String) iter.nextElement();
				System.setProperty(key, props.getProperty(key));
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			throw new Exception("Problem loading configuration", e1);
		} catch (NullPointerException npe) {
			throw new Exception("Couldn't find configuration", npe);
		}
		// add to system properties}
	}

	private String[] executeSearchRequest(FetaCannedRequestType[] requests) {

		FetaSearchResponseType response = null;
		try {

			FetaCompositeSearchRequestType reqs = new FetaCompositeSearchRequestType(
					requests);

			FetaPortType port = new FetaLocator().getfeta(new java.net.URL(
					fetaURL));

			// ((FetaPortTypeBindingStub)port).setTimeout(Integer.MAX_VALUE);

			System.out.println("Got the service binding with time out of "
					+ ((FetaPortTypeBindingStub) port).getTimeout());

			response = (FetaSearchResponseType) ((FetaPortTypeBindingStub) port)
					.inquire(reqs);
			if (response == null)
				return null;

		} catch (Exception e) {

			e.printStackTrace();

		}
		return response.getOperationURI();
	}

}
