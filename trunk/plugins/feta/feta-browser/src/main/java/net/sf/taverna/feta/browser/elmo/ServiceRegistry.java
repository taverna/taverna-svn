package net.sf.taverna.feta.browser.elmo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.openrdf.concepts.rdfs.Class;
import org.openrdf.elmo.ElmoManager;
import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.ElmoQuery;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import uk.org.mygrid.mygridmobyservice.Operation;
import uk.org.mygrid.mygridmobyservice.Organisation;
import uk.org.mygrid.mygridmobyservice.Parameter;
import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class ServiceRegistry {

	private static ServiceRegistry instance;

	public synchronized static ServiceRegistry getInstance() {
		if (instance == null) {
			instance = new ServiceRegistry();
		}
		return instance;
	}

	private QName bioAlgorithmQName = new QName(
			"http://www.mygrid.org.uk/ontology#", "bioinformatics_algorithm");

	private QName bioConceptQName = new QName(
			"http://www.mygrid.org.uk/ontology#", "bioinformatics_concept");
	private QName bioDataResourceQName = new QName(
			"http://www.mygrid.org.uk/ontology#",
			"bioinformatics_data_resource");
	private QName bioTaskQName = new QName(
			"http://www.mygrid.org.uk/ontology#", "bioinformatics_task");
	private File dataDir = new File("/tmp/serviceRegistry");
	private ElmoManager elmoManager;

	private SesameManagerFactory factory;
	private SailRepositoryConnection repConnection;

	private SailRepository repository;

	private ValueFactory valueFactory;

	private Date lastUpdated;

	protected ServiceRegistry() {
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException("Could not create ServiceRegistry", e);
		}
	}

	public List<ServiceDescription> getServicesParamPred(String hasParameter,
			String paramPred, Class type) {
		QName hasParameterName = new QName(
				"http://www.mygrid.org.uk/mygrid-moby-service#", hasParameter);
		QName paramPredName = new QName(
				"http://www.mygrid.org.uk/mygrid-moby-service#", paramPred);

		String query = "PREFIX service: <http://www.mygrid.org.uk/mygrid-moby-service#>\n"
				+ "SELECT DISTINCT ?service\n"
				+ "WHERE {\n"
				+ " ?obj a ?objectClass .\n"
				+ " ?service service:hasOperation ?oper .\n"
				+ " ?param ?paramPred ?obj .\n"
				+ " ?oper ?hasParameter ?param .\n" + "}";
		ElmoQuery<ServiceDescription> elmoQuery = (ElmoQuery<ServiceDescription>) getElmoManager()
				.createQuery(query);
		elmoQuery.setParameter("hasParameter", hasParameterName);
		elmoQuery.setParameter("paramPred", paramPredName);
		elmoQuery.setParameter("objectClass", type);
		return elmoQuery.getResultList();
	}

	public Class getBioConceptClass() {
		return (Class) getElmoManager().find(bioConceptQName);
	}

	public Class getBioMethodClass() {
		return (Class) getElmoManager().find(bioAlgorithmQName);
	}

	public Class getBioResourceClass() {
		return (Class) getElmoManager().find(bioDataResourceQName);
	}

	public Class getBioTaskClass() {
		return (Class) getElmoManager().find(bioTaskQName);
	}

	public Class getClass(QName qname) {
		Class resourceClass = (Class) getElmoManager().find(qname);
		if (resourceClass.getRdfsSubClassOf().isEmpty()) {
			return null;
		}
		return resourceClass;
	}

	@SuppressWarnings("unchecked")
	public List<Class> getClassesForHaving(Object subject, String predicate) {
		QName predName = new QName(
				"http://www.mygrid.org.uk/mygrid-moby-service#", predicate);
		String query = "PREFIX service: <http://www.mygrid.org.uk/mygrid-moby-service#>\n"
				+ "SELECT ?aClass \n"
				+ "WHERE {\n"
				+ " ?subject ?predicate ?object .\n"
				+ " ?object a ?aClass \n"
				+ "}";
		ElmoQuery<Class> elmoQuery = (ElmoQuery<Class>) getElmoManager()
				.createQuery(query);
		elmoQuery.setParameter("predicate", predName);
		elmoQuery.setParameter("subject", subject);
		List<Class> results = elmoQuery.getResultList();
		return results;
	}

	public ElmoManager getElmoManager() {
		return elmoManager;
	}

	public List<Class> getMethodClasses() {
		return getSubClasses(getBioMethodClass());
	}

	public List<Class> getMethodsUsedBy(Operation operation) {
		return getClassesForHaving(operation, "usesMethod");
	}

	@SuppressWarnings("unchecked")
	public Organisation getOrganisationByName(String name) {
		String query = "PREFIX service: <http://www.mygrid.org.uk/mygrid-moby-service#>\n"
				+ "SELECT ?org\n"
				+ "WHERE {\n"
				+ " ?org service:hasOrganisationNameText ?name \n" + "}";
		ElmoQuery<Organisation> elmoQuery = (ElmoQuery<Organisation>) getElmoManager()
				.createQuery(query);
		elmoQuery.setParameter("name", name);
		Organisation org = elmoQuery.getSingleResult();
		return org;
	}

	public Iterable<Organisation> getOrganisations() {
		return getElmoManager().findAll(Organisation.class);
	}

	public List<Class> getParamNamespaces(Parameter param) {
		return getClassesForHaving(param, "inNamespaces");
	}

	public List<Class> getParamObjectType(Parameter param) {
		return getClassesForHaving(param, "objectType");
	}

	public List<Class> getResourceClasses() {
		return getSubClasses(getBioResourceClass());
	}

	public List<Class> getResourcesUsedBy(Operation operation) {
		return getClassesForHaving(operation, "usesResource");
	}

	public ServiceDescription getServiceDescription(String id) {
		QName name = new QName(
				"urn:lsid:www.mygrid.org.uk:serviceDescription:", id);
		return getElmoManager().create(ServiceDescription.class, name);
	}

	public Iterable<ServiceDescription> getServiceDescriptions() {
		return getElmoManager().findAll(ServiceDescription.class);
	}

	public List<ServiceDescription> getServicesConsuming(Class type) {
		return getServicesParamPred("inputParameter", "objectType", type);
	}

	public List<ServiceDescription> getServicesConsumingNamespace(Class type) {
		return getServicesParamPred("inputParameter", "inNamespaces", type);
	}

	public List<ServiceDescription> getServicesMethodHas(Class objectClass,
			String predicate) {
		QName predName = new QName(
				"http://www.mygrid.org.uk/mygrid-moby-service#", predicate);

		String query = "PREFIX service: <http://www.mygrid.org.uk/mygrid-moby-service#>\n"
				+ "SELECT ?service\n"
				+ "WHERE {\n"
				+ " ?service service:hasOperation ?oper .\n"
				+ " ?oper ?operPred ?object .\n"
				+ " ?object a ?objectClass\n"
				+ "}";
		ElmoQuery<ServiceDescription> elmoQuery = (ElmoQuery<ServiceDescription>) getElmoManager()
				.createQuery(query);
		elmoQuery.setParameter("objectClass", objectClass);
		elmoQuery.setParameter("operPred", predName);
		return elmoQuery.getResultList();
	}

	public List<ServiceDescription> getServicesPerforming(Class task) {
		return getServicesMethodHas(task, "performsTask");
	}

	public List<ServiceDescription> getServicesProducing(Class type) {
		return getServicesParamPred("outputParameter", "objectType", type);
	}

	public List<ServiceDescription> getServicesProducingNamespace(Class type) {
		return getServicesParamPred("outputParameter", "inNamespaces", type);
	}

	@SuppressWarnings("unchecked")
	public List<ServiceDescription> getServicesProvidedByOrganisation(
			String name) {
		String query = "PREFIX service: <http://www.mygrid.org.uk/mygrid-moby-service#>\n"
				+ "SELECT ?service\n"
				+ "WHERE {\n"
				+ " ?service service:providedBy ?org .\n"
				+ " ?org service:hasOrganisationNameText ?name \n" + "}";
		ElmoQuery<ServiceDescription> elmoQuery = (ElmoQuery<ServiceDescription>) getElmoManager()
				.createQuery(query);
		elmoQuery.setParameter("name", name);
		return elmoQuery.getResultList();
	}

	public List<ServiceDescription> getServicesUsingMethod(Class method) {
		return getServicesMethodHas(method, "usesMethod");
	}

	public List<ServiceDescription> getServicesUsingResource(Class resource) {
		return getServicesMethodHas(resource, "usesResource");
	}

	public List<Class> getSubClasses(Class parent) {
		String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "SELECT ?class\n" + "WHERE {\n"
				+ " ?class rdfs:subClassOf ?parent\n" + "}";
		ElmoQuery<Class> elmoQuery = (ElmoQuery<Class>) getElmoManager()
				.createQuery(query);
		elmoQuery.setParameter("parent", parent);
		return elmoQuery.getResultList();
	}

	public List<Class> getTaskClasses() {
		String query = "PREFIX service: <http://www.mygrid.org.uk/mygrid-moby-service#>\n"
				+ "SELECT DISTINCT ?taskClass\n"
				+ "WHERE {\n"
				+ " ?oper service:performsTask ?task .\n"
				+ " ?task a ?taskClass \n"
				+ "}";
		ElmoQuery<Class> elmoQuery = (ElmoQuery<Class>) getElmoManager()
				.createQuery(query);
		return elmoQuery.getResultList();
	}

	public List<Class> getTasksPerformedBy(Operation operation) {
		return getClassesForHaving(operation, "performsTask");
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
		lastUpdated = new Date();
	}

	public List<Class> getNamespaceClasses() {
		String query = "PREFIX service: <http://www.mygrid.org.uk/mygrid-moby-service#>\n"
				+ "SELECT DISTINCT ?nsClass\n"
				+ "WHERE {\n"
				+ " ?param service:inNamespaces ?ns .\n"
				+ " ?ns a ?nsClass \n"
				+ "}";
		ElmoQuery<Class> elmoQuery = (ElmoQuery<Class>) getElmoManager()
				.createQuery(query);
		return elmoQuery.getResultList();
	}

	public List<Class> getTypesClasses() {
		String query = "PREFIX service: <http://www.mygrid.org.uk/mygrid-moby-service#>\n"
				+ "SELECT DISTINCT ?nsClass\n"
				+ "WHERE {\n"
				+ " ?param service:objectType ?ns .\n"
				+ " ?ns a ?nsClass \n"
				+ "}";
		ElmoQuery<Class> elmoQuery = (ElmoQuery<Class>) getElmoManager()
				.createQuery(query);
		return elmoQuery.getResultList();
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

}
