package net.sf.taverna.t2.component.registry.standard;

import static net.sf.taverna.t2.component.registry.standard.Utils.getElementString;

import java.net.MalformedURLException;
import java.net.URL;

import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.profile.ComponentProfile;
import uk.org.taverna.component.api.ComponentProfileType;
import uk.org.taverna.component.api.Description;

/**
 * Profiles managed by the new-interface component registry.
 * 
 * @author Donal Fellows
 */
class NewComponentProfile extends ComponentProfile {
	private static final String LOCATION = "content-uri";
	static final String ELEMENTS = LOCATION;

	private final NewComponentRegistry registry;
	private String id;
	private String location;
	private String resource;
	private final String uri;

	private static URL contentUrl(ComponentProfileType cpt)
			throws RegistryException {
		try {
			return new URL(cpt.getContentUri());
		} catch (MalformedURLException e) {
			throw new RegistryException("bad profile location", e);
		}
	}

	private static URL getLocationURL(Description cpd) throws RegistryException {
		try {
			return new URL(getElementString(cpd, LOCATION));
		} catch (MalformedURLException e) {
			throw new RegistryException("bad profile location", e);
		}
	}

	NewComponentProfile(NewComponentRegistry registry,
			ComponentProfileType profile) throws RegistryException {
		super(registry, contentUrl(profile));
		this.registry = registry;
		uri = profile.getUri();
		id = profile.getId();
		location = profile.getContentUri();
		resource = profile.getResource();
	}

	NewComponentProfile(NewComponentRegistry registry, Description cpd)
			throws RegistryException {
		super(registry, getLocationURL(cpd));
		this.registry = registry;
		uri = cpd.getUri();
		id = cpd.getId();
		location = getElementString(cpd, LOCATION);
		resource = cpd.getResource();
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
	public boolean equals(Object o) {
		if (o instanceof NewComponentProfile) {
			NewComponentProfile other = (NewComponentProfile) o;
			return registry.equals(other.registry) && id.equals(other.id);
		}
		return false;
	}

	private static final int BASEHASH = NewComponentProfile.class.hashCode();

	@Override
	public int hashCode() {
		return BASEHASH ^ registry.hashCode() ^ id.hashCode();
	}

	@Override
	public String toString() {
		return "NewComponentProfile at " + location;
	}

	public String getResourceLocation() {
		return resource;
	}
}
