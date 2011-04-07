package org.taverna.server.master.notification.atom;

import java.util.List;

import org.apache.abdera.model.Feed;
import org.apache.cxf.jaxrs.ext.atom.AtomElementWriter;

public class FeedWriter implements AtomElementWriter<Feed, List<AbstractEvent>> {
	@Override
	public void writeTo(Feed feed, List<AbstractEvent> events) {
		if (!events.isEmpty())
			feed.getUpdatedElement().setDate(
					events.get(0).getPublished().toDate());
		for (AbstractEvent event : events)
			event.write(feed);
	}
}
