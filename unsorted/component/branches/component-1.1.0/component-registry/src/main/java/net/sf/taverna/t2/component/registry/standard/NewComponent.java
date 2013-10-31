package net.sf.taverna.t2.component.registry.standard;

import static net.sf.taverna.t2.component.registry.standard.NewComponentRegistry.logger;
import static net.sf.taverna.t2.component.registry.standard.Policy.getPolicy;
import static net.sf.taverna.t2.component.registry.standard.Utils.getDataflowFromUri;
import static net.sf.taverna.t2.component.registry.standard.Utils.getElementString;
import static net.sf.taverna.t2.component.registry.standard.Utils.getValue;

import java.lang.ref.SoftReference;

import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.License;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.SharingPolicy;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import uk.org.taverna.component.api.ComponentDescription;
import uk.org.taverna.component.api.ComponentType;
import uk.org.taverna.component.api.Description;

class NewComponent extends Component {
	static final String ELEMENTS = "title,description";
	static final String EXTRA = "license-type,permissions";

	final NewComponentRegistry registry;
	final NewComponentFamily family;
	private final String id;
	private final String title;
	private final String description;

	NewComponent(NewComponentRegistry registry, NewComponentFamily family,
			ComponentDescription cd) throws RegistryException {
		super(cd.getUri());
		this.registry = registry;
		this.family = family;
		id = cd.getId().trim();
		title = getElementString(cd, "title");
		description = getElementString(cd, "description");
	}

	NewComponent(NewComponentRegistry registry, NewComponentFamily family,
			ComponentType ct) {
		super(ct.getUri());
		this.registry = registry;
		this.family = family;
		id = ct.getId().trim();
		title = ct.getTitle().trim();
		description = ct.getDescription().trim();
	}

	public ComponentType getCurrent(String elements) throws RegistryException {
		return registry.getComponentById(id, null, elements);
	}

	@Override
	protected String internalGetName() {
		return title;
	}

	@Override
	protected String internalGetDescription() {
		return description;
	}

	@Override
	protected void populateComponentVersionMap() {
		try {
			for (Description d : getCurrent("versions").getVersions()
					.getVersion())
				versionMap.put(d.getVersion(), new Version(d.getVersion(),
						getValue(d)));
		} catch (RegistryException e) {
			logger.warn("failed to retrieve version list: " + e.getMessage());
		}
	}

	@Override
	protected Version internalAddVersionBasedOn(Dataflow dataflow,
			String revisionComment) throws RegistryException {
		/*
		 * Only fetch the license and sharing policy now; user might have
		 * updated them on the site and we want to duplicate.
		 */
		ComponentType ct = getCurrent(EXTRA);
		License license = registry.getLicense(getValue(ct.getLicenseType())
				.trim());
		SharingPolicy sharingPolicy = getPolicy(ct.getPermissions());

		return (Version) registry.createComponentVersionFrom(this, title,
				revisionComment, dataflow, license, sharingPolicy);
	}

	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof NewComponent) {
			NewComponent other = (NewComponent) o;
			return registry.equals(other.registry) && id.equals(other.id);
		}
		return false;
	}

	private static final int BASEHASH = NewComponent.class.hashCode();

	@Override
	public int hashCode() {
		return BASEHASH ^ registry.hashCode() ^ id.hashCode();
	}

	class Version extends ComponentVersion {
		private int version;
		private String description;
		SoftReference<Dataflow> dataflow;

		protected Version(Integer version, String description, Dataflow dataflow) {
			super(NewComponent.this);
			this.version = version;
			this.description = description;
			this.dataflow = new SoftReference<Dataflow>(dataflow);
		}

		protected Version(Integer version, String description) {
			super(NewComponent.this);
			this.version = version;
			this.description = description;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Version) {
				Version other = (Version) o;
				return version == other.version
						&& NewComponent.this.equals(other.getComponent());
			}
			return false;
		}

		@Override
		public int hashCode() {
			return NewComponent.this.hashCode() ^ (version << 16)
					^ (version >> 16);
		}

		@Override
		protected Integer internalGetVersionNumber() {
			return version;
		}

		@Override
		protected String internalGetDescription() {
			return description;
		}

		@Override
		protected synchronized Dataflow internalGetDataflow()
				throws RegistryException {
			if (dataflow == null || dataflow.get() == null) {
				String contentUri = registry.getComponentById(id, version,
						"content-uri").getContentUri();
				try {
					dataflow = new SoftReference<Dataflow>(
							getDataflowFromUri(contentUri + "?version="
									+ version));
				} catch (Exception e) {
					logger.error(e);
					throw new RegistryException("Unable to open dataflow", e);
				}
			}
			return dataflow.get();
		}
	}

	@Override
	public Registry getRegistry() {
		return registry;
	}

	@Override
	public Family getFamily() {
		return family;
	}
}
