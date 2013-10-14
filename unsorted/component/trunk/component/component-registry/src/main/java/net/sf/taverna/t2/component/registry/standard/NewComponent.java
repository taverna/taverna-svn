package net.sf.taverna.t2.component.registry.standard;

import static net.sf.taverna.t2.component.registry.standard.NewComponentRegistry.logger;
import static net.sf.taverna.t2.component.registry.standard.Policy.PRIVATE;
import static net.sf.taverna.t2.component.registry.standard.Policy.getPolicy;
import static net.sf.taverna.t2.component.registry.standard.Utils.getDataflowFromUri;
import static net.sf.taverna.t2.component.registry.standard.Utils.getElementString;
import static net.sf.taverna.t2.component.registry.standard.Utils.getValue;
import net.sf.taverna.t2.component.api.License;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.SharingPolicy;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import uk.org.taverna.component.api.ComponentDescription;
import uk.org.taverna.component.api.ComponentType;
import uk.org.taverna.component.api.Description;
import uk.org.taverna.component.api.Versions;

public class NewComponent extends Component {
	static final String ELEMENTS = "title,description,license-type,permissions";

	NewComponentRegistry registry;
	NewComponentFamily family;
	private String id;
	private String title;
	private String description;
	private License license;
	private SharingPolicy sharingPolicy;

	protected NewComponent(NewComponentRegistry registry,
			NewComponentFamily family, ComponentDescription cd)
			throws RegistryException {
		super(cd.getUri());
		this.registry = registry;
		this.family = family;
		id = cd.getId().trim();
		title = getElementString(cd, "title");
		description = getElementString(cd, "description");
		license = registry.getLicense(getElementString(cd, "license-type"));
		try {
			// UGLY! Can't get the sharing policy back with the aggregate query
			sharingPolicy = getPolicy(registry.getComponent(id, "permissions")
					.getPermissions());
		} catch (RegistryException e) {
			sharingPolicy = PRIVATE;
		}
	}

	public NewComponent(NewComponentRegistry registry,
			NewComponentFamily family, ComponentType ct)
			throws RegistryException {
		super(ct.getUri());
		this.registry = registry;
		this.family = family;
		id = ct.getId().trim();
		title = ct.getTitle().trim();
		description = ct.getDescription().trim();
		license = registry.getLicense(getValue(ct.getLicenseType()).trim());
		sharingPolicy = getPolicy(ct.getPermissions());
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
		Versions versions;
		try {
			versions = registry.getComponent(id, "versions").getVersions();
		} catch (RegistryException e) {
			logger.warn("failed to retrieve version list: " + e.getMessage());
			return;
		}
		for (Description d : versions.getVersion())
			versionMap.put(d.getVersion(), new Version(d.getVersion(),
					getValue(d)));
	}

	@Override
	protected Version internalAddVersionBasedOn(Dataflow dataflow,
			String revisionComment) throws RegistryException {
		return (Version) registry.createComponentVersionFrom(this, title,
				revisionComment, dataflow, license, sharingPolicy);
	}

	public String getId() {
		return id;
	}

	class Version extends ComponentVersion {
		private Integer version;
		private String description;
		private Dataflow dataflow;

		protected Version(Integer version, String description, Dataflow dataflow) {
			super(NewComponent.this);
			this.version = version;
			this.description = description;
			this.dataflow = dataflow;
		}

		protected Version(Integer version, String description) {
			super(NewComponent.this);
			this.version = version;
			this.description = description;
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
		protected Dataflow internalGetDataflow() throws RegistryException {
			if (dataflow == null) {
				String contentUri = registry.getComponent(id, "content-uri")
						.getContentUri();
				try {
					dataflow = getDataflowFromUri(contentUri + "?version="
							+ version);
				} catch (Exception e) {
					logger.error(e);
					throw new RegistryException("Unable to open dataflow", e);
				}
			}
			return dataflow;
		}
	}
}
