package net.sf.taverna.t2.provenance.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jdom.JDOMException;

import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.api.Query;
import net.sf.taverna.t2.provenance.api.QueryAnswer;
import net.sf.taverna.t2.provenance.client.Janus.StandAloneRDFProvenanceWriter;
import net.sf.taverna.t2.provenance.client.XMLQuery.ProvenanceQueryParser;
import net.sf.taverna.t2.provenance.client.XMLQuery.QueryParseException;
import net.sf.taverna.t2.provenance.client.XMLQuery.QueryValidationException;

public class ProvenanceExporter {

	public ProvenanceExporter() {		
	}
	
	public ProvenanceExporter(ProvenanceAccess provenanceAccess) {
		this.provenanceAccess = provenanceAccess;		
	}
	
	private ProvenanceAccess provenanceAccess;

	public void setProvenanceAccess(ProvenanceAccess provenanceAccess) {
		this.provenanceAccess = provenanceAccess;
	}

	public ProvenanceAccess getProvenanceAccess() {
		return provenanceAccess;
	}
	
	public void exportAsJanusRDF(String workflowRunId, OutputStream outStream) throws SQLException {
		NativeToRDF nativeToRdf = new NativeToRDF();
		ProvenanceQueryParser pqp = new ProvenanceQueryParser();
		pqp.setPAccess(getProvenanceAccess());
		nativeToRdf.setPqp(pqp);
		nativeToRdf.setpAccess(getProvenanceAccess());
		nativeToRdf.setInvocationContext(getProvenanceAccess().getInvocationContext());
		StandAloneRDFProvenanceWriter rdfWriter = new StandAloneRDFProvenanceWriter(
				getProvenanceAccess().getPq(), outStream);
		nativeToRdf.generateRDF(workflowRunId, rdfWriter);
	}
	
	public void exportAsOPMRDF(String workflowRunId, OutputStream outStream) throws JDOMException, QueryParseException, QueryValidationException, SQLException, IOException {
		String dataflowId = getProvenanceAccess().getTopLevelWorkflowID(workflowRunId);
		String query = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
		"<pquery xmlns=\"http://taverna.org.uk/2009/provenance/pquery/\"\n" + 
		"        xsi:schemaLocation=\"http://taverna.org.uk/2009/provenance/pquery/ pquery.xsd\" \n" + 
		"        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
		"	<scope workflowId='" + dataflowId + "'>\n" +
		"		<runs>\n" + 				 
		"			<run id='" +workflowRunId + "' />\n" + 				 
		"		</runs>\n" + 
		"	</scope>\n" + 
		"</pquery>";

		ProvenanceQueryParser queryParser = new ProvenanceQueryParser();
		queryParser.setPAccess(getProvenanceAccess());
		
		Query provQuery = queryParser.parseProvenanceQueryXml(query);
		
		QueryAnswer answer = getProvenanceAccess()
				.executeQuery(provQuery);
		String rdfXml = answer.getOPMAnswer_AsRDF();
		OutputStreamWriter outWriter = new OutputStreamWriter(outStream, "utf-8");
		outWriter.write(rdfXml);
		outWriter.flush();
	}
}
