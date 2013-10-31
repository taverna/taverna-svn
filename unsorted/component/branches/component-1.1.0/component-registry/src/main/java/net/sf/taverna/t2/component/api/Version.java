package net.sf.taverna.t2.component.api;

import java.net.URL;

import net.sf.taverna.t2.workflowmodel.Dataflow;

public interface Version {

	Integer getVersionNumber();

	String getDescription();

	Dataflow getDataflow() throws RegistryException;

	Component getComponent();

	interface ID {

		String getFamilyName();

		URL getRegistryBase();

		String getComponentName();

		Integer getComponentVersion();

		/**
		 * Tests whether this ID is equal to the given one, <i>excluding</i> the version.
		 * @param id The ID to compare to.
		 * @return A boolean
		 */
		boolean mostlyEqualTo(ID id);

		/**
		 * Tests whether this ID is equal to the given component, <i>excluding</i> the version.
		 * @param component The component to compare to.
		 * @return A boolean
		 */
		boolean mostlyEqualTo(Component component);
	}
}
