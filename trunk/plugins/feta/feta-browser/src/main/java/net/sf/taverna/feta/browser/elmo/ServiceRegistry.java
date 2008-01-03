package net.sf.taverna.feta.browser.elmo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.openrdf.elmo.ElmoManager;
import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.ElmoQuery;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import uk.org.mygrid.mygridmobyservice.Organisation;
import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class ServiceRegistry {

	private static ServiceRegistry instance;

	public synchronized static ServiceRegistry getInstance() {
		if (instance == null) {
			instance = new ServiceRegistry();
		}
		return instance;
	}

	private File dataDir = new File("/tmp/serviceRegistry");

	private SailRepository repository;
	private SailRepositoryConnection repConnection;
	private ValueFactory valueFactory;
	private SesameManagerFactory factory;
	private ElmoManager elmoManager;

	protected ServiceRegistry() {
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException("Could not create ServiceRegistry", e);
		}
	}

	public void init() throws RepositoryException, RDFParseException,
			IOException {
		System.out.println("Creating service registry..");
		dataDir.mkdirs();
		repository = new SailRepository(new MemoryStore(dataDir));
		repository.initialize();
		repConnection = repository.getConnection();
		valueFactory = repository.getValueFactory();
		ElmoModule elmoModule = new ElmoModule();
		factory = new SesameManagerFactory(elmoModule, repository);
		elmoManager = factory.createElmoManager();
		System.out.println("Created service registry instance in " + dataDir);
	}

	public void updateFeta() throws MalformedURLException, IOException,
			RDFParseException, RepositoryException {
		repConnection.clear();
		String location = "http://soiland.no/feta-2007-12-19.n3";
		String baseURI = location;
		URL url = new URL(location);
		URI context = valueFactory.createURI(location);
		repConnection.add(url, baseURI, RDFFormat.N3, context);
	}

	public ElmoManager getElmoManager() {
		return elmoManager;
	}

	public ServiceDescription getServiceDescription(String id) {
		QName name = new QName(
				"urn:lsid:www.mygrid.org.uk:serviceDescription:", id);
		return getElmoManager().create(ServiceDescription.class, name);
	}

	public Iterable<ServiceDescription> getServiceDescriptions() {
		return getElmoManager().findAll(ServiceDescription.class);
	}

	@SuppressWarnings("unchecked")
	public Organisation getOrganisationByName(String name) {
		String query = "PREFIX service: <http://www.mygrid.org.uk/mygrid-moby-service#>\n"
				+ "SELECT ?org\n"
				+ "WHERE {\n"
				+ " ?org service:hasOrganisationNameText ?name \n" + "}";
		System.out.println(query);
		ElmoQuery<Organisation> elmoQuery = (ElmoQuery<Organisation>) getElmoManager()
				.createQuery(query);
		elmoQuery.setParameter("name", name);
		Organisation org = elmoQuery.getSingleResult();
		System.out.println("Found " + org);
		return org;
	}

	@SuppressWarnings("unchecked")
	public List<ServiceDescription> getServicesProvidedByOrganisation(String name) {
		String query = "PREFIX service: <http://www.mygrid.org.uk/mygrid-moby-service#>\n"
				+ "SELECT ?service\n"
				+ "WHERE {\n"
				+ " ?service service:providedBy ?org .\n"
				+ " ?org service:hasOrganisationNameText ?name \n" + "}";
		System.out.println(query);
		ElmoQuery<ServiceDescription> elmoQuery = (ElmoQuery<ServiceDescription>) getElmoManager()
				.createQuery(query);
		elmoQuery.setParameter("name", name);
		return elmoQuery.getResultList();
	}
}
