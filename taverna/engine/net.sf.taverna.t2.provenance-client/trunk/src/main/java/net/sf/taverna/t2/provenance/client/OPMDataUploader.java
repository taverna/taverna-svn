/**
 * 
 */
package net.sf.taverna.t2.provenance.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.FileManager;

/**
 * @author paolo Missier
 * input: an OPM graph in RDF form. <br/>
 * this util uploads each data element associated to an artifact onto an ftp server,
 * and maps the original data reference (the URI) to the new ftp: URL coming from the server <br/>
 * return the id mappings
 *
 */
public class OPMDataUploader {

	private static final String TMPDIR = "/tmp";

	private static final String SERVER = "rpc264.cs.man.ac.uk";

	private static final String REMOTE_DIR = "pub/";

	private static Logger logger = Logger.getLogger(OPMDataUploader.class);

	ModelMaker mm = null;
	Model      m  = null;

	private List<Statement> newStatements = new ArrayList<Statement>();

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		// load the OPM graph from the command line arg
		if (args.length == 0) {
			System.out.println("need an OPM file name on the command line");
			System.exit(0);
		}
		String OPMFileName = args[0];

		OPMDataUploader uploader = new OPMDataUploader();

		Model m = uploader.openModel(OPMFileName);

		FTPClient ftp = uploader.ftpConnect();

		if (ftp != null) { 
			uploader.uploadAllData(m, ftp);		
			// write OPM model back to file
			uploader.writeCurrentModel(OPMFileName+".uploaded_.rdf");

		} else {
			logger.fatal("could not connect to ftp server, exiting");
		}
		uploader.ftpDisconnect(ftp);
	}



	/**
	 * an OPM Graph is loaded into a Jena Model m. For each Artifact found in the input OPM Graph, this method retrieves the
	 * data values, wraps it into a file, and uploads it to a public (ftp) server.It then asserts the equivalence of the new ftp URL to 
	 * the original Artifact reference, using a sameAs property in the model. The updated model with these additional assertions is returned.  
	 * @param m  a Jena model holding an OPM graph 
	 * @param ftp  an ftp client used to upload the data
	 * @return an updated OPMGraph as a Jena model
	 * @throws IOException
	 */
	public Model uploadAllData(Model m, FTPClient ftp) throws IOException {	

		logger.info("uploading data found in OPM graph...");

		// query to retrieve all artifacts along with their values
		String qstring = 
			"PREFIX t: <http://ns.taverna.org.uk/2011/provenance/opm/> \n"+
			"PREFIX opm: <http://www.ipaw.info/2007/opm#> \n"+			
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"+
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
			"SELECT ?a ?v\n"+
			"WHERE  { \n"+
			"?a rdf:type opm:Artifact . \n"+			
			"OPTIONAL { ?a t:value ?v } . \n"+
			"}";

		ResultSet s = execSPARQL(qstring,m);

		if (!s.hasNext()) { logger.info("no artifacts found for query \n"+qstring); }

		int cnt =0, nullCnt=0;
		while ( s.hasNext() ) {
			QuerySolution sol = s.nextSolution();
			String artifactID=null;
			Literal value=null;

			Resource artifactResource =  sol.getResource("a"); 
			if (artifactResource!= null) artifactID = artifactResource.getURI();
			if (sol.getLiteral("v")!= null) value = sol.getLiteral("v");

			logger.info("found artifact ["+artifactID+" with value \n"+value);

			// wrap this value into a file
			// file name
			if (value != null) {

				UUID fileID = UUID.randomUUID();
				String filename = TMPDIR+"/"+fileID.toString();

				try {
					FileWriter fw = new FileWriter(new File(filename));
					fw.write(value.getString());
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// upload the file
				byte currentXMLBytes[] = value.getString().getBytes();
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentXMLBytes);

				ftp.setFileType(FTP.BINARY_FILE_TYPE);
				ftp.enterLocalPassiveMode();

				boolean success = ftp.storeFile(REMOTE_DIR+fileID.toString(), byteArrayInputStream);
				byteArrayInputStream.close();
				if (!success) {
					logger.info("ERROR uploading value "+value+" to ftp server");
				}
				else {
					logger.info("value "+value+" uploaded to ftp server");

					// assert artifactID sameAs <URL to the ftp server entry>
					String newReference = "ftp://"+SERVER+"/"+REMOTE_DIR+fileID.toString();
					Resource newResource = m.createResource(newReference);
					Property p = m.createProperty("http://www.w3.org/2002/07/owl#", "sameAs");
					newStatements.add(m.createStatement(artifactResource, p, newResource));
				}
			}  else {
				nullCnt++;
			}
			cnt++;
		}

		logger.info("uploaded "+(cnt-nullCnt)+" data artifacts. "+nullCnt+" have null value");
		// add all new statements
		logger.info("asserting "+newStatements.size()+" sameAs statements:");
		for (Statement stmt: newStatements) {
			m.add(stmt);			
			logger.info("equivalence "+stmt.getSubject().getURI()+" sameAs "+stmt.getObject().asResource().getURI()+" asserted");
		}		
		return m;
	}



	FTPClient ftpConnect() {

		FTPClient ftp = new FTPClient();

		try {
			int reply;
			ftp.connect(SERVER);
			System.out.println("Connected to " + SERVER + ".");
			System.out.print(ftp.getReplyString());

			// After connection attempt, you should check the reply code to verify
			// success.
			reply = ftp.getReplyCode();

			if(!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				System.err.println("FTP server refused connection.");
				return null;
			}
			ftp.login("anonymous", "me@my.address");
		} catch(IOException e) {
			e.printStackTrace();
		}
		return ftp;	 
	}


	void ftpDisconnect(FTPClient ftp) {
		try {
			ftp.disconnect();
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch(IOException ioe) {
					// do nothing
				}
			}
		}
	}


	private Model openModel(String OPMFileName) {

		m = ModelFactory.createDefaultModel();

		// use the FileManager to find the input file
		InputStream in = FileManager.get().open( OPMFileName );
		if (in == null) {
			throw new IllegalArgumentException("File: " + OPMFileName + " not found");
		}

		// read the RDF/XML file
		m.read(in, null);
		return m;		
	}


	/**
	 * utility to write to file -- assumes the model is open. error if it is closed
	 */
	public void writeCurrentModel(String fileName) {	
		if (m!=null) {
			try {
				OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileName));
				m.write(osw);	
				logger.info("Model written to ["+fileName+"]");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			logger.fatal("could not write current model to ["+fileName+"]");
		}
	}


	private Model getModel() { return m; }


	public  ResultSet execSPARQL(String qstring) {
		return execSPARQL(qstring, getModel());
	}


	/**
	 * util: execute a SPRQL query
	 * @param qstring a valid SPRQL query string
	 * @return a Jena ResultSet
	 */
	public  ResultSet execSPARQL(String qstring, Model m ) {

		if (m == null)  {
			logger.fatal("null model for query");
			return null;  // should raise an exception
		}

		logger.debug("QUERY: ["+qstring+"]");

		Query q = QueryFactory.create(qstring);
		QueryExecution qexec = QueryExecutionFactory.create(q, m);
		return qexec.execSelect();
	}

}
