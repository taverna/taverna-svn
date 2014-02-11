package net.sf.taverna.t2.component.registry.standard;

import static net.sf.taverna.t2.component.registry.standard.NewComponentRegistry.logger;
import static net.sf.taverna.t2.component.registry.standard.Policy.getPolicy;
import static net.sf.taverna.t2.component.registry.standard.Utils.getDataflowFromUri;
import static net.sf.taverna.t2.component.registry.standard.Utils.getElementString;
import static net.sf.taverna.t2.component.registry.standard.Utils.getValue;

import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.IllegalFormatException;

import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.License;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.SharingPolicy;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.workflowmodel.Dataflow;
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
	private final String resource;

	NewComponent(NewComponentRegistry registry, NewComponentFamily family,
			Description cd) throws RegistryException {
		super(cd.getUri());
		this.registry = registry;
		this.family = family;
		id = cd.getId().trim();
		title = getElementString(cd, "title");
		description = getElementString(cd, "description");
		resource = cd.getResource();
	}

	NewComponent(NewComponentRegistry registry, NewComponentFamily family,
			ComponentType ct) {
		super(ct.getUri());
		this.registry = registry;
		this.family = family;
		id = ct.getId().trim();
		title = ct.getTitle().trim();
		description = ct.getDescription().trim();
		resource = ct.getResource();
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
					.getWorkflow())
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

	public String getResourceLocation() {
		return resource;
	}

	private static final int BASEHASH = NewComponent.class.hashCode();

	@Override
	public int hashCode() {
		return BASEHASH ^ registry.hashCode() ^ id.hashCode();
	}

	class Version extends ComponentVersion {
		private int version;
		private String description;
		private String dataflowUri;
		private SoftReference<Dataflow> dataflowRef;
		
		private static final String htmlPageTemplate = "%1$s/workflows/%2$s/versions/%3$s.html";

		protected Version(Integer version, String description, Dataflow dataflow) {
			super(NewComponent.this);
			this.version = version;
			this.description = description;
			this.dataflowRef = new SoftReference<Dataflow>(dataflow);
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

		private String getDataflowUri() throws RegistryException {
			if (dataflowUri == null)
				dataflowUri = registry.getComponentById(id, version,
						"content-uri").getContentUri();
			return dataflowUri;
		}

		@Override
		protected synchronized Dataflow internalGetDataflow()
				throws RegistryException {
			if (dataflowRef == null || dataflowRef.get() == null) {
				String contentUri = getDataflowUri();
				try {
					Dataflow result = getDataflowFromUri(contentUri
							+ "?version=" + version);
					dataflowRef = new SoftReference<Dataflow>(result);
					return result;
				} catch (Exception e) {
					throw new RegistryException("Unable to open dataflow", e);
				}
			}
			return dataflowRef.get();
		}

		@Override
		public URL getHelpURL() {
			URL result = null;
			try {
				String urlString = String.format(htmlPageTemplate, registry.getRegistryBaseString(), getId(), version);
				result = new URL(urlString);
			} catch (IllegalFormatException | MalformedURLException e) {
				logger.error(e);
			}
			return result;
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
