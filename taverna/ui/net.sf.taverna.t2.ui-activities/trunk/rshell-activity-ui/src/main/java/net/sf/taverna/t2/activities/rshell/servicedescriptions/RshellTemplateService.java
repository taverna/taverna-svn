/*******************************************************************************
 * Copyright (C) 2009 Ingo Wassink of University of Twente, Netherlands and
 * The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/

/**
 * @author Ingo Wassink
 * @author Ian Dunlop
 * @author Alan R Williams
 */
package net.sf.taverna.t2.activities.rshell.servicedescriptions;

import java.net.URI;

import javax.swing.Icon;

import net.sf.taverna.t2.servicedescriptions.AbstractTemplateService;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.property.PropertyLiteral;
import uk.org.taverna.scufl2.api.property.PropertyResource;

public class RshellTemplateService extends AbstractTemplateService {

	private static final URI ACTIVITY_TYPE = URI.create("http://ns.taverna.org.uk/2010/activity/rshell");
	private static final String RSHELL = "Rshell";

	private static final URI providerId = URI
	.create("http://taverna.sf.net/2010/service-provider/rshell");

	@Override
	public URI getActivityType() {
		return ACTIVITY_TYPE;
	}

	@Override
	public Configuration getActivityConfiguration() {
		Configuration configuration = new Configuration();
		configuration.setType(ACTIVITY_TYPE.resolve("#Config"));
		configuration.getPropertyResource().addPropertyAsString(ACTIVITY_TYPE.resolve("#script"), "");
		PropertyResource connection = configuration.getPropertyResource().addPropertyAsNewResource(
				ACTIVITY_TYPE.resolve("#connection"),
				ACTIVITY_TYPE.resolve("#Connection"));
		connection.addProperty(ACTIVITY_TYPE.resolve("#hostname"), new PropertyLiteral("localhost"));
		connection.addProperty(ACTIVITY_TYPE.resolve("#port"), new PropertyLiteral(6311));
		connection.addProperty(ACTIVITY_TYPE.resolve("#keepSessionAlive"), new PropertyLiteral(false));

		return configuration;
	}

	@Override
	public Icon getIcon() {
		return RshellActivityIcon.getRshellIcon();
	}

	public String getName() {
		return RSHELL;
	}

	@Override
	public String getDescription() {
		return "A service that allows the calling of R scripts on an R server";
	}

	@SuppressWarnings("unchecked")
	public static ServiceDescription getServiceDescription() {
		RshellTemplateService rts = new RshellTemplateService();
		return rts.templateService;
	}

	public String getId() {
		return providerId.toString();
	}
}
