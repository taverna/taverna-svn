package net.sf.taverna.service.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

public class JavaProcess {

	private static Logger logger = Logger.getLogger(JavaProcess.class);

	private static final String BIN = "bin";
	
	private static String classpathToString(URL[] classpath) {
		StringBuffer sb = new StringBuffer();
		for (URL url : classpath) {
			sb.append(url);
			sb.append(';');
		}
		if (sb.length() > 0) {
			// Delete last ;
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}

	private String classpath;
	private String className;
	
	public JavaProcess(URL[] classpath, String className) {
		this(classpathToString(classpath), className);
	}
	
	
	public JavaProcess(String classpath, String className) {
		this.classpath = classpath;
		this.className = className;
	}
	
	List<String> javaArgs = new ArrayList<String>();
	
	List<String> args = new ArrayList<String>();

	private boolean redirectError = true;
	
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
		
		cmdLine.add("-Xmx256m");

		cmdLine.add("-classpath");
		cmdLine.add(classpath);
		
		cmdLine.addAll(javaArgs);
		
		cmdLine.add(className);
		
		cmdLine.addAll(args);
		
		return cmdLine;
	}
	
	public Process run() {
		List<String> cmdLine = buildArguments();
		ProcessBuilder procBuilder = new ProcessBuilder(cmdLine);
		procBuilder.redirectErrorStream(redirectError);
		System.out.println("Starting " + this);				
		try {
			return procBuilder.start();
		} catch (IOException e) {
			logger.warn("Could not execute: " + cmdLine, e);
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
	public boolean isRedirectError() {
		return redirectError;
	}


	/**
	 * @param redirectError the redirectError to set
	 */
	public void setRedirectError(boolean redirectError) {
		this.redirectError = redirectError;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		args = buildArguments();
		for (String arg: args) {
			if (arg.matches("^[-A-Za-z0-9_/=.]*$")) {
				sb.append(arg);
			} else {
				sb.append('"');
				sb.append(arg.replace("\"", "\\\""));
				sb.append('"');
			}
			sb.append(" ");
		}
		
		return sb.toString();
	}
	
}
