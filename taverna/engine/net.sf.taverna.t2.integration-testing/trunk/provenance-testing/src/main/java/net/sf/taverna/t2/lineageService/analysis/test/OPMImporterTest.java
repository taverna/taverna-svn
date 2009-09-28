/**
 * 
 */
package net.sf.taverna.t2.lineageService.analysis.test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.sf.taverna.t2.lineageService.capture.test.testFiles;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceAnalysis;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.mysql.MySQLProvenanceWriter;
import net.sf.taverna.t2.provenance.opm.OPMImporter;

import org.apache.log4j.Logger;

import org.junit.Before;
import org.junit.Test;

/**
 * @author paolo
 *
 */
public class OPMImporterTest {

	private static Logger logger = Logger.getLogger(OPMImporterTest.class);

	// the graph we import if no user preference
	private static final String DEFAULT_OPM_XML = "src/test/resources/provenance-testing/OPM/OPMGraph.xml";

	private String DB_URL_LOCAL = testFiles.getString("dbhost"); // URL of database server //$NON-NLS-1$
	private String DB_USER = testFiles.getString("dbuser"); // database user id //$NON-NLS-1$
	private String DB_PASSWD = testFiles.getString("dbpassword"); //$NON-NLS-1$

	String _aOPM_XMLFile;
	ProvenanceWriter pw;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		String jdbcString = "jdbc:mysql://" + DB_URL_LOCAL +"/T2Provenance?user=" + DB_USER +"&password=" + DB_PASSWD;
		
		// the OPM graph we import
		_aOPM_XMLFile = AnalysisTestFiles.getString("OPM.XMLfile");
		if (_aOPM_XMLFile == null || _aOPM_XMLFile.contains("!")) {
			logger.info("invalid property OPM.XMLfile: using default OPM file "+DEFAULT_OPM_XML);			
			_aOPM_XMLFile = DEFAULT_OPM_XML;			
		}
		
		pw = new MySQLProvenanceWriter();
		pw.setDbURL(jdbcString);
		pw.clearDBStatic();
		pw.clearDBDynamic();
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.provenance.opm.OPMImporter#importGraph(java.lang.String)}.
	 * @throws JAXBException 
	 * @throws SQLException 
	 */
	@Test
	public final void testImportGraph() throws JAXBException, SQLException {

		OPMImporter importer = new OPMImporter(pw);
		
		importer.importGraph(_aOPM_XMLFile);
		
		List<String>  orphans = importer.getOrphanArtifacts();
		
		logger.info("orphan artifacts: ");
		for (String s:orphans) { logger.info(s); }
	
	
	}

}
