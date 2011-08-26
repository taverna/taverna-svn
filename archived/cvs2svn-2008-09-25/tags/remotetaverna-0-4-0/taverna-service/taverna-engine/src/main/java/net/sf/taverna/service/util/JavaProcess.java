package net.sf.taverna.service.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Execute a Java main() method in a new JVM as a new process. In particular
 * this gives the caller the possibillity to kill the process after a timeout
 * without worrying about how many threads the class forks out, and this class
 * will do the magic needed to find the "java" binary.
 * <p>
 * The new JVM can optionally inherit the classpath of the current JVM (guessed
 * using various heuristics) by construction with
 * {@link JavaProcess#JavaProcess(String, ClassLoader)}
 * 
 * @author Stian Soiland
 */
public class JavaProcess {

	private static Logger logger = Logger.getLogger(JavaProcess.class);

	private static final String BIN = "bin";

	
	private String classpath;

	private String className;
	
	private String allocatedMemory;

	private ClassLoader callingClassLoader;

	/**
	 * Create a new {@link JavaProcess} inheriting the classpath of the caller.
	 * The classpath will be built from the provided parent classloader. Call
	 * this constructor as: <code>
	 * 	new JavaProcess("com.example.Application", getClass().getClassLoader());
	 * </code>
	 * (The code will handle a <code>null</code> classloader. )
	 * 
	 * @see #setInherittingClasspath(boolean)
	 * @param className
	 *            The class which main(String[]) method to invoke in a new
	 *            process.
	 * @param callingClassLoader
	 *            The caller's classloader from where to extract the classpath.
	 * @param allocatedMemory The maximum memory to be allocated to this process. In megabytes.
	 */
	public JavaProcess(String className, ClassLoader callingClassLoader,String allocatedMemory) {
		this(new URL[0], className,allocatedMemory);
		setInherittingClasspath(true);
		this.callingClassLoader = callingClassLoader;
	}

	public JavaProcess(URL[] classpath, String className, String allocatedMemory) {
		this(classpathToString(classpath), className);
		this.allocatedMemory=allocatedMemory;
	}

	private JavaProcess(String classpath, String className) {
		this.classpath = classpath;
		this.className = className;
	}

	List<String> javaArgs = new ArrayList<String>();

	List<String> args = new ArrayList<String>();

	private boolean redirectingError = true;

	private boolean inherittingClasspath = false;

	/**
	 * Find the Java binary, searching java.home
	 * 
	 * @return The full path to the "java" binary, or simply "java" or
	 *         "java.exe" if it could not be found on java.home.
	 */
	public static String findJava() {
		String java = "java";
		if (System.getProperty("os.name").startsWith("Windows")) {
			java = "java.exe";
		}

		File javaHome = new File(System.getProperty("java.home"));
		File bin = new File(javaHome, BIN);
		File javaBin = new File(bin, java);
		if (!javaBin.isFile()) {
			bin = new File(new File(javaHome, ".."), BIN);
			javaBin = new File(bin, java);
		}
		if (!javaBin.isFile()) {
			// Rely on "java" being on the path as a last resort
			return java;
		}
		return javaBin.toString();
	}

	protected List<String> buildArguments() {
		List<String> cmdLine = new ArrayList<String>();

		cmdLine.add(findJava());

		cmdLine.add("-Xmx"+allocatedMemory+"m");

		cmdLine.add("-classpath");
		if (isInherittingClasspath()) {
			cmdLine.add(classpath + System.getProperty("path.separator")
				+ classpathToString(getCurrentClasspath()));
		} else {
			cmdLine.add(classpath);
		}

		cmdLine.addAll(javaArgs);

		cmdLine.add(className);

		cmdLine.addAll(args);

		return cmdLine;
	}

	public Process run() {
		List<String> cmdLine = buildArguments();
		ProcessBuilder procBuilder = new ProcessBuilder(cmdLine);
		procBuilder.redirectErrorStream(redirectingError);
		System.out.println("Starting " + this);
		try {
			return procBuilder.start();
		} catch (IOException e) {
			logger.warn("Could not execute: " + cmdLine, e);
			System.err.println("Could not execute " + cmdLine);
			return null;
		}
	}

	public List<String> getJavaArguments() {
		return javaArgs;
	}

	public List<String> getArguments() {
		return args;
	}

	public void addSystemProperty(String key, String value) {
		javaArgs.add("-D" + key + "=" + value);
	}

	public void addJavaArguments(String... strings) {
		javaArgs.addAll(Arrays.asList(strings));
	}

	public void addArguments(String... strings) {
		args.addAll(Arrays.asList(strings));
	}

	/**
	 * @return the redirectError
	 */
	public boolean isRedirectingError() {
		return redirectingError;
	}

	/**
	 * @param redirectError
	 *            the redirectError to set
	 */
	public void setRedirectingError(boolean redirectError) {
		this.redirectingError = redirectError;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		List<String> args = buildArguments();
		for (String arg : args) {
			if (arg.matches("^[-A-Za-z0-9_/=.]*$")) {
				sb.append(shortened(arg));
			} else {
				sb.append('"');
				sb.append(shortened(arg.replace("\"", "\\\"")));
				sb.append('"');
			}
			sb.append(" ");
		}

		return sb.toString();
	}

	public boolean isInherittingClasspath() {
		return inherittingClasspath;
	}

	/**
	 * Inherit the classpath of the current VM. This uses magic to traverse the
	 * classloaders and extract their URLs to form the classpath of the new
	 * process. The inheritted classpath will be added to the end of the
	 * specified {@link #classpath}.
	 * 
	 * @param inherittingClasspath
	 *            the inherittingClasspath to set
	 */
	public void setInherittingClasspath(boolean inherittingClasspath) {
		this.inherittingClasspath = inherittingClasspath;
	}

	public ClassLoader getCallingClassLoader() {
		return callingClassLoader;
	}

	/**
	 * Set the calling classloader. This is normally
	 * <code>getClass().getClassLoader()</code> in the method that is using
	 * {@link JavaProcess}.
	 * <p>
	 * If {@link #isInherittingClasspath()} is <code>true</code>, and the
	 * calling classloader is an {@link URLClassLoader} (normally the case) the
	 * classpath of that classloader will be included in the classpath of the
	 * new process.
	 * 
	 * @param callingClassLoader
	 *            the callingClassLoader to set
	 */
	public void setCallingClassLoader(ClassLoader callingClassLoader) {
		this.callingClassLoader = callingClassLoader;
	}
	
	private static String classpathToString(URL[] classpath) {
		return classpathToString(Arrays.asList(classpath));
	}

	private LinkedHashSet<URL> getCurrentClasspath() {
		LinkedHashSet<URL> classpath = new LinkedHashSet<URL>();
		LinkedHashSet<ClassLoader> classloaders = new LinkedHashSet<ClassLoader>();
	
		classloaders.add(getCallingClassLoader());
		classloaders.add(getClass().getClassLoader());
		classloaders.add(Thread.currentThread().getContextClassLoader());
		
		// Iterate over copy so we can modify it
		for (ClassLoader cl : new ArrayList<ClassLoader>(classloaders)) {
			if (cl == null) {
				continue;
			}
			// Add all the parents
			cl = cl.getParent();
			// FIXME: Parents are not added immediately after their children
			while (cl != null && ! classloaders.contains(cl)) {
				classloaders.add(cl);
				cl = cl.getParent();
			}				
		}		
		classloaders.add(ClassLoader.getSystemClassLoader());
		
		// Extract URLs
		for (ClassLoader cl : classloaders) {
			if (cl == null || ! (cl instanceof URLClassLoader)) {
				continue;
			}
			if (cl == ClassLoader.getSystemClassLoader()) {
				// Only include stuff in java.class.path instead
				String[] paths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
				for (String path : paths) {
					File pathFile = new File(path).getAbsoluteFile();
					try {
						classpath.add(pathFile.toURI().toURL());
					} catch (MalformedURLException e) {						
						logger.warn("Ignoring invalid path " + path, e);
					}
				}
				continue;
			}
			URLClassLoader urlCL = (URLClassLoader) cl;
			classpath.addAll(Arrays.asList(urlCL.getURLs()));
		}
		return classpath;
	}

	private static String shortened(String string) {
		if (string.length() <= 40) {
			return string;
		}
		return string.substring(0, 19) + "..." + string.substring(string.length() - 19);
	}

	private static String classpathToString(Iterable<URL> urls) {
		String pathSep = System.getProperty("path.separator");
		StringBuffer sb = new StringBuffer();
		for (URL url : urls) {
			if (! url.getProtocol().equals("file")) {
				logger.debug("Ignoring non-file " + url);
				continue;
			}
			String file = url.getFile();
			if (file.contains(pathSep)) {
				logger.debug("Ignoring invalid character in " + url);
				continue;
			}
			if (file.equals("")) {
				// Use current directory instead
				sb.append(System.getProperty("user.dir"));
			} else {
				sb.append(file);
			}
			sb.append(pathSep);
		}
		if (sb.length() > 0) {
			// Delete last ;
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}


}
