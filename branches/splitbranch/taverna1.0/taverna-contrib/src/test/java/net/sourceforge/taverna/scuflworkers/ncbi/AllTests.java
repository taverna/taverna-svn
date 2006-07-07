package net.sourceforge.taverna.scuflworkers.ncbi;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class
 * 
 * Last edited by $Author: davidwithers $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.1 $
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for net.sourceforge.taverna.scuflworkers.ncbi");
        //$JUnit-BEGIN$
        suite.addTestSuite(HomoloGeneWorkerTest.class);
        suite.addTestSuite(EntrezGeneWorkerTest.class);
        suite.addTestSuite(INSDSeqXMLWorkerTest.class);
        suite.addTestSuite(LocusLinkWorkerTest.class);
        suite.addTestSuite(NucleotideFastaWorkerTest.class);
        suite.addTestSuite(NucleotideGBSeqWorkerTest.class);
        suite.addTestSuite(NucleotideINSDSeqXMLWorkerTest.class);
        suite.addTestSuite(NucleotideTinySeqXMLWorkerTest.class);
        suite.addTestSuite(OMIMWorkerTest.class);
        suite.addTestSuite(NucleotideXMLWorkerTest.class);
        suite.addTestSuite(EntrezProteinWorkerTest.class);
        suite.addTestSuite(ProteinFastaWorkerTest.class);
        suite.addTestSuite(ProteinGBSeqWorkerTest.class);
        suite.addTestSuite(ProteinINSDSeqXMLWorkerTest.class);
        suite.addTestSuite(ProteinTinySeqXMLWorkerTest.class);
        suite.addTestSuite(PubMedEFetchWorkerTest.class);
        suite.addTestSuite(PubMedESearchWorkerTest.class);
        //$JUnit-END$
        return suite;
    }
}
