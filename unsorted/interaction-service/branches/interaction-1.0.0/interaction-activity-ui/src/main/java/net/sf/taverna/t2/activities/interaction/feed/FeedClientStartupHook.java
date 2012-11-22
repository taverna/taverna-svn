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
		final Thread feedClientThread = new Thread(){

			@Override
			public void run() {
				Thread.currentThread().setContextClassLoader(FeedClientStartupHook.class.getClassLoader());
				if (InteractionPreference.getInstance().getUseJetty()) {
					InteractionJetty.checkJetty();
				}

				final Parser parser = new FOMParser(Abdera.getInstance());
				Date lastCheckedDate = new Date();
					while (true) {
						try {
							sleep(5000);
						} catch (final InterruptedException e1) {
							logger.error(e1);
						}
						final Date newLastCheckedDate = new Date();
						InputStream openStream = null;
						try {
						final URL url = new URL(InteractionPreference.getInstance().getFeedUrl());
						openStream = url.openStream();
						final Document<Feed> doc = parser.parse(openStream, url.toString());
						final Feed feed = doc.getRoot().sortEntriesByEdited(true);

						for (final Entry entry : feed.getEntries()) {
							Date d = entry.getEdited();
							if (d == null) {
								d = entry.getUpdated();
							}
							if (d == null) {
								d = entry.getPublished();
							}
							if (d.before(lastCheckedDate)) {
								break;
							}
							final Link presentationLink = entry.getLink("presentation");
								if (presentationLink != null) {
									Desktop.getDesktop().browse(presentationLink.getHref().toURI());
								}
						}
						lastCheckedDate = newLastCheckedDate;
						} catch (final MalformedURLException e) {
							logger.error(e);
						} catch (final ParseException e) {
							logger.error(e);
						} catch (final IOException e) {
							logger.error(e);
						} catch (final URISyntaxException e) {
							logger.error(e);
						}
						finally {
							try {
								openStream.close();
							} catch (final IOException e) {
								logger.error(e);
							}

						}
					}
			}};
		    feedClientThread.start();
		return true;
	}

}
