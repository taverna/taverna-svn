package net.sf.taverna.t2.component.registry.standard;

import static java.net.HttpURLConnection.HTTP_OK;
import static net.sf.taverna.t2.component.registry.standard.Utils.getElement;
import static net.sf.taverna.t2.component.registry.standard.Utils.serializeDataflow;

import java.io.Serializable;
import java.io.StringWriter;
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
import javax.xml.bind.JAXBElement;
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
import net.sf.taverna.t2.component.registry.standard.myexpclient.MyExperimentClient;
import net.sf.taverna.t2.component.registry.standard.myexpclient.ServerResponse;
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
import uk.org.taverna.component.api.PolicyList;

public class NewComponentRegistry extends ComponentRegistry {
	private static Map<String, NewComponentRegistry> componentRegistries = new HashMap<String, NewComponentRegistry>();
	static final Logger logger = Logger.getLogger(NewComponentRegistry.class);
	private final MyExperimentClient myExperimentClient;
	static final JAXBContext jaxbContext;
	static final Charset utf8;
	private static final ObjectFactory objectFactory = new ObjectFactory();

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

	protected NewComponentRegistry(URL registryBase) throws RegistryException {
		super(registryBase);
		try {
			myExperimentClient = new MyExperimentClient(logger);
			myExperimentClient.setBaseURL(registryBase.toExternalForm());
			myExperimentClient.doLogin();
		} catch (Exception e) {
			logger.error(e);
			throw new RegistryException("Unable to access registry", e);
		}
	}

	<T> T get(Class<T> clazz, String uri, String... query)
			throws RegistryException {
		StringBuilder uriBuilder = new StringBuilder(uri);
		for (String queryElement : query) {
			uriBuilder.append(uriBuilder.indexOf("?") < 0 ? "?" : "&");
			uriBuilder.append(queryElement);
		}
		try {
			ServerResponse response = myExperimentClient
					.doMyExperimentGET(getRegistryBaseString() + uriBuilder);
			if (response.getResponseCode() != HTTP_OK)
				throw new RegistryException("Unable to perform request "
						+ response.getResponseCode());
			return response.getResponse(jaxbContext, clazz);
		} catch (RegistryException e) {
			throw e;
		} catch (JAXBException e) {
			throw new RegistryException("Problem when unmarshalling response",
					e);
		} catch (Exception e) {
			logger.info("failed in GET to " + uriBuilder, e);
			throw new RegistryException("Problem when sending request", e);
		}
	}

	<T> T post(Class<T> clazz, JAXBElement<?> elem, String uri,
			String... strings) throws RegistryException {
		StringBuilder uriBuilder = new StringBuilder(uri);
		for (String queryElement : strings) {
			uriBuilder.append(uriBuilder.indexOf("?") < 0 ? "?" : "&");
			uriBuilder.append(queryElement);
		}
		try {
			StringWriter sw = new StringWriter();
			jaxbContext.createMarshaller().marshal(elem, sw);
			ServerResponse response = myExperimentClient.doMyExperimentPOST(
					getRegistryBaseString() + uriBuilder, sw.toString());
			if (response.getResponseCode() >= 400)
				throw new RegistryException("Unable to perform request "
						+ response.getResponseCode());
			return response.getResponse(jaxbContext, clazz);
		} catch (RegistryException e) {
			throw e;
		} catch (JAXBException e) {
			throw new RegistryException("Problem when marshalling request", e);
		} catch (Exception e) {
			logger.info("failed in POST to " + uriBuilder, e);
			throw new RegistryException("Problem when sending request", e);
		}
	}

	void delete(String uri, String... query) throws RegistryException {
		StringBuilder uriBuilder = new StringBuilder(uri);
		for (String queryElement : query) {
			uriBuilder.append(uriBuilder.indexOf("?") < 0 ? "?" : "&");
			uriBuilder.append(queryElement);
		}
		ServerResponse response;
		try {
			response = myExperimentClient
					.doMyExperimentDELETE(getRegistryBaseString() + uriBuilder);
		} catch (Exception e) {
			throw new RegistryException("Unable to perform request: "
					+ e.getMessage(), e);
		}
		if (response.getResponseCode() >= 400)
			throw new RegistryException(
					"Unable to perform request: result code "
							+ response.getResponseCode());
		return;
	}

	@Override
	protected void populateFamilyCache() throws RegistryException {
		for (Profile pr : getComponentProfiles()) {
			NewComponentProfile p = (NewComponentProfile) pr;
			ComponentFamilyList cfl = get(ComponentFamilyList.class,
					"/component-families.xml",
					"component-profile=" + p.getUri(), "elements="
							+ NewComponentFamily.ELEMENTS);
			for (ComponentFamilyDescription cfd : cfl.getPack()) {
				String title = getElement(cfd, "title").getValue().toString()
						.trim();
				familyCache.put(title, new NewComponentFamily(this, p, cfd));
			}
		}
	}

	ComponentType getComponent(String id, Integer version, String elements)
			throws RegistryException {
		if (version != null)
			return get(ComponentType.class, "/component.xml", "id=" + id,
					"version=" + version, "elements=" + elements);
		return get(ComponentType.class, "/component.xml", "id=" + id,
				"elements=" + elements);
	}

	ComponentFamilyType getComponentFamily(String id, String elements)
			throws RegistryException {
		return get(ComponentFamilyType.class, "/pack.xml", "id=" + id,
				"elements=" + elements);
	}

	ComponentProfileType getComponentProfile(String id, String elements)
			throws RegistryException {
		return get(ComponentProfileType.class, "/file.xml", "id=" + id,
				"elements=" + elements);
	}

	private JAXBElement<ComponentFamilyType> makeComponentFamilyCreateRequest(
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
		if (sharingPolicy == null)
			sharingPolicy = Policy.PRIVATE;
		familyDoc.setPermissions(((Policy) sharingPolicy)
				.getPermissionsElement());

		return objectFactory.createPack(familyDoc);
	}

	@Override
	protected Family internalCreateComponentFamily(String familyName,
			Profile componentProfile, String description, License license,
			SharingPolicy sharingPolicy) throws RegistryException {
		NewComponentProfile profile = (NewComponentProfile) componentProfile;

		return new NewComponentFamily(this, profile, post(
				ComponentFamilyType.class,
				makeComponentFamilyCreateRequest(profile, familyName,
						description, license, sharingPolicy),
				"/component-family.xml", "elements="
						+ NewComponentFamily.ELEMENTS));
	}

	@Override
	protected void internalRemoveComponentFamily(Family componentFamily)
			throws RegistryException {
		NewComponentFamily ncf = (NewComponentFamily) componentFamily;
		delete("/workflow.xml", "id=" + ncf.getId());
	}

	@Override
	protected void populateProfileCache() throws RegistryException {
		ComponentProfileList cpl = get(ComponentProfileList.class,
				"/component-profiles.xml", "elements="
						+ NewComponentProfile.ELEMENTS);
		if (cpl == null)
			return;
		for (ComponentProfileDescription cpd : cpl.getFile()) {
			if (cpd.getUri() == null || cpd.getUri().isEmpty())
				continue;
			profileCache.add(new NewComponentProfile(this, cpd));
		}
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
					return new NewComponentProfile(this, getComponentProfile(
							profile.getId(), NewComponentProfile.ELEMENTS));
			}
		} catch (RegistryException e) {
			// Do nothing but fall through
		}
		return new NewComponentProfile(this, post(
				ComponentProfileType.class,
				makeComponentProfileCreateRequest(componentProfile.getName(),
						componentProfile.getDescription(),
						componentProfile.getXML(), license, sharingPolicy),
				"/component-profile.xml", "elements="
						+ NewComponentProfile.ELEMENTS));
	}

	private JAXBElement<ComponentProfileType> makeComponentProfileCreateRequest(
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
		if (sharingPolicy == null)
			sharingPolicy = Policy.PRIVATE;
		profile.setPermissions(((Policy) sharingPolicy).getPermissionsElement());

		return objectFactory.createFile(profile);
	}

	private JAXBElement<ComponentType> makeComponentVersionCreateRequest(
			String title, String description, String content,
			NewComponentFamily family, License license,
			SharingPolicy sharingPolicy) throws RegistryException {
		ComponentType comp = new ComponentType();

		comp.setTitle(title);
		comp.setDescription(description);
		comp.setComponentFamily(family.getUri());
		comp.setContentType("application/vnd.taverna.t2flow+xml");
		comp.setContent(new Content());
		comp.getContent().setEncoding("base64");
		comp.getContent().setType("binary");
		comp.getContent().setValue(content.getBytes(utf8));
		if (license == null)
			license = getPreferredLicense();
		if (license != null) {
			comp.setLicenseType(new Description());
			comp.getLicenseType().getContent().add(license.getAbbreviation());
		}
		if (sharingPolicy == null)
			sharingPolicy = Policy.PRIVATE;
		comp.setPermissions(((Policy) sharingPolicy).getPermissionsElement());

		return objectFactory.createWorkflow(comp);
	}

	@Override
	protected void populatePermissionCache() {
		permissionCache.add(Policy.PUBLIC);
		permissionCache.add(Policy.PRIVATE);
		try {
			for (Description d : get(PolicyList.class, "/policies.xml",
					"type=group").getPolicy())
				permissionCache.add(new Policy.Group(d.getId()));
		} catch (RegistryException e) {
			logger.warn("failed to fetch sharing policies", e);
			return;
		}
	}

	@Override
	protected void populateLicenseCache() {
		LicenseList licenses;
		try {
			licenses = get(LicenseList.class, "/licenses.xml", "elements="
					+ NewComponentLicense.ELEMENTS);
		} catch (RegistryException e) {
			logger.warn("failed to fetch licenses", e);
			return;
		}
		for (LicenseType lt : licenses.getLicense())
			licenseCache.add(new NewComponentLicense(this, lt));
	}

	@Override
	public License getPreferredLicense() throws RegistryException {
		return getLicenseByAbbreviation("by-nd");
	}

	@Override
	public Set<ID> searchForComponents(String prefixes, String text)
			throws RegistryException {
		HashSet<ID> versions = new HashSet<ID>();
		for (ComponentDescription cd : get(ComponentDescriptionList.class,
				"/components.xml", "query=" + text, "prefixes=" + prefixes,
				"elements=" + NewComponent.ELEMENTS).getWorkflow()) {
			NewComponent nc = null;
			for (Family f : getComponentFamilies()) {
				if (!(f instanceof NewComponentFamily))
					continue;
				nc = (NewComponent) ((NewComponentFamily) f)
						.getComponent(getElement(cd, "title").getValue()
								.toString().trim());
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
		for (ComponentDescription cd : get(ComponentDescriptionList.class,
				"/components.xml", "component-family=" + family.getUri(),
				"elements=" + NewComponent.ELEMENTS).getWorkflow())
			result.add(new NewComponent(this, family, cd));
		return result;
	}

	protected void deleteComponent(NewComponent component)
			throws RegistryException {
		delete("/workflow.xml", "id=" + component.getId());
	}

	protected Version createComponentFrom(NewComponentFamily family,
			String componentName, String description, Dataflow dataflow,
			License license, SharingPolicy sharingPolicy)
			throws RegistryException {
		ComponentType ct = post(
				ComponentType.class,
				makeComponentVersionCreateRequest(componentName, description,
						serializeDataflow(dataflow), family, license,
						sharingPolicy), "/component.xml", "elements="
						+ NewComponent.ELEMENTS);
		NewComponent nc = new NewComponent(this, family, ct);
		return nc.new Version(ct.getVersion(), description, dataflow);
	}

	protected Version createComponentVersionFrom(NewComponent component,
			String componentName, String description, Dataflow dataflow,
			License license, SharingPolicy sharingPolicy)
			throws RegistryException {
		ComponentType ct = post(
				ComponentType.class,
				makeComponentVersionCreateRequest(componentName, description,
						serializeDataflow(dataflow), component.family, license,
						sharingPolicy), "/component.xml",
				"id=" + component.getId(), "elements=" + NewComponent.ELEMENTS);
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
			MyExperimentClient myExperimentClient = new MyExperimentClient(
					logger);
			myExperimentClient.setBaseURL(registryBase.toExternalForm());
			myExperimentClient.doLogin();
			return myExperimentClient.doMyExperimentGET(
					registryBase + "/component-profiles.xml").getResponseCode() == HTTP_OK;
		} catch (Exception e) {
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
