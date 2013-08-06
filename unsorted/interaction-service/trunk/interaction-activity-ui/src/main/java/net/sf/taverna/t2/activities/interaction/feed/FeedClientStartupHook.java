/**
 *
 */
package net.sf.taverna.t2.activities.interaction.feed;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;

import net.sf.taverna.t2.activities.interaction.FeedReader;
import net.sf.taverna.t2.workbench.StartupSPI;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;
import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class FeedClientStartupHook implements StartupSPI {

	private static Logger logger = Logger
			.getLogger(FeedClientStartupHook.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.workbench.StartupSPI#positionHint()
	 */
	@Override
	public int positionHint() {
		return 1000;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.workbench.StartupSPI#startup()
	 */
	@Override
	public boolean startup() {
		final Thread feedClientThread = new FeedReader("UI Feed Reader") {

			@Override
			protected void setClassLoader() {
				Thread.currentThread().setContextClassLoader(
						FeedClientStartupHook.class.getClassLoader());
			}

			@Override
			protected void considerEntry(final Entry entry) {
				final Link presentationLink = entry.getLink("presentation");
				if (presentationLink != null) {
					try {
						Desktop.getDesktop().browse(
								presentationLink.getHref().toURI());
					} catch (final IOException e) {
						logger.error("Cannot open presentation");
					} catch (final URISyntaxException e) {
						logger.error("Cannot open presentation");
					}
				}
			}
		};
		feedClientThread.start();
		return true;
	}

}
