package net.sf.taverna.t2.component.registry.standard;

import static net.sf.taverna.t2.component.registry.standard.Policy.PRIVATE;
import static net.sf.taverna.t2.component.registry.standard.Utils.getElementString;
import static net.sf.taverna.t2.component.registry.standard.Utils.serializeDataflow;

import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.License;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.SharingPolicy;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.api.Version.ID;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

import uk.org.taverna.component.api.ComponentDescription;
import uk.org.taverna.component.api.ComponentDescriptionList;
import uk.org.taverna.component.api.ComponentFamilyDescription;
import uk.org.taverna.component.api.ComponentFamilyList;
import uk.org.taverna.component.api.ComponentFamilyType;
import uk.org.taverna.component.api.ComponentProfileDescription;
import uk.org.taverna.component.api.ComponentProfileList;
import uk.org.taverna.component.api.ComponentProfileType;
import uk.org.taverna.component.api.ComponentType;
import uk.org.taverna.component.api.Content;
import uk.org.taverna.component.api.Description;
import uk.org.taverna.component.api.LicenseList;
import uk.org.taverna.component.api.LicenseType;
import uk.org.taverna.component.api.ObjectFactory;
import uk.org.taverna.component.api.Permissions;
import uk.org.taverna.component.api.PolicyList;

public class NewComponentRegistry extends ComponentRegistry {
	private static final Map<String, NewComponentRegistry> componentRegistries = new HashMap<String, NewComponentRegistry>();
	static final Logger logger = Logger.getLogger(NewComponentRegistry.class);
	static final JAXBContext jaxbContext;
	static final Charset utf8;
	private static final ObjectFactory objectFactory = new ObjectFactory();

	// service URIs
	private static final String COMPONENT_SERVICE = "/component.xml";
	private static final String COMPONENT_FAMILY_SERVICE = "/component-family.xml";
	private static final String COMPONENT_PROFILE_SERVICE = "/component-profile.xml";
	private static final String COMPONENT_LIST = "/components.xml";
	private static final String COMPONENT_FAMILY_LIST = "/component-families.xml";
	private static final String COMPONENT_PROFILE_LIST = "/component-profiles.xml";
	private static final String WORKFLOW_SERVICE = "/workflow.xml";
	private static final String PACK_SERVICE = "/pack.xml";
	private static final String FILE_SERVICE = "/file.xml";
	private static final String LICENSE_LIST = "/licenses.xml";
	private static final String POLICY_LIST = "/policies.xml";

	static {
		JAXBContext c = null;
		Charset cs = null;
		try {
			c = JAXBContext.newInstance(ComponentDescriptionList.class,
					ComponentFamilyList.class, ComponentProfileList.class,
					ComponentType.class, ComponentFamilyType.class,
					ComponentProfileType.class, PolicyList.class,
					LicenseList.class);
			cs = Charset.forName("UTF-8");
		} catch (JAXBException e) {
			throw new Error("failed to build context", e);
		} catch (UnsupportedCharsetException e) {
			throw new Error("failed to find charset", e);
		} finally {
			jaxbContext = c;
			utf8 = cs;
		}
	}

	private final Client client;

	protected NewComponentRegistry(URL registryBase) throws RegistryException {
		super(registryBase);
		try {
			client = new Client(jaxbContext, logger, registryBase);
		} catch (Exception e) {
			logger.error(e);
			throw new RegistryException("Unable to access registry", e);
		}
	}

	@Override
	protected void populateFamilyCache() throws RegistryException {
		for (Profile pr : getComponentProfiles()) {
			NewComponentProfile p = (NewComponentProfile) pr;
			for (ComponentFamilyDescription cfd : client.get(
					ComponentFamilyList.class, COMPONENT_FAMILY_LIST,
					"component-profile=" + p.getUri(),
					"elements=" + NewComponentFamily.ELEMENTS).getPack())
				familyCache.put(getElementString(cfd, "title"),
						new NewComponentFamily(this, p, cfd));
		}
	}

	ComponentType getComponentById(String id, Integer version, String elements)
			throws RegistryException {
		if (version != null)
			return client.get(ComponentType.class, WORKFLOW_SERVICE,
					"id=" + id, "version=" + version, "elements=" + elements);
		return client.get(ComponentType.class, WORKFLOW_SERVICE, "id=" + id,
				"elements=" + elements);
	}

	ComponentFamilyType getComponentFamilyById(String id, String elements)
			throws RegistryException {
		return client.get(ComponentFamilyType.class, PACK_SERVICE, "id=" + id,
				"elements=" + elements);
	}

	ComponentProfileType getComponentProfileById(String id, String elements)
			throws RegistryException {
		return client.get(ComponentProfileType.class, FILE_SERVICE, "id=" + id,
				"elements=" + elements);
	}

	@Override
	protected Family internalCreateComponentFamily(String familyName,
			Profile componentProfile, String description, License license,
			SharingPolicy sharingPolicy) throws RegistryException {
		NewComponentProfile profile = (NewComponentProfile) componentProfile;

		return new NewComponentFamily(this, profile, client.post(
				ComponentFamilyType.class,
				objectFactory.createPack(makeComponentFamilyCreateRequest(
						profile, familyName, description, license,
						sharingPolicy)), COMPONENT_FAMILY_SERVICE, "elements="
						+ NewComponentFamily.ELEMENTS));
	}

	@Override
	protected void internalRemoveComponentFamily(Family componentFamily)
			throws RegistryException {
		NewComponentFamily ncf = (NewComponentFamily) componentFamily;
		client.delete(WORKFLOW_SERVICE, "id=" + ncf.getId());
	}

	@Override
	protected void populateProfileCache() throws RegistryException {
		for (ComponentProfileDescription cpd : client.get(
				ComponentProfileList.class, COMPONENT_PROFILE_LIST,
				"elements=" + NewComponentProfile.ELEMENTS).getFile())
			if (cpd.getUri() != null && !cpd.getUri().isEmpty())
				profileCache.add(new NewComponentProfile(this, cpd));
	}

	@Override
	protected Profile internalAddComponentProfile(Profile componentProfile,
			License license, SharingPolicy sharingPolicy)
			throws RegistryException {
		if (componentProfile == null)
			throw new RegistryException(("Component profile must not be null"));
		try {
			if (componentProfile instanceof NewComponentProfile) {
				NewComponentProfile profile = (NewComponentProfile) componentProfile;
				if (profile.getComponentRegistry().getRegistryBase()
						.equals(getRegistryBase()))
					return new NewComponentProfile(this,
							getComponentProfileById(profile.getId(),
									NewComponentProfile.ELEMENTS));
			}
		} catch (RegistryException e) {
			// Do nothing but fall through
		}
		return new NewComponentProfile(this, client.post(
				ComponentProfileType.class, objectFactory
						.createFile(makeComponentProfileCreateRequest(
								componentProfile.getName(),
								componentProfile.getDescription(),
								componentProfile.getXML(), license,
								sharingPolicy)), COMPONENT_PROFILE_SERVICE,
				"elements=" + NewComponentProfile.ELEMENTS));
	}

	public Permissions getPermissions(SharingPolicy userSharingPolicy) {
		if (userSharingPolicy == null)
			userSharingPolicy = getDefaultSharingPolicy();
		return ((Policy) userSharingPolicy).getPermissionsElement();
	}

	private ComponentProfileType makeComponentProfileCreateRequest(
			String title, String description, String content, License license,
			SharingPolicy sharingPolicy) throws RegistryException {
		ComponentProfileType profile = new ComponentProfileType();

		profile.setFilename(title + ".xml");
		profile.setTitle(title);
		profile.setTitle(description);
		profile.setType(new Description());
		profile.getType().getContent().add("XML");
		profile.setContentType("application/vnd.taverna.component-profile+xml");
		profile.setContent(new Content());
		profile.getContent().setEncoding("base64");
		profile.getContent().setType("binary");
		profile.getContent().setValue(content.getBytes(utf8));
		if (license == null)
			license = getPreferredLicense();
		profile.setLicenseType(new Description());
		profile.getLicenseType().getContent().add(license.getAbbreviation());
		profile.setPermissions(getPermissions(sharingPolicy));

		return profile;
	}

	private ComponentFamilyType makeComponentFamilyCreateRequest(
			NewComponentProfile profile, String familyName, String description,
			License license, SharingPolicy sharingPolicy)
			throws RegistryException {
		ComponentFamilyType familyDoc = new ComponentFamilyType();

		familyDoc.setComponentProfile(profile.getLocation());
		familyDoc.setDescription(description);
		familyDoc.setTitle(familyName);
		if (license == null)
			license = getPreferredLicense();
		familyDoc.setLicenseType(new Description());
		familyDoc.getLicenseType().getContent().add(license.getAbbreviation());
		familyDoc.setPermissions(getPermissions(sharingPolicy));

		return familyDoc;
	}

	private ComponentType makeComponentVersionCreateRequest(String title,
			String description, Dataflow content, NewComponentFamily family,
			License license, SharingPolicy sharingPolicy)
			throws RegistryException {
		ComponentType comp = new ComponentType();

		comp.setTitle(title);
		comp.setDescription(description);
		comp.setComponentFamily(family.getUri());
		comp.setContentType("application/vnd.taverna.t2flow+xml");
		comp.setContent(new Content());
		comp.getContent().setEncoding("base64");
		comp.getContent().setType("binary");
		comp.getContent().setValue(serializeDataflow(content).getBytes(utf8));
		if (license == null)
			license = getPreferredLicense();
		if (license != null) {
			comp.setLicenseType(new Description());
			comp.getLicenseType().getContent().add(license.getAbbreviation());
		}
		comp.setPermissions(getPermissions(sharingPolicy));

		return comp;
	}

	private List<Description> listPolicies() throws RegistryException {
		return client.get(PolicyList.class, POLICY_LIST, "type=group")
				.getPolicy();
	}

	@Override
	protected void populatePermissionCache() {
		permissionCache.add(Policy.PUBLIC);
		permissionCache.add(Policy.PRIVATE);
		try {
			for (Description d : listPolicies())
				permissionCache.add(new Policy.Group(d.getId()));
		} catch (RegistryException e) {
			logger.warn("failed to fetch sharing policies", e);
		}
	}

	private List<LicenseType> listLicenses() throws RegistryException {
		return client.get(LicenseList.class, LICENSE_LIST,
				"elements=" + NewComponentLicense.ELEMENTS).getLicense();
	}

	@Override
	protected void populateLicenseCache() {
		try {
			for (LicenseType lt : listLicenses())
				licenseCache.add(new NewComponentLicense(this, lt));
		} catch (RegistryException e) {
			logger.warn("failed to fetch licenses", e);
		}
	}

	@Override
	public License getPreferredLicense() throws RegistryException {
		return getLicenseByAbbreviation(getNameOfPreferredLicense());
	}

	public String getNameOfPreferredLicense() {
		return "by-nd";
	}

	public SharingPolicy getDefaultSharingPolicy() {
		return PRIVATE;
	}

	@Override
	public Set<ID> searchForComponents(String prefixes, String text)
			throws RegistryException {
		HashSet<ID> versions = new HashSet<ID>();
		for (ComponentDescription cd : client.get(
				ComponentDescriptionList.class, COMPONENT_LIST,
				"query=" + text, "prefixes=" + prefixes,
				"elements=" + NewComponent.ELEMENTS).getWorkflow()) {
			NewComponent nc = null;
			for (Family f : getComponentFamilies()) {
				if (!(f instanceof NewComponentFamily))
					continue;
				nc = (NewComponent) ((NewComponentFamily) f)
						.getComponent(getElementString(cd, "title"));
				if (nc != null)
					break;
			}
			if (nc != null)
				versions.add(new VersionId(nc, cd.getVersion()));
			else
				logger.warn("could not construct component for " + cd.getUri());
		}
		return versions;
	}

	static class VersionId implements ID, Serializable {
		private static final long serialVersionUID = 398785161396817963L;
		private URL registry;
		private String family, name;
		private int version;

		VersionId(NewComponent component, Integer version) {
			registry = component.registry.getRegistryBase();
			family = component.family.getName();
			name = component.getName();
			this.version = version;
		}

		@Override
		public String getFamilyName() {
			return family;
		}

		@Override
		public URL getRegistryBase() {
			return registry;
		}

		@Override
		public String getComponentName() {
			return name;
		}

		@Override
		public Integer getComponentVersion() {
			return version;
		}
	}

	protected List<Component> listComponents(NewComponentFamily family)
			throws RegistryException {
		List<Component> result = new ArrayList<Component>();
		for (ComponentDescription cd : client.get(
				ComponentDescriptionList.class, COMPONENT_LIST,
				"component-family=" + family.getUri(),
				"elements=" + NewComponent.ELEMENTS).getWorkflow())
			result.add(new NewComponent(this, family, cd));
		return result;
	}

	protected void deleteComponent(NewComponent component)
			throws RegistryException {
		client.delete(WORKFLOW_SERVICE, "id=" + component.getId());
	}

	protected Version createComponentFrom(NewComponentFamily family,
			String componentName, String description, Dataflow dataflow,
			License license, SharingPolicy sharingPolicy)
			throws RegistryException {
		ComponentType ct = client.post(ComponentType.class, objectFactory
				.createWorkflow(makeComponentVersionCreateRequest(
						componentName, description, dataflow, family, license,
						sharingPolicy)), COMPONENT_SERVICE, "elements="
				+ NewComponent.ELEMENTS);
		NewComponent nc = new NewComponent(this, family, ct);
		return nc.new Version(ct.getVersion(), description, dataflow);
	}

	protected Version createComponentVersionFrom(NewComponent component,
			String componentName, String description, Dataflow dataflow,
			License license, SharingPolicy sharingPolicy)
			throws RegistryException {
		ComponentType ct = client.post(ComponentType.class, objectFactory
				.createWorkflow(makeComponentVersionCreateRequest(
						componentName, description, dataflow, component.family,
						license, sharingPolicy)), COMPONENT_SERVICE, "id="
				+ component.getId(), "elements=" + NewComponent.ELEMENTS);
		return component.new Version(ct.getVersion(), description, dataflow);
	}

	public static synchronized NewComponentRegistry getComponentRegistry(
			URL registryBase) throws RegistryException {
		if (!componentRegistries.containsKey(registryBase.toExternalForm()))
			componentRegistries.put(registryBase.toExternalForm(),
					new NewComponentRegistry(registryBase));
		return componentRegistries.get(registryBase.toExternalForm());
	}

	public static boolean verifyBase(URL registryBase) {
		try {
			return new Client(jaxbContext, logger, registryBase).verify();
		} catch (Exception e) {
			logger.info("failed to construct connection client to "
					+ registryBase, e);
			return false;
		}
	}

	public License getLicense(String name) throws RegistryException {
		for (License l : getLicenses())
			if (l.getAbbreviation().equals(name))
				return l;
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof NewComponentRegistry) {
			NewComponentRegistry other = (NewComponentRegistry) o;
			return getRegistryBase().equals(other.getRegistryBase());
		}
		return false;
	}

	private static final int BASEHASH = NewComponentRegistry.class.hashCode();

	@Override
	public int hashCode() {
		return BASEHASH ^ getRegistryBase().hashCode();
	}
}
