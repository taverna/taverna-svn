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
		if (listener.handleOpenApplication()) {
			event.isHandled();
		}
	}

	public void handleReOpenApplication(ApplicationEvent event) {
		if (listener.handleReOpenApplication()) {
			event.isHandled();
		}
	}

	public void handleAbout(ApplicationEvent event) {
		if (listener.handleAbout()) {
			event.isHandled();
		}
	}

	public void handleOpenFile(ApplicationEvent event) {
		if (listener.handleOpenFile(event.getFilename())) {
			event.isHandled();
		}
	}

	public void handlePrintFile(ApplicationEvent event) {
		if (listener.handlePrintFile(event.getFilename())) {
			event.isHandled();
		}
	}

	public void handlePreferences(ApplicationEvent event) {
		if (listener.handlePreferences()) {
			event.isHandled();
		}
	}

	public void handleQuit(ApplicationEvent event) {
		if (listener.handleQuit()) {
			event.isHandled();
		}
	}

}
