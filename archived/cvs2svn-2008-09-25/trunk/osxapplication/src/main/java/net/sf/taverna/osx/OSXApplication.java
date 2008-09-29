package net.sf.taverna.osx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * OS X application proxy. By implementing the OSXListener interface (or
 * subclassing the OSXAdapter) and calling setListener(), an application can
 * migrate with OS X to handle OS X specific features such as "About" and "Quit"
 * in the generated Application menu.
 * <p>
 * This is an independant proxy to com.apple.eawt.ApplicationListener to avoid
 * compile problems and class loading problems outside OS X.
 * <p>
 * See <a
 * href="http://developer.apple.com/documentation/Java/Reference/1.5.0/appledoc/api/index.html?com/apple/eawt/ApplicationListener.html">developer.apple.com</a>
 * for details on the original interface as provided by Apple.
 * 
 * @author Stian Soiland
 */
public class OSXApplication {
	
	/**
	 * Register the given listener with the OS X Java backend. If the current
	 * operating system is not OS X, this method will not do anything.
	 * <p>
	 * The OSXApplicationListener class and its dependencies are loaded
	 * dynamically to avoid compile- and linkage problems on non-OSX platforms.
	 * 
	 * @param listener
	 *            Listener that will be callbacked from the OS X Java
	 *            implementation.
	 */
	public static void setListener(OSXListener listener) {
		String os = System.getProperty("os.name").toLowerCase();
		if (! os.startsWith("mac os x")) {
			return;
		}
		ClassLoader cl = OSXApplication.class.getClassLoader();
		if (cl == null) {
			cl = Thread.currentThread().getContextClassLoader();
		}
		try {
			Class appListener = cl.loadClass("net.sf.taverna.osx.OSXApplicationListener");
			Method register = appListener.getMethod("register", OSXListener.class);
			register.invoke(null, new Object[] { listener });
			//System.out.println("Registered!");
	
		// Ignore all errors.. 
		// TODO: log exceptions (which logger?)
		} catch (ClassNotFoundException e) {
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (NullPointerException e) {
		} catch (InvocationTargetException e) {
		}
	}
}
