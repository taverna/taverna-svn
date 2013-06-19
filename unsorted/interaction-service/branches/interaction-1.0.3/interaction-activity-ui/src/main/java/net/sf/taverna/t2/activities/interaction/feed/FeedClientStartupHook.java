/**
 *
 */
package net.sf.taverna.t2.activities.interaction.feed;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.workbench.StartupSPI;
import net.sf.taverna.t2.activities.interaction.FeedReader;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.parser.stax.FOMParser;
import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class FeedClientStartupHook implements StartupSPI {

	private static Logger logger = Logger.getLogger(FeedClientStartupHook.class);

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.StartupSPI#positionHint()
	 */
	@Override
	public int positionHint() {
		return 1000;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.StartupSPI#startup()
	 */
	@Override
	public boolean startup() {
		final Thread feedClientThread = new FeedReader("UI Feed Reader"){

			@Override
			protected void setClassLoader() {
				Thread.currentThread().setContextClassLoader(FeedClientStartupHook.class.getClassLoader());
			}

			@Override
			protected void considerEntry(Entry entry) {
				final Link presentationLink = entry.getLink("presentation");
				if (presentationLink != null) {
					try {
						Desktop.getDesktop().browse(presentationLink.getHref().toURI());
					} catch (IOException e) {
						logger.error("Cannot open presentation");
					} catch (URISyntaxException e) {
						logger.error("Cannot open presentation");
					}
				}
			}
		};
		    feedClientThread.start();
		return true;
	}

}
