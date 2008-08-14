/**
 * 
 */
package uk.org.mygrid.sogsa.sbs.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.org.mygrid.sogsa.sbs.ClientConfig;
import uk.org.mygrid.sogsa.sbs.SemanticBindingService;
import uk.org.mygrid.sogsa.sbs.utils.Util;

/**
 * @author paolo
 *
 */
public class AnnotatorTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link uk.org.mygrid.sogsa.sbs.semanticAnnotation.OPMGraphAnnotator#annotateRDF(java.lang.String)}.
	 */
	@Test
	public final void testAnnotateRDF() {
		
		String testRDFFile = ClientConfig.getString("SOGSAClient.3");
		
		SemanticBindingService sbs = new SemanticBindingService(null);  // CHECK this is legal
		
		String annotatedRDF = sbs.annotateRDF(Util.textFileToContent(testRDFFile));
		
		assertTrue("annotated RDF:\n"+annotatedRDF, annotatedRDF!=null);
	}

}
