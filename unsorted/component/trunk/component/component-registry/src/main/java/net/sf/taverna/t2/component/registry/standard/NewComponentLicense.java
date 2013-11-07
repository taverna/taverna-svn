package net.sf.taverna.t2.component.registry.standard;

import uk.org.taverna.component.api.LicenseType;
import net.sf.taverna.t2.component.api.License;

class NewComponentLicense implements License {
	private NewComponentRegistry registry;
	private String id;
	private String title;
	private String description;
	private String abbreviation;

	static final String ELEMENTS = "title,description,unique-name";

	NewComponentLicense(NewComponentRegistry newComponentRegistry,
			LicenseType lt) {
		registry = newComponentRegistry;
		id = lt.getId();
		title = lt.getTitle();
		description = lt.getDescription();
		abbreviation = lt.getUniqueName();
	}

	String getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof NewComponentLicense))
			return false;
		NewComponentLicense other = (NewComponentLicense) o;
		return registry.equals(other.registry) && id.equals(other.id);
	}

	private static final int BASEHASH = NewComponentLicense.class.hashCode();

	@Override
	public int hashCode() {
		return BASEHASH ^ registry.hashCode() ^ id.hashCode();
	}

	@Override
	public String getName() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getAbbreviation() {
		return abbreviation;
	}

}
