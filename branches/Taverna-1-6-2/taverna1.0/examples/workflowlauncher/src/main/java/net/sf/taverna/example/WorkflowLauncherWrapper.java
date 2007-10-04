package net.sf.taverna.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.tools.WorkflowLauncher;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.jdom.JDOMException;

/**
 * <p>
 * A simplified example application demonstrating how to invoke the Taverna API
 * through Raven, without the need to bootstrap the application.
 * </p>
 * <p>
 * Its main point is to demonstrate populating an {@link Repository} instance
 * with the system artifacts and external artifacts required to execute a
 * workflow. Workflow execution is the most popular request, but this technique
 * can also be applied to access other parts of the Taverna API.
 * </p>
 * <p>
 * The system artifacts relate to the artifacts that already exist within the
 * applications classpath. Correctly defining these ensures that the
 * applications classloader is used to create instances of classes contained
 * within these artifacts. This prevents Raven creating instances through its
 * own classloaders leading to ClassCastException or similar errors. These
 * artifacts are defined in
 * {@link WorkflowLauncherWrapper#buildSystemArtifactSet()}
 * </p>
 * <p>
 * Since you necessarily will be accessing parts of Taverna's API, you will need
 * to include the relevant artifacts on your application's classpath to be able
 * to access the classes. If you access other parts of Taverna than this example
 * shows, you will need to add those artifacts to
 * {@link #buildSystemArtifactSet()} as well.
 * </p>
 * <p>
 * For instance, if you are constructing a new workflow using the API, and you
 * need access to a
 * {@link org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor}, you
 * will need to both list taverna-soaplab-processor as a system artifact and
 * include it and its dependencies in your application's classpath.
 * </p>
 * <p>
 * The external artifacts relate to artifacts that exist outside of the
 * application as SPI plugin points, ie. artifacts that are <strong>not</strong>
 * on the application's classpath. For the purpose of this example this is the
 * {@link Processor}s that are needed during workflow execution. These
 * artifacts are defined in
 * {@link WorkflowLauncherWrapper#buildExternalArtifactSet()}. Depending on
 * which parts of Taverna's API you are invoking, you might have to include more
 * artifacts from Taverna's distribution profile.
 * </p>
 * <p>
 * Because these artifacts are not defined in the application they, and their
 * dependencies, need to be downloaded when the application is first run and are
 * downloaded to the local repository location defined by the method
 * {@link WorkflowLauncherWrapper#getRepositoryBaseFile()}, and ultimately the
 * runtime argument <em>-basedir</em> if provided.
 * </p>
 * <p>
 * The external artifacts are downloaded from the remote repository locations
 * defined in {@link WorkflowLauncherWrapper#buildRepositoryLocationSet}.
 * </p>
 * <p>
 * The key to this approach is initialising a {@link Repository} using the API
 * call:
 * </p>
 * 
 * <pre>
 * Repository repository =
 * 	LocalRepository.getRepository(localrepository, applicationClassloader,
 * 		systemArtifacts);
 * </pre>
 * 
 * <p>
 * Then making sure the local repository is up to date with a call:
 * </p>
 * 
 * <pre>
 * repository.update();
 * </pre>
 * 
 * <p>
 * The repository is then registered with the {@link TavernaSPIRegistry} with
 * the call:
 * </p>
 * 
 * <pre>
 * TavernaSPIRegistry.setRepository(repository);
 * </pre>
 * 
 * <p>
 * This all takes place within
 * {@link WorkflowLauncherWrapper#initialiseRepository()}
 * </p>
 * <p>
 * Note that for simplicity in this example the external artifacts and remote
 * repositories have been hardcoded and would normally be better defined
 * separately in a file. For the same reason, exception handling has also been
 * kept to a minimum.
 * </p>
 * <p>
 * There is nothing requiring you to use this mechanism from an
 * {@link #main(String[])} method started from the command line, this is just to
 * make this example self-contained and simple. As long as you include the
 * necessary dependencies this example should be easily ported to be used within
 * a servlet container such as Tomcat.
 * </p>
 * <h4>To use:</h4>
 * <p>
 * First build using maven together with the appassembler plugin:
 * </p>
 * <pre>mvn package appassembler:assemble</pre>
 * <p>
 * Then navigate to the <code>target/appassembler/bin</code> directory
 * and run the <code>runme[.bat]</code> command:
 * </p>
 * <pre>
 * runme [-inputdoc &lt;path to input doc&gt; 
 *        -outputdoc &lt;path to output doc&gt; 
 *        -basedir &lt;path to local repository download dir&gt;] 
 *        -workflow &lt;path to workflow scufl file&gt;.
 * </pre>
 * 
 * @author Stuart Owen
 * @author Stian Soiland
 */
public class WorkflowLauncherWrapper {

	/**
	 * The version of Taverna core components that is used, for instance
	 * <code>1.6.0.0</code>.
	 * <p>
	 * (Note that although processors and plugins might be updated by their
	 * minor-minor version, say a
	 * {@link org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor} can
	 * be in version <code>1.6.0.1</code>, the core components listed by
	 * {@link #buildSystemArtifactSet()} would still be <code>1.6.0.0</code>,
	 * and thus this constant would also be <code>1.6.0.0</code>. You might
	 * have to update use of <code>TAVERNA_BASE_VERSION</code> in
	 * {@link #buildExternalArtifactSet()} if you want a newer version of a
	 * processor or similar.)
	 * <p>
	 * This version has to match the version of the real dependency you have to
	 * Taverna libraries on your classpath (ie. the pom.xml dependencies).
	 */
	public static final String TAVERNA_BASE_VERSION = "1.6-SNAPSHOT";

	String workflowName;

	String inputDocumentName;

	String outputDocumentName;

	String baseDirName;

	private static Logger logger =
		Logger.getLogger(WorkflowLauncherWrapper.class);

	public static void main(String[] args) {
		try {
			new WorkflowLauncherWrapper().run(args);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * The execution entry point, called by {@link #main(String[])}
	 * 
	 * @param args
	 *            the arguments as passed to {@link #main(String[])}
	 * @throws Exception if anything goes wrong
	 */
	public void run(String[] args) throws Exception {
		processArgs(args);
		if (workflowName == null) {
			logger.error("You must specify a workflow with the argument -workflow");
			logger.error("e.g. runme.bat -workflow C:/myworkflow.xml");
			System.exit(2);
		}

		InputStream workflowInputStream = getWorkflowInputStream();

		Map<String, DataThing> inputs = loadInputDocument();
		if (inputs == null) {
			inputs = new HashMap<String, DataThing>();
		}
			
		Repository repository = initialiseRepository();
		TavernaSPIRegistry.setRepository(repository);

		WorkflowLauncher launcher = new WorkflowLauncher(workflowInputStream);

		Map<String, DataThing> outputs = launcher.execute(inputs);
		if (outputDocumentName == null) {
			logger.warn("No -outputdoc defined to save results. " + 
				"Results returned contained "
				+ outputs.size() + " outputs.");
		} else {
			saveOutputDocument(outputs);
		}

	}

	/**
	 * Open an input stream to the workflow defined by the value of the
	 * argument <code>-workflow</code>
	 * 
	 * @return The workflow {@link InputStream}
	 * @throws FileNotFoundException
	 *             if the <code>-workflow</code> argument does not represent a valid path to
	 *             an existing file.
	 */
	protected InputStream getWorkflowInputStream() throws FileNotFoundException {
		return new FileInputStream(new File(workflowName));
	}

	/**
	 * <p>
	 * Provide the required initialisation of a {@link Repository} instance for
	 * executing a workflow.
	 * </p>
	 * <p>
	 * This involves first defining the artifacts that exist within the
	 * application classpath, external plugin artifacts required during workflow
	 * execution (processors) and a location to download these artifacts to.</p>
	 * <p>
	 * Based upon this information the repository is updated, causing any
	 * external artifacts to be downloaded to the local repository location
	 * defined by <code>-basedir</code> if required.
	 * 
	 * @return The initialised {@link Repository} instance
	 * @throws IOException
	 */
	protected Repository initialiseRepository() throws IOException {

		// these lines are necessary if working with Taverna 1.5.2 or earlier:
		
		// System.setProperty("raven.profile",
		//   "http://www.mygrid.org.uk/taverna/updates/1.5.2/taverna-1.5.2.1-profile.xml");
		// Bootstrap.properties = new Properties();

		Set<Artifact> systemArtifacts = buildSystemArtifactSet();
		Set<Artifact> externalArtifacts = buildExternalArtifactSet();
		List<URL> repositoryLocations = buildRepositoryLocationSet();

		File base = getRepositoryBaseFile();
		ClassLoader myLoader = getClass().getClassLoader();
		if (myLoader == null) {
			myLoader = ClassLoader.getSystemClassLoader();
		}
		Repository repository =
			LocalRepository.getRepository(base,
				myLoader, systemArtifacts);
		for (Artifact artifact : externalArtifacts) {
			repository.addArtifact(artifact);
		}

		for (URL location : repositoryLocations) {
			repository.addRemoteRepository(location);
		}

		repository.update();
		return repository;
	}

	/**
	 * Provide an ordered list of {@link URL}s to public Maven 2 repositories
	 * containing required artifacts. This should contain at a minimum:
	 * <ul>
	 * <li>http://www.mygrid.org.uk/maven/repository/ - the myGrid artifact
	 * repository</li>
	 * <li>http://mobycentral.icapture.ubc.ca/maven/ - Biomoby specific
	 * artifacts</li>
	 * <li>http://www.ibiblio.org/maven2/ - the central Maven repository and/or
	 * any mirrors</li>
	 * </ul>
	 * <p>
	 * The repositories will be searched in order.
	 * </p>
	 * <p>
	 * Although the URLs are hard-coded in this example, it is advisable to
	 * store these in a separate file in a real application.
	 * 
	 * @return A {@link List} containing the list of URL locations
	 * @throws MalformedURLException if the programmer entered an invalid URL :-)
	 */
	protected List<URL> buildRepositoryLocationSet()
		throws MalformedURLException {
		List<URL> result = new ArrayList<URL>();
		
		// Guess local Maven2 repository is in ~/.m2/repository
		File home = new File(System.getProperty("user.home"));
		File m2Repository = new File(new File(home, ".m2"), "repository");
		if (m2Repository.isDirectory()) {
			// This is useful for developers
			logger.debug("Including local maven repository " + m2Repository);
			result.add(m2Repository.toURI().toURL());
		}
		
		result.add(new URL("http://www.mygrid.org.uk/maven/repository/"));
		result.add(new URL("http://mirrors.sunsite.dk/maven2/"));
		result.add(new URL("http://www.ibiblio.org/maven2/"));
		result.add(new URL("http://mobycentral.icapture.ubc.ca/maven/"));
		result.add(new URL(
			"http://www.mygrid.org.uk/maven/snapshot-repository/"));
		return result;
	}

	/**
	 * <p>
	 * Provide a set of {@link Artifact}s (normally {@link BasicArtifact}
	 * instances) defining the artifacts (ie. JAR files) whose classes and
	 * dependencies also exist within the applications classpath.
	 * </p>
	 * <p>
	 * These are used to let Raven know that these classes already exist and
	 * prevents it creating duplicate classes from its own classloaders leading
	 * to a potential ClassCastException or similar.
	 * 
	 * @return Set<Artifact> containing the list of system {@link Artifact}s
	 */
	protected Set<Artifact> buildSystemArtifactSet() {
		
		Set<Artifact> systemArtifacts = new HashSet<Artifact>();
		systemArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna",
			"taverna-core", TAVERNA_BASE_VERSION));
		systemArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna",
			"taverna-enactor", TAVERNA_BASE_VERSION));
		systemArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna",
			"taverna-tools", TAVERNA_BASE_VERSION));

		systemArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna.baclava",
			"baclava-core", TAVERNA_BASE_VERSION));
		systemArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna.baclava",
			"baclava-tools", TAVERNA_BASE_VERSION));

		systemArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna.scufl",
			"scufl-core", TAVERNA_BASE_VERSION));
		systemArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna.scufl",
			"scufl-model", TAVERNA_BASE_VERSION));
		systemArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna.scufl",
			"scufl-tools", TAVERNA_BASE_VERSION));
		systemArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna.scufl",
			"scufl-workflow", TAVERNA_BASE_VERSION));
		
		return systemArtifacts;
	}

	/**
	 * <p>
	 * Provide additional artifacts that are external SPI plugins and won't
	 * already exist on the classpath. This can be compared to the Taverna
	 * distributions' profile to specify which components should be dynamically
	 * loaded.
	 * </p>
	 * <p>
	 * In this example, the application executes a workflow, and therefore
	 * define all required {@link Processor} types. These artifacts, and their
	 * dependencies, will be downloaded from the
	 * {@link #buildRepositoryLocationSet()} to {@link #getRepositoryBaseFile()}
	 * when the application is first run.
	 * </p>
	 * <p>
	 * Although hard-coded for this example it would be advisable to define
	 * these artifacts in an external file.
	 * </p>
	 * 
	 * @return Set<Artifact> containing the list of external artifacts.
	 */
	protected Set<Artifact> buildExternalArtifactSet() {
		Set<Artifact> externalArtifacts = new HashSet<Artifact>();

		String groupId = "uk.org.mygrid.taverna.processors";
		
		externalArtifacts.add(new BasicArtifact(groupId,
			"taverna-beanshell-processor", TAVERNA_BASE_VERSION));

		externalArtifacts.add(new BasicArtifact(groupId,
			"taverna-biomart-processor", TAVERNA_BASE_VERSION));

		externalArtifacts.add(new BasicArtifact("biomoby.org",
			"taverna-biomoby", TAVERNA_BASE_VERSION));

		externalArtifacts.add(new BasicArtifact("uk.org.mygrid.taverna",
			"taverna-contrib", TAVERNA_BASE_VERSION));

		externalArtifacts.add(new BasicArtifact(groupId,
			"taverna-java-processor", TAVERNA_BASE_VERSION));

		externalArtifacts.add(new BasicArtifact(groupId,
			"taverna-localworkers", TAVERNA_BASE_VERSION));

		externalArtifacts.add(new BasicArtifact(groupId,
			"taverna-notification-processor", TAVERNA_BASE_VERSION));

		externalArtifacts.add(new BasicArtifact(groupId,
			"taverna-soaplab-processor", TAVERNA_BASE_VERSION));

		externalArtifacts.add(new BasicArtifact(groupId,
			"taverna-stringconstant-processor", TAVERNA_BASE_VERSION));

		externalArtifacts.add(new BasicArtifact(groupId,
			"taverna-wsdl-processor", TAVERNA_BASE_VERSION));
		
		return externalArtifacts;
	}

	/**
	 * <p>
	 * Provide a {@link File} representation of the local repository directory
	 * that external artifacts and their dependencies will be downloaded to.</p>
	 * <p>
	 * This is defined by the argument <code>-basedir</code>. If this is not
	 * defined a temporary directory is created.</p>
	 * 
	 * @return a {@link File} representation
	 * @throws IOException if the base directory could not be accessed
	 */
	protected File getRepositoryBaseFile() throws IOException {
		if (baseDirName != null) {
			return new File(baseDirName);
		} else {
			File temp = File.createTempFile("repository", "");
			temp.delete();
			temp.mkdir();
			logger.warn("No -basedir defined, so using temporary location of:"
				+ temp.getAbsolutePath());
			return temp;
		}
	}

	/**
	 * Load an XML input document, if defined by the argument
	 * <code>-inputdoc</code>.
	 * 
	 * @return The {@link Map} of input {@link DataThing}s, or
	 *         <code>null</code> if <code>-inputdoc</code> was not specified
	 * @throws FileNotFoundException If the input document can't be found
	 * @throws JDOMException If the input document is invalid XML
	 * @throws IOException If the input document can't be read
	 */
	protected Map<String, DataThing> loadInputDocument() throws FileNotFoundException,
		JDOMException, IOException {
		if (inputDocumentName == null) {
			return null;
		}
		File file = new File(inputDocumentName);
		return WorkflowLauncher.loadInputDoc(file);
	}

	/**
	 * Save an XML output document for the results of running the workflow to
	 * the location defined by <code>-outputdoc</code>, if specified.
	 * 
	 * @param outputs The {@link Map} of results to be saved
	 * @throws IOException If the results could not be saved
	 */
	protected void saveOutputDocument(Map<String, DataThing> outputs) throws IOException {
		if (outputDocumentName != null) {
			File file = new File(outputDocumentName);
			WorkflowLauncher.saveOutputDoc(outputs, file);
			System.out.println(file.getAbsolutePath());
		}
	}

	/**
	 * Process command line argument and set attributes for input/output
	 * document, basedir and workflow.
	 * 
	 * @param args The list of arguments from {@link #main(String[])}
	 */
	private void processArgs(String[] args) {
		// TODO: Use org.apache.commons.cli instead of manual parsing
		for (int i = 0; i < args.length; i += 2) {
			boolean handled = false;
			String param = args[i];
			String value = args[i + 1];
			if (param.equals("-workflow")) {
				workflowName = value;
				handled = true;
			}
			if (param.equals("-inputdoc")) {
				inputDocumentName = value;
				handled = true;
			}
			if (param.equals("-outputdoc")) {
				outputDocumentName = value;
				handled = true;
			}
			if (param.equals("-basedir")) {
				baseDirName = value;
				handled = true;
			}
			if (!handled) {
				logger.error("Unrecognised argument:" + param + " with value:"
					+ value);
			}
		}
	}

}
