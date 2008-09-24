package net.sf.taverna.osx;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

public class OSXApplicationListener implements ApplicationListener {

	/**
	 * Register with the OS X Application framework using the given listener.
	 * 
	 * @param listener
	 *            A OSXListener instance that will be called back for various OS
	 *            X application events.
	 */
	public static void register(OSXListener listener) {
		new OSXApplicationListener(listener);
	}

	private OSXListener listener;

	private OSXApplicationListener(OSXListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}
		this.listener = listener;
		Application application = new Application();
		application.addApplicationListener(this);
		application.setEnabledPreferencesMenu(listener.hasPreferences());
		application.setEnabledAboutMenu(listener.hasAbout());
	}

	public void handleOpenApplication(ApplicationEvent event) {
		event.setHandled(listener.handleOpenApplication());
	}

	public void handleReOpenApplication(ApplicationEvent event) {
		event.setHandled(listener.handleReOpenApplication());
	}

	public void handleAbout(ApplicationEvent event) {
		event.setHandled(listener.handleAbout());
	}

	public void handleOpenFile(ApplicationEvent event) {
		event.setHandled(listener.handleOpenFile(event.getFilename()));
	}

	public void handlePrintFile(ApplicationEvent event) {
		event.setHandled(listener.handlePrintFile(event.getFilename()));
	}

	public void handlePreferences(ApplicationEvent event) {
		event.setHandled(listener.handlePreferences());
	}

	public void handleQuit(ApplicationEvent event) {
		event.setHandled(listener.handleQuit());
	}

}
