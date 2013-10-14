package net.sf.taverna.t2.component.registry.standard;

import static net.sf.taverna.t2.component.registry.standard.Utils.getElementString;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.profile.ComponentProfile;
import uk.org.taverna.component.api.ComponentProfileDescription;
import uk.org.taverna.component.api.ComponentProfileType;

/**
 * Profiles managed by the new-interface component registry.
 * 
 * @author Donal Fellows
 */
public class NewComponentProfile extends ComponentProfile {
	private static final String LOCATION = "content-uri";
	static final String ELEMENTS = LOCATION;

	private String id;
	private String location;
	private final String uri;

	NewComponentProfile(NewComponentRegistry registry,
			ComponentProfileType profile) throws RegistryException {
		super(registry, profile.getContentUri());
		uri = profile.getUri();
		id = profile.getId();
		location = profile.getContentUri();
	}

	NewComponentProfile(NewComponentRegistry registry,
			ComponentProfileDescription cpd) throws RegistryException {
		super(registry, getElementString(cpd, LOCATION));
		uri = cpd.getUri();
		id = cpd.getId();
		location = getElementString(cpd, LOCATION);
	}

	public String getLocation() {
		return location;
	}

	public String getID() {
		return id;
	}

	public String getUri() {
		return uri;
	}

	@Override
	public String toString() {
		return "NewComponentProfile at " + location;
	}
}
