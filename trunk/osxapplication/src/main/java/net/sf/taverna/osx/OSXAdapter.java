package net.sf.taverna.osx;

/**
 * OS X application listener interface. By subclassing this adapter and
 * registering with OSXApplication.setListener(), an application can migrate
 * with OS X to handle OS X specific features such as "About" and "Quit" in the
 * generated Application menu.
 * <p>
 * This is an independant proxy to com.apple.eawt.ApplicationAdapter to avoid
 * compile problems and class loading problems outside OS X.
 * <p>
 * Note that the default for all handlers is false, which means the default OS X
 * behaviour will be used. hasAbout() return true, which mean the OS X default
 * About dialogue will be enabled, while hasPreferences() return false, meaning
 * that "Preferences" is not shown in the application menu.
 * <p>
 * See <a
 * href="http://developer.apple.com/documentation/Java/Reference/1.5.0/appledoc/api/index.html?com/apple/eawt/ApplicationAdapter.html">developer.apple.com</a>
 * for details on the original interface as provided by Apple.
 * 
 * @author Stian Soiland
 */
public class OSXAdapter implements OSXListener {

	public boolean handleAbout() {
		return false;
	}

	public boolean handleOpenApplication() {
		return false;
	}

	public boolean handleOpenFile(String filename) {
		return false;
	}

	public boolean handlePreferences() {
		return false;
	}

	public boolean handlePrintFile(String filename) {
		return false;
	}

	public boolean handleQuit() {
		return false;
	}

	public boolean handleReOpenApplication() {
		return false;
	}

	public boolean hasAbout() {
		// Show OS X generated About by default
		return true;
	}

	public boolean hasPreferences() {
		return false;
	}

}
