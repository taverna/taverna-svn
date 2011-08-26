package uk.org.mygrid.logbook.metadataservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Calendar;

import info.aduna.collections.iterators.CloseableIterator;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.ibm.adtech.boca.model.INamedGraph;

public class JenaSesameHelper {

	static Logger logger = Logger.getLogger(JenaSesameHelper.class);

	public static Model toJenaModel(INamedGraph namedGraph) {
		CloseableIterator<Statement> statements = namedGraph.getStatements();
		Model model = ModelFactory.createDefaultModel();
		while (statements.hasNext()) {
			Statement statement = statements.next();
			Resource subject = statement.getSubject();
			if (!(subject instanceof URI)) {
				logger.warn("Skipping statement: subject " + subject
						+ " not a URI.");
				continue;
			}
			URI predicate = statement.getPredicate();
			Value object = statement.getObject();
			com.hp.hpl.jena.rdf.model.Resource subjectResource = model
					.createResource(subject.toString());
			Property property = model.createProperty(predicate.toString());
			if (object instanceof URI)
				model.add(subjectResource, property, model
						.createResource(object.toString()));
			else if (object instanceof Literal) {
				model.add(subjectResource, property, object.toString());
			} else {
				logger.warn("Skipping statement: object " + object
						+ " not a URI nor a Literal.");
			}
		}
		return model;
	}

	static public String jenaToString(Model model) {
		StringWriter writer = new StringWriter();
		model.write(writer);
		if (model != null)
			model.close();
		return writer.toString();
	}

	static public Repository toRepository(String rdf, String baseURI)
			throws RepositoryException, RDFParseException, IOException {
		Repository myRepository = new SailRepository(new MemoryStore());
		myRepository.initialize();
		RepositoryConnection con = myRepository.getConnection();
		StringReader reader = new StringReader(rdf);
		con.add(reader, baseURI, RDFFormat.RDFXML, new Resource[] {});
		con.close();
		return myRepository;
	}

	static public Repository toRepository(URL rdf, String baseURI)
			throws RepositoryException, RDFParseException, IOException {
		Repository myRepository = new SailRepository(new MemoryStore());
		myRepository.initialize();
		RepositoryConnection con = myRepository.getConnection();
		InputStream inputStream = rdf.openStream();
		con.add(inputStream, baseURI, RDFFormat.RDFXML, new Resource[] {});
		con.close();
		return myRepository;
	}

	static public Repository jenaToRepository(Model model)
			throws RepositoryException, RDFParseException, IOException {
		String rdf = jenaToString(model);
		return toRepository(rdf, ProvenanceOntology.PROVENANCE_NS);
	}

	static Model toJenaModel(String rdf) {
		Model tempModel = JenaSesameHelper.toJenaModel(rdf,
				ProvenanceOntology.PROVENANCE_NS);
		return tempModel;
	}

	static Model toJenaModel(String rdf, String namespace) {
		if (logger.isDebugEnabled()) {
			logger.debug("toJenaModel(String rdf=" + rdf
					+ ", String namespace=" + namespace + ") - start");
		}

		Model tempModel = ModelFactory.createDefaultModel();
		logger.debug("toJenaModel(String, String) - Model tempModel="
				+ tempModel);

		StringReader tempStringReader = new StringReader(rdf);
		logger
				.debug("toJenaModel(String, String) - StringReader tempStringReader="
						+ tempStringReader);

		tempModel.read(tempStringReader, namespace);

		if (logger.isDebugEnabled()) {
			logger.debug("toJenaModel(String, String) - end");
		}
		return tempModel;
	}

	public static Literal getCurrentTimeLiteral() {
		return toTimeLiteral(Calendar.getInstance());
	}

	public static Literal toTimeLiteral(Calendar date) {
		XSDDateTime dateTime = new XSDDateTime(date);
		Literal timeLiteral = new LiteralImpl(dateTime.toString(), new URIImpl(
				XSDDatatype.XSDdateTime.getURI()));
		return timeLiteral;
	}

}
