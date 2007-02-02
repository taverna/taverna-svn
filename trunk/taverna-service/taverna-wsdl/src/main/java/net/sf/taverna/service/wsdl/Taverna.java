package net.sf.taverna.service.wsdl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.tools.Bootstrap;

public class Taverna {
	
	private static File tavernaHome = null;
	private String loaderVersion = "1.5.1";
	private static Class targetClass;
	private static Object targetObject = null;

	public Taverna() {
		synchronized (this) {
			if (tavernaHome == null || targetClass == null || targetObject == null) {
				prepare();
			} else {
				System.out.println("Already prepared");
			}
		}
	}
	public void reset() {
		prepare();
	}
	
	public String runWorkflow(String scufl, String inputDoc) {
		return call("runWorkflow", scufl, inputDoc);
	}
	
	public String runWorkflowFile(String filename, String inputDoc) {
		return call("runWorkflowFile", filename, inputDoc);
	}
	
	public String jobStatus(String job_id) {
		return call("jobStatus", job_id);
	}
	
	public String getResultDocument(String job_id) {
		return call("getResultDocument", job_id);
	}

	public String getProgressReport(String job_id) {
		return call("getProgressReport", job_id);
	}
	
	public String jobs() {
		return call("jobs");
	}

	public String call(String method, String msg) {
		System.out.println("Calling method " + method);
		try {
			return (String) invoke(method, msg);
		} catch (Throwable t) {
			System.out.println("Oh noe!");
			t.printStackTrace();
			return "Error: " + t;
		}
	}
	private String call(String method, String... msg) {
		System.out.println("Calling method " + method);
		try {
			return (String) invoke(method, (Object[])msg);
		} catch (Throwable t) {
			System.out.println("Oh noe!");
			t.printStackTrace();
			return "Error: " + t;
		}
	}
	private Object invoke(String methodName, Object... args)
	throws NoSuchMethodException, Throwable {
		// Find classes to match our method
		Class[] classes = new Class[args.length];
		for (int i=0; i<args.length; i++) {
			classes[i] = args[i].getClass();
		}
		try {
			// Try m(args) first
			Method method = targetClass.getMethod(methodName, 
				classes);
			// Note: Invokes static if targetObject==null
			return method.invoke(targetObject, args);				
		} catch (NoSuchMethodException ex) {
			System.err.println("Could not find method " + methodName);
			throw ex;
		} catch (InvocationTargetException ex) {
			System.err.println("Exception occured in " + methodName);
			throw ex.getCause();
		}
}
	
	private synchronized void prepare() {
			try {
				tavernaHome = File.createTempFile("taverna", "home");
				tavernaHome.delete();
				tavernaHome.mkdir();
			} catch (IOException e) {
				e.printStackTrace();
				throw new NullPointerException("Can't make taverna.home");
			}
			
	
			System.err.println("Using fresh taverna.home " + tavernaHome);
			System.setProperty("taverna.home", tavernaHome.getAbsolutePath());
			//System.setProperty("java.awt.headless", "true");
			
	//		System.setProperty("raven.target.groupid", "uk.org.mygrid.taverna.scufl");
	//		System.setProperty("raven.target.artifactid", "scufl-tools");
	//		System.setProperty("raven.target.version", loaderVersion);
	//		System.setProperty("raven.target.class", "org.embl.ebi.escience.scufl.tools.WorkflowLauncher");
	//		System.setProperty("raven.target.method", "main");
			
			System.setProperty("raven.target.groupid", "uk.org.mygrid.tavernaservice");
			System.setProperty("raven.target.artifactid", "taverna-engine");
			System.setProperty("raven.target.version", "1.0.0");
			System.setProperty("raven.target.class", "net.sf.taverna.service.backend.Engine");
			
			System.setProperty("raven.loader.version", "1.5.1");
			// FIXME: /tmp/tavernaXhome/conf/taverna-service-profile.xml is still
			//  version="1.5.1.0" name="Taverna Workbench"
			System.setProperty("raven.remoteprofile", 
			"http://rpc268.cs.man.ac.uk/profiles/taverna-service-profile.xml");
			File m2Repo = new File(System.getProperty("user.home"), ".m2/repository");
			if (m2Repo.isDirectory()) {
				System.setProperty("raven.repository.1", m2Repo.toURI().toString());
			} else {
				System.err.println("Could not find " + m2Repo);
			}
			
//			System.setProperty("raven.repository.2", 
//				"http://www.mygrid.org.uk/maven/repository/");
			
			try {
				bootstrap();
			} catch (Throwable t) {
				System.err.println("Could not bootstrap!");
				t.printStackTrace();
			}
		}
	private synchronized void bootstrap() throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
		Bootstrap.findUserDir();
		Bootstrap.properties = Bootstrap.findProperties();
		Bootstrap.remoteRepositories = Bootstrap.findRepositories(Bootstrap.properties);
		if (Bootstrap.properties.getProperty("raven.remoteprofile") != null) {
			Bootstrap.initialiseProfile(Bootstrap.properties.getProperty("raven.remoteprofile"));
		}
		List<URL> localLoaderUrls = new ArrayList<URL>();
		List<URL> remoteLoaderUrls = new ArrayList<URL>();
		Bootstrap.getLoaderUrls(localLoaderUrls,remoteLoaderUrls);
		Bootstrap.addSystemLoaderArtifacts();
		
		Method loaderMethod = Bootstrap.createLoaderMethod(localLoaderUrls,remoteLoaderUrls);
		targetClass = Bootstrap.createWorkbenchClass(loaderVersion, loaderMethod);
		
		try {
			targetObject = null;
			targetObject = invoke("getInstance");
			System.out.println("Found object " + targetObject);
		} catch (NoSuchMethodException e) {
			System.err.println("Could not find getInstance(), static access only");
		} catch (Throwable e) {
			System.err.println("Could not call getInstance(), static access only");
			e.printStackTrace();
		}
	}
}
