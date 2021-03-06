package net.sf.taverna.feta.browser.elmo;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import net.sf.taverna.feta.browser.util.Utils;
import net.sf.taverna.feta.wsdl.client.Feta;

import org.apache.log4j.Logger;
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

	private static Logger logger = Logger.getLogger(ServiceRegistry.class);

	// Can be overridden with setDataDir(File)
	private static File dataDir = new File("/tmp/serviceRegistry");

	private static final String HASH_METHOD = "SHA-1";

	private static ServiceRegistry instance;

	private static Utils setUtils = Utils.getInstance();

	private static final String UTF_8 = "UTF-8";

	public static File getDataDir() {
		return dataDir;
	}

	public static StringReader getFetaN3() throws Exception {
		// SerQL query to get all statements.
		String query = "CONSTRUCT {serv} p {uri} \n" + "FROM {serv} p {uri}";

		// Feta and friends from target/generated by wsdl2java of
		// cxf-codegen-plugin
		Feta feta = new Feta();
		String brokenN3 = feta.getFeta().freeFormQuery(query);

		String n3 = brokenN3.replace("\t", "    ").replace("j.0:", "dc:");
		// File n3File = new File("/tmp/feta.n3");
		// FileUtils.writeStringToFile(n3File, n3, "utf-8");
		return new StringReader(n3);
	}

	public synchronized static ServiceRegistry getInstance() {
		if (instance == null) {
			instance = new ServiceRegistry();
		}
		return instance;
	}

	public static void setDataDir(File dataDir) {
		ServiceRegistry.dataDir = dataDir;
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
	private ElmoManager elmoManager;

	private SesameManagerFactory factory;

	private Date lastUpdated;

	private SailRepositoryConnection repConnection;

	private SailRepository repository;

	private ValueFactory valueFactory;

	private HashMap<String, ServiceDescription> services;

	protected ServiceRegistry() {
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException("Could not create ServiceRegistry", e);
		}
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

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public List<Class> getMethodClasses() {
		return getSubClasses(getBioMethodClass());
	}

	public List<Class> getMethodsUsedBy(Operation operation) {
		return getClassesForHaving(operation, "usesMethod");
	}

	@SuppressWarnings("unchecked")
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

	public ServiceDescription getServiceDescription(String hash) {
		HashMap<String, ServiceDescription> myServices = services;
		if (myServices == null) {
			myServices = new HashMap<String, ServiceDescription>();
			for (ServiceDescription service : getServiceDescriptions()) {
				myServices.put(getServiceHash(service), service);
			}
			synchronized (this) {
				services = myServices;
			}
		}
		return myServices.get(hash);
	}

	public Iterable<ServiceDescription> getServiceDescriptions() {
		return getElmoManager().findAll(ServiceDescription.class);
	}

	public String getServiceHash(ServiceDescription service) {
		String serviceLocation = setUtils.firstOf(service
				.getHasServiceDescriptionLocations());
		MessageDigest md;
		byte[] hash;
		try {
			md = MessageDigest.getInstance(HASH_METHOD);
			hash = md.digest(serviceLocation.getBytes(UTF_8));
		} catch (NoSuchAlgorithmException e) {
			logger.error("Can't find " + HASH_METHOD + " algorithm", e);
			throw new RuntimeException("Can't find " + HASH_METHOD
					+ " algorithm", e);
		} catch (UnsupportedEncodingException e) {
			logger.error("Can't find " + UTF_8 + " encoding", e);
			throw new RuntimeException("Can't find " + UTF_8 + " encoding", e);
		}

		BigInteger bi = new BigInteger(hash);
		String hashHex = bi.toString(16);
		return hashHex;
	}

	public List<ServiceDescription> getServicesConsuming(Class type) {
		return getServicesParamPred("inputParameter", "objectType", type);
	}

	public List<ServiceDescription> getServicesConsumingNamespace(Class type) {
		return getServicesParamPred("inputParameter", "inNamespaces", type);
	}

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	public List<Class> getSubClasses(Class parent) {
		String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "SELECT ?class\n" + "WHERE {\n"
				+ " ?class rdfs:subClassOf ?parent\n" + "}";
		ElmoQuery<Class> elmoQuery = (ElmoQuery<Class>) getElmoManager()
				.createQuery(query);
		elmoQuery.setParameter("parent", parent);
		return elmoQuery.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Class> getTaskClasses() {
		String query = "PREFIX service: <http://www.mygrid.org.uk/mygrid-moby-service#>\n"
				+ "SELECT DISTINCT ?taskClass\n"
				+ "WHERE {\n"
				+ " ?oper service:performsTask ?task .\n"
				+ " ?task a ?taskClass \n" + "}";
		ElmoQuery<Class> elmoQuery = (ElmoQuery<Class>) getElmoManager()
				.createQuery(query);
		return elmoQuery.getResultList();
	}

	public List<Class> getTasksPerformedBy(Operation operation) {
		return getClassesForHaving(operation, "performsTask");
	}

	@SuppressWarnings("unchecked")
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

	public void init() throws RepositoryException, RDFParseException,
			IOException {
		dataDir.mkdirs();
		repository = new SailRepository(new MemoryStore(dataDir));
		repository.initialize();
		repConnection = repository.getConnection();
		valueFactory = repository.getValueFactory();
		ElmoModule elmoModule = new ElmoModule();
		factory = new SesameManagerFactory(elmoModule, repository);
		elmoManager = factory.createElmoManager();
		lastUpdated = new Date(dataDir.lastModified());
	}

	public synchronized void updateFeta() throws Exception {
		logger.debug("Updating registry from feta..");
		repConnection.clear();
		String baseURI = "http://www.mygrid.org.uk/feta";
		URI context = valueFactory.createURI(baseURI);
		repConnection.add(getFetaN3(), baseURI, RDFFormat.N3, context);
		services = null;
		lastUpdated = new Date(dataDir.lastModified());
		logger.info("Updated registry from feta");
	}

}
