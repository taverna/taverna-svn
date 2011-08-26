package net.sf.taverna.osx;

/**
 * OS X application listener interface. By implementing this interface and
 * registering with OSXApplication.setListener(), an application can migrate
 * with OS X to handle OS X specific features such as "About" and "Quit" in the
 * generated Application menu.
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
public interface OSXListener {

	/**
	 * If the application should have a "Preferences" men item.
	 * handlePreferences() will be called if the menu item is selected. By
	 * default OS X don't provide a Preferences implementation, so this should
	 * be false unless handlePreferences() is implemented.
	 * 
	 * @see handlePreferences()
	 * @return true if the "Preferences" menu item should be shown
	 */
	boolean hasPreferences();

	/**
	 * If the application should have a "About" menu item. handleAbout() will be
	 * called if the menu item is selected. OS X provdides a default About
	 * implementation, so this should be true even if handleAbout() is not
	 * implemented.
	 * 
	 * @see handleAbout()
	 * @return true if the "About" menu item should be shown
	 */
	boolean hasAbout();

	/**
	 * Called when the user selects the "About" menu item from the application
	 * menu. This menu item will only appear if hasAbout() returned true. By
	 * implementing this method and returning true, the application can show its
	 * own About dialogue window. If the method returns false, OS X will provide
	 * a default About window.
	 * 
	 * @see hasAbout()
	 * @return true if the application handled the event, false if OS X is to
	 *         show the default About dialogue.
	 */
	boolean handleAbout();

	/**
	 * Called when the user opens the application by double-clicking the icon
	 * from the Finder.
	 * 
	 * @return true if the application handled the event.
	 */
	boolean handleOpenApplication();

	/**
	 * Called when the user opens the application by clicking the icon from the
	 * dock.
	 * 
	 * @return true if the application handled the event.
	 */
	boolean handleReOpenApplication();

	/**
	 * Called when the user asks to open an associated file. The document is to
	 * be opened within the application.
	 * 
	 * @param filename
	 *            Document to be opened
	 * @return true if the application handled the event.
	 */
	boolean handleOpenFile(String filename);

	/**
	 * Called when the user asks to print an associated file. The document is to
	 * be printed within the application.
	 * 
	 * @param filename
	 *            Document to be printed
	 * @return true if the application handled the event.
	 */
	boolean handlePrintFile(String filename);

	/**
	 * Called when the user selects the "Preferences" menu item from the
	 * application menu. This menu item will only appear if hasPreferences()
	 * returned true. By implementing this method the application can show its
	 * own Preferences dialogue window. OS X does not provide any default
	 * dialogue if the method returns false.
	 * 
	 * @see hasPreferences()
	 * @return true if the application handled the event.
	 */
	boolean handlePreferences();

	/**
	 * Called when the user selects the "Quit" menu item or presses Apple-Q to
	 * quit the application. The application can perform clean-up or "Do you
	 * want to save" procedures. If the method returns true, the application
	 * will quit, if it returns false the quit will be aborted.
	 * 
	 * @return true if the application is to quit, or false if the quit is
	 *         cancelled.
	 */
	boolean handleQuit();

}
