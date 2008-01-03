package net.sf.taverna.feta.browser;

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.elmo.ElmoManager;
import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.elmo.sesame.roles.SesameEntity;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class ElmoTest {

	private SailRepository repository;
	private SailRepositoryConnection repConnection;
	private SesameManagerFactory factory;
	private ValueFactory valueFactory;


	@Before
	public void makeRepository() throws RepositoryException, RDFParseException, IOException {
		repository = new SailRepository(new MemoryStore());
		repository.initialize();
		
		repConnection = repository.getConnection();
		valueFactory = repository.getValueFactory();
		String location = "http://soiland.no/feta-2007-12-19.n3";
		String baseURI = location;
		URL url = new URL(location);
		URI context = valueFactory.createURI(location);
		repConnection.add(url, baseURI, RDFFormat.N3, context);
		
	}
	

	@Test
	public void doElmoStuff() throws Exception {
		System.out.println("w00t");
		ElmoModule elmoModule = new ElmoModule();
		factory = new SesameManagerFactory(elmoModule, repository);
		ElmoManager manager = factory.createElmoManager();
		
		QName id = new QName("urn:lsid:www.mygrid.org.uk:serviceDescription:", "ae5c01a5-4377-405a-b3f7-d8a8e449b348");
		ServiceDescription o = manager.create(ServiceDescription.class, id);
		System.out.println(o.getLocationURI());
		
		
	}

}
