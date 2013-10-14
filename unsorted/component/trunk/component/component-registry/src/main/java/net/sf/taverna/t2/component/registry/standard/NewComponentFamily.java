package net.sf.taverna.t2.component.registry.standard;

import static net.sf.taverna.t2.component.registry.standard.Utils.getAnnotation;
import static net.sf.taverna.t2.component.registry.standard.Utils.getElementString;
import net.sf.taverna.t2.annotation.annotationbeans.DescriptiveTitle;
import net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription;
import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import uk.org.taverna.component.api.ComponentFamilyDescription;

/**
 * A family of components in the new-interface registry.
 * 
 * @author Donal Fellows
 */
public class NewComponentFamily extends ComponentFamily {
	static final String ELEMENTS = "title,description";

	NewComponentRegistry registry;
	NewComponentProfile profile;
	String id;
	String name;
	String description;
	private final String uri;

	NewComponentFamily(NewComponentRegistry componentRegistry,
			NewComponentProfile profile, ComponentFamilyDescription familyDesc)
			throws RegistryException {
		super(componentRegistry);
		uri = familyDesc.getUri();
		registry = componentRegistry;
		id = familyDesc.getId().trim();
		name = getElementString(familyDesc, "title");
		description = getElementString(familyDesc, "description");
		this.profile = profile;
	}

	@Override
	protected String internalGetName() {
		return name;
	}

	@Override
	protected String internalGetDescription() {
		return description;
	}

	@Override
	protected Profile internalGetComponentProfile() throws RegistryException {
		return profile;
	}

	@Override
	protected void populateComponentCache() throws RegistryException {
		for (Component c : registry.listComponents(this)) {
			NewComponent component = (NewComponent) c;
			componentCache.put(component.getName(), component);
		}
	}

	@Override
	protected Version internalCreateComponentBasedOn(String componentName,
			String description, Dataflow dataflow) throws RegistryException {
		if (componentName == null)
			componentName = getAnnotation(dataflow, DescriptiveTitle.class,
					"Untitled");
		if (description == null)
			description = getAnnotation(dataflow, FreeTextDescription.class,
					"Undescribed");
		return registry.createComponentFrom(this, componentName, description,
				dataflow, null, null);
	}

	@Override
	protected void internalRemoveComponent(Component component)
			throws RegistryException {
		registry.deleteComponent((NewComponent) component);
	}

	String getId() {
		return id;
	}

	public String getUri() {
		return uri;
	}
}
