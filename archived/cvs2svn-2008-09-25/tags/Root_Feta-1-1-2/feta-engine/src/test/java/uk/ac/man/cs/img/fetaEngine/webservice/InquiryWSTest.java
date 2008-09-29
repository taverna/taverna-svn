package uk.ac.man.cs.img.fetaEngine.webservice;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import uk.ac.man.cs.img.fetaEngine.TestUtil;

public class InquiryWSTest extends TestCase {

	// set a default in case throws properties file value is not set.
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

	public void testByTask() throws Exception {

		try {
			FetaCannedRequestType req1 = new FetaCannedRequestType(
					CannedQueryType.ByTask,
					"http://www.mygrid.org.uk/ontology#aligning");
			FetaCannedRequestType[] tmp = { req1 };
			FetaCompositeSearchRequestType reqs = new FetaCompositeSearchRequestType(
					tmp);

			assertTrue(checkResultsWithExpected("ByTaskResults",
					executeRequest(tmp)));

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void testByResource() throws Exception {

		try {
			FetaCannedRequestType req1 = new FetaCannedRequestType(
					CannedQueryType.ByResource,
					"http://www.mygrid.org.uk/ontology#bioinformatics_data_resource");
			FetaCannedRequestType[] tmp = { req1 };
			FetaCompositeSearchRequestType reqs = new FetaCompositeSearchRequestType(
					tmp);

			assertTrue(checkResultsWithExpected("ByResourceResults",
					executeRequest(tmp)));

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void testByMethod1() throws Exception {

		try {
			FetaCannedRequestType req1 = new FetaCannedRequestType(
					CannedQueryType.ByMethod,
					"http://www.mygrid.org.uk/ontology#BLAST_Basic_Local_Alignment_Search_Tool");
			FetaCannedRequestType[] tmp = { req1 };
			FetaCompositeSearchRequestType reqs = new FetaCompositeSearchRequestType(
					tmp);

			assertTrue(checkResultsWithExpected("ByMethodResults",
					executeRequest(tmp)));

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void testByApplication2() throws Exception {

		try {
			FetaCannedRequestType req1 = new FetaCannedRequestType(
					CannedQueryType.ByApplication,
					"http://www.mygrid.org.uk/ontology#blastx");
			FetaCannedRequestType[] tmp = { req1 };
			FetaCompositeSearchRequestType reqs = new FetaCompositeSearchRequestType(
					tmp);

			// we expect no results for this query
			assertTrue((executeRequest(tmp) == null));

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void testByMethod2() throws Exception {

		try {
			FetaCannedRequestType req1 = new FetaCannedRequestType(
					CannedQueryType.ByMethod,
					"http://www.mygrid.org.uk/ontology#bioinformatics_algorithm");
			FetaCannedRequestType[] tmp = { req1 };
			FetaCompositeSearchRequestType reqs = new FetaCompositeSearchRequestType(
					tmp);

			// we expect some results now

			assertTrue(checkResultsWithExpected("ByMethodResults",
					executeRequest(tmp)));

			// we expect no results for this query
			// assertTrue((executeRequest(tmp)==null) );

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void testByInput1() throws Exception {

		try {
			FetaCannedRequestType req1 = new FetaCannedRequestType(
					CannedQueryType.ByInput,
					"http://www.mygrid.org.uk/ontology#biological_sequence_id");
			FetaCannedRequestType[] tmp = { req1 };
			FetaCompositeSearchRequestType reqs = new FetaCompositeSearchRequestType(
					tmp);

			assertTrue(checkResultsWithExpected("ByInputResults-1",
					executeRequest(tmp)));

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void testByOutput() throws Exception {

		try {
			FetaCannedRequestType req1 = new FetaCannedRequestType(
					CannedQueryType.ByOutput,
					"http://www.mygrid.org.uk/ontology#nucleotide_sequence");
			FetaCannedRequestType[] tmp = { req1 };
			FetaCompositeSearchRequestType reqs = new FetaCompositeSearchRequestType(
					tmp);

			assertTrue(checkResultsWithExpected("ByOutputResults",
					executeRequest(tmp)));

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void testByName() throws Exception {

		try {
			FetaCannedRequestType req1 = new FetaCannedRequestType(
					CannedQueryType.ByName, "HGVBase");
			FetaCannedRequestType[] tmp = { req1 };
			FetaCompositeSearchRequestType reqs = new FetaCompositeSearchRequestType(
					tmp);

			assertTrue(checkResultsWithExpected("ByNameResults",
					executeRequest(tmp)));

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void testComposite() throws Exception {

		try {

			FetaCannedRequestType req2 = new FetaCannedRequestType(
					CannedQueryType.ByResource,
					"http://www.mygrid.org.uk/ontology#bioinformatics_data_resource");
			FetaCannedRequestType req3 = new FetaCannedRequestType(
					CannedQueryType.ByTask,
					"http://www.mygrid.org.uk/ontology#aligning");

			FetaCannedRequestType[] tmp = { req2, req3 };

			assertTrue(checkResultsWithExpected("compositeResults",
					executeRequest(tmp)));

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void testGetAll() throws Exception {

		try {
			FetaCannedRequestType req1 = new FetaCannedRequestType(
					CannedQueryType.GetAll, "");

			FetaCannedRequestType[] tmp = { req1 };
			FetaCompositeSearchRequestType reqs = new FetaCompositeSearchRequestType(
					tmp);

			assertTrue(checkResultsWithExpected("GetAllResults",
					executeRequest(tmp)));

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void testByDescription() throws Exception {

		try {
			FetaCannedRequestType req1 = new FetaCannedRequestType(
					CannedQueryType.ByDescription, "GO Id");
			FetaCannedRequestType[] tmp = { req1 };
			FetaCompositeSearchRequestType reqs = new FetaCompositeSearchRequestType(
					tmp);

			assertTrue(checkResultsWithExpected("ByDescriptionResults",
					executeRequest(tmp)));

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void testFreeFormQuery() throws Exception {

		try {
			FetaPortType port = new FetaLocator().getfeta(new java.net.URL(
					fetaURL));

			String name = "Omim";
			String queryString = " CONSTRUCT DISTINCT {oper} mg:hasOperationNameText {operName}\n"
					+ " FROM {serv} mg:hasServiceDescriptionLocation {descloc}, \n"
					+ " {serv} mg:hasServiceNameText {servName},\n"
					+ " {serv} mg:hasOperation {oper} mg:hasOperationNameText {operName}\n"
					+ "  WHERE operName  LIKE \"*"
					+ name
					+ "*\" IGNORE CASE \n"
					+ " USING NAMESPACE \n"
					+ " mg = <http://www.mygrid.org.uk/mygrid-moby-service#>\n";
			// " moby =
			// <http://biomoby.org/RESOURCES/MOBY-S/ServiceDescription#>\n" ;

			String response = (String) ((FetaPortTypeBindingStub) port)
					.freeFormQuery(queryString);
			System.out.println(response);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean checkResultsWithExpected(String expectedResultFileName,
			String[] queryResults) {

		Set expectedURISet;
		boolean equalwithExpected = false;
		if (queryResults == null) {

			System.out.print("Query results null!!");
			return equalwithExpected;
		}

		try {
			BufferedReader in = new BufferedReader(new FileReader(
					"./etc/testData/expected-results/" + expectedResultFileName
							+ ".txt"));
			String str;
			expectedURISet = new HashSet();
			System.out.println("---------------");
			while ((str = in.readLine()) != null) {

				if (TestUtil.trim(str).length() > 0) {
					expectedURISet.add(TestUtil.trim(str));
					System.out.println("Expecting " + str);
				}
			}
			in.close();
		} catch (IOException exp) {

			exp.printStackTrace();
			return equalwithExpected;
		}

		// the engine should return a list of unique operation URIs.
		// which equals the set containing the expected results
		if (queryResults.length != expectedURISet.size()) {
			System.out
					.println("UN-EQUALITY IN NUMBER OF EXPECTED AND FOUND RESULTS");
			for (int i = 0; i < queryResults.length; i++) {

				// tokenize location, service name and operation name
				String[] tokens = queryResults[i].split("\\$");
				if (tokens.length > 2) {
					System.out.println("Found " + tokens[1] + "$" + tokens[2]);
				}
			}
			return equalwithExpected;

		} else {
			for (int i = 0; i < queryResults.length; i++) {

				// tokenize location, service name and operation name
				String[] tokens = queryResults[i].split("\\$");
				if (tokens.length > 2) {
					expectedURISet.remove(tokens[1] + "$" + tokens[2]);
					System.out.println("Found " + tokens[1] + "$" + tokens[2]);
				}
			}

			System.out.println("---------------");
			return expectedURISet.isEmpty();
		}
	}

	private String[] executeRequest(FetaCannedRequestType[] requests) {

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