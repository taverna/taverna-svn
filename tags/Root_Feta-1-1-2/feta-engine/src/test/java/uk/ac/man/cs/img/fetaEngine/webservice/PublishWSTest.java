package uk.ac.man.cs.img.fetaEngine.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import junit.framework.TestCase;

public class PublishWSTest extends TestCase {

	// set a default in case throws properties file value is not set.
	private String fetaURL = "http://localhost:8080/fetaEngine1.0/services/feta";

	private String fetaDescriptionLocation = "file:///C:/work/code/mygrid/fetaEngine1.0/etc/testData/services_test.xml";

	/*
	 * @see TestCase#setUp()
	 */
	public void setUp() throws Exception {
		super.setUp();
		addProperties();
		String tmp = System.getProperty("fetaEngine.test.ws.url");
		if (tmp != null)
			fetaURL = tmp;
		String tmp2 = System.getProperty("fetaEngine.test.file");
		if (tmp2 != null)
			fetaDescriptionLocation = tmp2;

	}

	/*
	 * @see TestCase#tearDown()
	 */

	public void tearDown() throws Exception {

		super.tearDown();
	}

	public void testPublish() throws Exception {

		try {

			FetaCannedRequestType req1 = new FetaCannedRequestType(
					CannedQueryType.GetAll, "");

			FetaCannedRequestType[] tmp = { req1 };
			FetaCompositeSearchRequestType reqs = new FetaCompositeSearchRequestType(
					tmp);

			// first the repository should be empty
			if (executeSearchRequest(tmp) == null) {
				executePublishRequest(fetaDescriptionLocation)
						.getPublishResult().getValue().toString()
						.equalsIgnoreCase("Success");
				assertTrue((executeSearchRequest(tmp).length > 0 ? true : false));
			} else {

				printResults(executeSearchRequest(tmp));

				assertTrue(false);
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	private void printResults(String[] arr) {

		for (int i = 0; i < arr.length; i++) {

			System.out.println(arr[i]);
		}

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

	private FetaPublishResponseType executePublishRequest(String operURLString) {

		FetaPublishResponseType response = null;
		try {

			FetaPortType port = new FetaLocator().getfeta(new java.net.URL(
					fetaURL));

			System.out.println("Got the service binding with time out of "
					+ ((FetaPortTypeBindingStub) port).getTimeout());

			response = (FetaPublishResponseType) ((FetaPortTypeBindingStub) port)
					.publishDescription(operURLString);

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
}