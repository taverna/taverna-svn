package net.sf.taverna.feta.browser;

import java.io.File;
import java.net.URI;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.taverna.feta.browser.elmo.ServiceRegistry;
import net.sf.taverna.feta.browser.util.URIFactory;

public class ServicesInitialisation implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent context) {
	}

	public void contextInitialized(ServletContextEvent contextEvent) {
		ServletContext context = contextEvent.getServletContext();
		File tmpDir = (File) context
				.getAttribute("javax.servlet.context.tempdir");
		File sesameDir = new File(tmpDir, "sesame");
		sesameDir.mkdirs();
		ServiceRegistry.setDataDir(sesameDir);
	}

}
