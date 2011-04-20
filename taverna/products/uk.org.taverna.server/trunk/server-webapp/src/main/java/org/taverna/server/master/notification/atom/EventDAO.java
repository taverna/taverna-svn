/*
 * Copyright (C) 2011 The University of Manchester
 * 
 * See the file "LICENSE.txt" for license terms.
 */
package org.taverna.server.master.notification.atom;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.PersistenceAware;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;
import org.taverna.server.master.ContentsDescriptorBuilder.UriBuilderFactory;
import org.taverna.server.master.interfaces.MessageDispatcher;
import org.taverna.server.master.interfaces.TavernaRun;
import org.taverna.server.master.utils.JDOSupport;
import org.taverna.server.master.utils.UsernamePrincipal;

import edu.umd.cs.findbugs.annotations.NonNull;

@PersistenceAware
public class EventDAO extends JDOSupport<AbstractEvent> implements
		MessageDispatcher {
	public EventDAO() {
		super(AbstractEvent.class);
	}

	private Log log = LogFactory.getLog("Taverna.Server.Atom");
	private UriBuilderFactory ubf;
	private int expiryAgeDays;

	@Required
	public void setExpiryAgeDays(int expiryAgeDays) {
		this.expiryAgeDays = expiryAgeDays;
	}

	@Required
	public void setUriBuilderFactory(UriBuilderFactory ubf) {
		this.ubf = ubf;
	}

	@NonNull
	@WithinSingleTransaction
	public List<AbstractEvent> getEvents(@NonNull UsernamePrincipal user) {
		@SuppressWarnings("unchecked")
		List<String> ids = (List<String>) namedQuery("eventsForUser").execute(
				user.getName());
		log.debug("found " + ids.size() + " events for user " + user);

		List<AbstractEvent> result = new ArrayList<AbstractEvent>();
		for (String id : ids) {
			AbstractEvent event = getById(id);
			result.add(detach(event));
		}
		return result;
	}

	@NonNull
	@WithinSingleTransaction
	public AbstractEvent getEvent(@NonNull UsernamePrincipal user,
			@NonNull String id) {
		@SuppressWarnings("unchecked")
		List<String> ids = (List<String>) namedQuery("eventForUserAndId")
				.execute(user.getName(), id);
		log.debug("found " + ids.size() + " events for user " + user
				+ " with id = " + id);

		if (ids.size() != 1)
			throw new IllegalArgumentException("no such id");
		return detach(getById(ids.get(0)));
	}

	@WithinSingleTransaction
	public void deleteEventById(@NonNull String id) {
		delete(getById(id));
	}

	@WithinSingleTransaction
	public void deleteExpiredEvents() {
		Date death = new DateTime().plusDays(-expiryAgeDays).toDate();
		death = new Timestamp(death.getTime()); // UGLY SQL HACK

		@SuppressWarnings("unchecked")
		List<String> ids = (List<String>) namedQuery("eventsFromBefore")
				.execute(death);
		log.debug("found " + ids.size()
				+ " events to be squelched (older than " + death + ")");

		for (String id : ids)
			delete(getById(id));
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	@WithinSingleTransaction
	public void dispatch(TavernaRun originator, String messageSubject,
			String messageContent, String targetParameter) throws Exception {
		UsernamePrincipal owner = originator.getSecurityContext().getOwner();
		UriBuilder ub = ubf.getRunUriBuilder(originator);
		persist(new TerminationEvent(ub.build(), owner, messageSubject,
				messageContent));
	}
}
