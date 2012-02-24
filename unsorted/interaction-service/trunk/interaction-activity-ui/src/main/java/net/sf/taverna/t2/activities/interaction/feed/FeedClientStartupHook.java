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

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.AtomDate;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.Parser;
import org.apache.log4j.Logger;

import net.sf.taverna.t2.activities.interaction.jetty.InteractionJetty;
import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.workbench.StartupSPI;

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
		return 10;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.StartupSPI#startup()
	 */
	@Override
	public boolean startup() {
		Thread feedClientThread = new Thread(){

			@Override
			public void run() {
				InteractionJetty.checkJetty();
				Parser parser = Abdera.getNewParser();
				Date lastCheckedDate = new Date();
					while (true) {
						try {
							sleep(5000);
						} catch (InterruptedException e1) {
							logger.error(e1);
						}
						Date newLastCheckedDate = new Date();
						InputStream openStream = null;
						try {
						URL url = new URL(InteractionPreference.getInstance().getFeedUrl());
						openStream = url.openStream();
						Document<Feed> doc = parser.parse(openStream, url.toString());
						Feed feed = doc.getRoot().sortEntriesByEdited(true);

						for (Entry entry : feed.getEntries()) {
							if (entry.getEdited().before(lastCheckedDate)) {
								break;
							}
							Link presentationLink = entry.getLink("presentation");
								if (presentationLink != null) {
									Desktop.getDesktop().browse(presentationLink.getHref().toURI());
								}
						}
						lastCheckedDate = newLastCheckedDate;
						} catch (MalformedURLException e) {
							logger.error(e);
						} catch (ParseException e) {
							logger.error(e);
						} catch (IOException e) {
							logger.error(e);
						} catch (URISyntaxException e) {
							logger.error(e);
						}
						finally {
							/*try {
								openStream.close();
							} catch (IOException e) {
								logger.error(e);
							}*/

						}
					}
			}};
		feedClientThread.start();
		return true;
	}

}
