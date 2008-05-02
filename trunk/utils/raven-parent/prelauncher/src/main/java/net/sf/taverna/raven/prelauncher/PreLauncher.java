package net.sf.taverna.raven.prelauncher;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class PreLauncher {

	private static final String LAUNCHER_CLASS = "net.sf.taverna.raven.launcher.Launcher";
	private static final String LAUNCHER_MAIN = "main";

	public class JarFilenameFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(".jar");
		}
	}

	public static void main(String[] args) {
		PreLauncher launcher = new PreLauncher();
		int result = launcher.launchArgs(args);
		if (result != 0) {
			System.exit(result);
		}
	}

	public int launchArgs(String[] args) {
		List<URL> classPath;
		try {
			classPath = buildClassPath();
		} catch (IOException e) {
			System.err.println("Could not build classpath");
			e.printStackTrace();
			return -1;
		}

		String method = LAUNCHER_CLASS + "." + LAUNCHER_MAIN + "(String[])";
		try {
			runLauncher(classPath, args);
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid arguments for method " + method);
			e.printStackTrace();
			return -2;
		} catch (ClassNotFoundException e) {
			System.err.println("Could not find class " + LAUNCHER_CLASS);
			e.printStackTrace();
			return -3;
		} catch (IllegalAccessException e) {
			System.err.println("Could not access method " + method);
			e.printStackTrace();
			return -4;
		} catch (InvocationTargetException e) {
			System.err.println("Exception from method " + method);
			e.getCause().printStackTrace();
			return -5;
		} catch (SecurityException e) {
			System.err.println("Not allowed by VM to access method " + method);
			e.printStackTrace();
			return -6;
		} catch (NoSuchMethodException e) {
			System.err.println("Could not find method " + method);
			e.printStackTrace();
			return -7;
		}
		return 0;
	}

	private void runLauncher(List<URL> classPath, String[] args)
			throws ClassNotFoundException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		URLClassLoader classLoader = new URLClassLoader(classPath
				.toArray(new URL[0]), getClass().getClassLoader());
		Class<?> launcherClass = classLoader.loadClass(LAUNCHER_CLASS);
		Method launcherMain = launcherClass.getMethod(LAUNCHER_MAIN,
				String[].class);
		launcherMain.invoke((Object) null, (Object) args);
	}

	protected List<URL> buildClassPath() throws IOException {
		List<URL> classPath = new ArrayList<URL>();
		File libDir = BootstrapLocation.getBootstrapFile(getClass());

		// The directory itself, when run from .class files
		classPath.add(libDir.toURL());

		File[] jarFiles = libDir.listFiles(new JarFilenameFilter());
		if (jarFiles == null) {
			System.err.println("Can't list files of " + libDir);
		} else {
			for (File jarFile : jarFiles) {
				classPath.add(jarFile.toURL());
			}
		}
		return classPath;
	}
}
