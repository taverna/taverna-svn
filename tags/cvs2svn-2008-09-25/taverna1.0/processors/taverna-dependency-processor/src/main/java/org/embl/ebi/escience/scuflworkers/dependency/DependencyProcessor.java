package org.embl.ebi.escience.scuflworkers.dependency;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.tools.BootstrapClassLoader;
import net.sf.taverna.utils.MyGridConfiguration;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * A processor that needs dependency management. Typical subclasses are
 * {@link org.embl.ebi.escience.scuflworkers.apiconsumer.BeanshellProcessor} and
 * {@link org.embl.ebi.escience.scuflworkers.beanshell.APIConsumerProcessor}.
 * This abstract Processor defines dependencies on local JAR files or Raven
 * artefacts.
 * 
 * @author Stian Soiland
 */
public abstract class DependencyProcessor extends Processor {

	private static Logger logger = Logger.getLogger(DependencyProcessor.class);

	public DependencyProcessor(ScuflModel model, String name)
		throws ProcessorCreationException, DuplicateProcessorNameException {
		super(model, name);
	}

	/**
	 * Different ways to share class loader.
	 * <dl>
	 * <dt>fresh</dt>
	 * <dd>Always new class loader</dd>
	 * <dt>iteration</dt>
	 * <dd>Same classloader within iteration of a processor</dd>
	 * <dt>workflow</dt>
	 * <dd>Same classloader for all processors defining <code>workflow</code></dd>
	 * <dt>system</dt>
	 * <dd>System classloader</dd>
	 * </dl>
	 * 
	 * @see DependencyProcessor#setClassLoaderSharing(org.embl.ebi.escience.scuflworkers.dependency.DependencyProcessor.ClassLoaderSharing)}
	 */
	public enum ClassLoaderSharing {
		fresh, iteration, workflow, system
	}
	
	/**
	 * Current classloader sharing policy, by default {@link ClassLoaderSharing#iteration}.
	 */
	private ClassLoaderSharing classLoaderSharing =
		ClassLoaderSharing.iteration;

	/**
	 * Where to find local JAR files. By default this is the directory 
	 * <code>lib</code> in <code>.taverna</code>
	 * 
	 */
	File libDir = MyGridConfiguration.getUserDir("lib");

	/**
	 * Local dependencies, ie. filenames of JARs. The files should be present in
	 * {@link #libDir}, and the paths should be relative.
	 */
	public final LinkedHashSet<String> localDependencies = new LinkedHashSet<String>();

	/**
	 * Artifact dependencies. These artifacts should be available from
	 * the centrally known repositories or from one of the listed repositories in
	 * repositories.
	 */
	public final LinkedHashSet<BasicArtifact> artifactDependencies = new LinkedHashSet<BasicArtifact>();

	/**
	 * Repositories to use when searching for artifacts. In addition, the system
	 * repositories will be searched.
	 */
	public final LinkedHashSet<URL> repositories = new LinkedHashSet<URL>();

	/**
	 * For persisting class loaders across iterations. Note that since each
	 * execution runs from a different ScuflModel (copied through XML), the keys
	 * will be unique per run. That's also why we need to do this with
	 * WeakHashMap so we don't keep up references to old processors (and thereby
	 * old ScuflModels)
	 */
	static WeakHashMap<DependencyProcessor, ClassLoader> iterationClassLoaders =
		new WeakHashMap<DependencyProcessor, ClassLoader>();

	/**
	 * For persisting class loaders across a whole workflow run. See note for
	 * {@link #iterationClassLoaders} on why this will still be unique per
	 * workflow run.
	 */
	static WeakHashMap<ScuflModel, ClassLoader> workflowClassLoaders =
		new WeakHashMap<ScuflModel, ClassLoader>();

	/**
	 * Find and possibly build the classloader. The classloader depends on the
	 * current classloader sharing policy as defined by
	 * {@link #setClassLoaderSharing(org.embl.ebi.escience.scuflworkers.dependency.DependencyProcessor.ClassLoaderSharing)},
	 * and local dependencies as listed in {@link #localDependencies}.
	 * <p>
	 * If the classloader sharing is {@link ClassLoaderSharing#fresh}, a new
	 * classloader solely depending on the current {@link #localDependencies} is
	 * returned. This policy would in effect reload classes for each iteration,
	 * so they would always be "fresh".
	 * <p>
	 * If the classloader sharing is {@link ClassLoaderSharing#iteration}, the
	 * classloader will be built as for {@link ClassLoaderSharing#fresh}, but
	 * only once per {@link DependencyProcessor} instance. In reality this would
	 * for Taverna mean once per processor per workflow run, as processors are
	 * built from scratch on execution. This policy would avoid reloading
	 * classes for each iteration, but each processor instance would still have
	 * different classloaders.
	 * <p>
	 * If the classloader sharing is {@link ClassLoaderSharing#workflow}, a
	 * common classloader will be used for the whole workflow for all processors
	 * with the same policy. The dependencies will be constructed as the union
	 * of all {@link #localDependencies} sets at the point of the first call to
	 * findClassLoader().
 	 * <p>
	 * All of these classloaders will have as a parent the loader of this
	 * instance (ie. the loader of the deepest subclass of
	 * {@link DependencyProcessor}).
	 * <p>
	 * However, if the classloader sharing is {@link ClassLoaderSharing#system}, the
	 * system classloader will be used. Note that dependencies are ignored and
	 * must be specified by <code>-classpath</code> when starting the Java.
	 * This is useful in combination with JNI based libraries, which would also
	 * require <code>-Djava.library.path</code> and possibly the operating
	 * system's PATH / LD_LIBRARY_PATH / DYLD_LIBRARY_PATH environment variable.
	 * 
	 * @return A new or existing {@link ClassLoader} according to the
	 *         classloader sharing policy
	 */
	public ClassLoader findClassLoader() {
		if (classLoaderSharing == ClassLoaderSharing.fresh) {
			return makeClassLoader();
		}
		if (classLoaderSharing == ClassLoaderSharing.iteration) {
			synchronized (iterationClassLoaders) {
				ClassLoader cl = iterationClassLoaders.get(this);
				if (cl == null) {
					cl = makeClassLoader();
					iterationClassLoaders.put(this, cl);
				}
				return cl;
			}
		}
		if (classLoaderSharing == ClassLoaderSharing.workflow) {
			synchronized (workflowClassLoaders) {
				ClassLoader cl = workflowClassLoaders.get(getModel());
				if (cl == null) {
					cl = makeClassLoader();
					workflowClassLoaders.put(getModel(), cl);
				}
				return cl;
			}
		}
		if (classLoaderSharing == ClassLoaderSharing.system) {
			ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
			if (sysClassLoader instanceof BootstrapClassLoader) {
				updateBootstrapClassLoader((BootstrapClassLoader) sysClassLoader);				
			} else {
				logger.warn("System classloader is not a BootstrapClassLoader, dependencies must be defined with -classpath");
			}
			return sysClassLoader;
		}
		logger.error("Unknown classLoaderSharing: " + classLoaderSharing);
		throw new RuntimeException("Unknown classLoaderSharing");
	}

	/**
	 * Add any new dependencies identified by {@link #findDependencies()} to the
	 * {@link BootstrapClassLoader} system classloader.
	 * 
	 * @param loader The current system ClassLoader
	 */
	private void updateBootstrapClassLoader(BootstrapClassLoader loader) {
		List<URL> deps = findDependencies();
		Set<URL> exists = new HashSet<URL>(Arrays.asList(loader.getURLs()));
		for (URL dep : deps) {
			if (exists.contains(dep)) {
				continue;
			}
			logger.info("Registering with system classloader:" + dep);
			loader.addURL(dep);
			exists.add(dep);
		}
	}

	/**
	 * Construct a new classloader according to the current classloader sharing
	 * policy.
	 * <p>
	 * If the current {@link ClassLoaderSharing} is
	 * {@link ClassLoaderSharing#workflow}, this will build a list of
	 * {@link #localDependencies}Dep as a union from all
	 * {@link DependencyProcessor}s in the containing workflow.
	 * <p>
	 * For all other policies, the classloader will use
	 * {@link #localDependencies} of this processor only.
	 * <p>
	 * The returned class loader will use the found list of dependencies as its
	 * classpath.
	 * 
	 * @return A {@link ClassLoader} capable of accessing the dependencies
	 */
	ClassLoader makeClassLoader() {
		ClassLoader parent = this.getClass().getClassLoader();
		// All sharing policies include at least our own dependencies
		List<URL> urls = findDependencies();
		return new URLClassLoader(urls.toArray(new URL[0]), parent) {
			@Override
			protected String findLibrary(String libname) {
				String filename = System.mapLibraryName(libname);
				File libraryFile = new File(libDir, filename);
				if (libraryFile.isFile()) {
					logger.info("Found library " + libname + ": " + libraryFile.getAbsolutePath());
					return libraryFile.getAbsolutePath();
				}
				return super.findLibrary(libname);
			}
		};
	}

	private List<URL> findDependencies() {
		Set<String> locals = new HashSet<String>(localDependencies);
		if (classLoaderSharing == ClassLoaderSharing.workflow || classLoaderSharing == ClassLoaderSharing.system) {
			// We'll merge inn all dependencies from all processors claiming to
			// be workflow-sharing.
			
			// FIXME: This does not include processors from nested workflows (or
			// we might be inside a nested workflow)
			for (Processor p : getModel().getProcessorsOfType(
				DependencyProcessor.class)) {
				DependencyProcessor proc = (DependencyProcessor) p;
				if (proc == this) {
					// Not really neccessary to skip since locals is a set
					continue;
				}
				if (proc.getClassLoaderSharing() != ClassLoaderSharing.workflow) {
					continue;
				}
				locals.addAll(proc.localDependencies);
			}
		}
		List<URL> urls = new ArrayList<URL>();
		for (String jar : locals) {
			try {
				urls.add(new File(libDir, jar).toURI().toURL());
			} catch (MalformedURLException e) {
				logger.warn("Invalid URL for " + jar, e);
				continue;
			}
		}
		return urls;
	}

	/**
	 * Set the classloader sharing to the given policy. This policy affects how
	 * {@link #findClassLoader()} determines, builds and/or reuses classloaders.
	 * <p>
	 * By default, the classloader sharing is set to
	 * {@link ClassLoaderSharing#iteration}.
	 * 
	 * @see ClassLoaderSharing
	 * @see #findClassLoader()
	 * @param sharing
	 */
	public void setClassLoaderSharing(ClassLoaderSharing sharing) {
		classLoaderSharing = sharing;
	}

	/**
	 * Return the current classloader sharing policy as used by
	 * {@link #findClassLoader()}.
	 * 
	 * @return A {@link ClassLoaderSharing} policy.
	 */
	public ClassLoaderSharing getClassLoaderSharing() {
		return classLoaderSharing;
	}

}
