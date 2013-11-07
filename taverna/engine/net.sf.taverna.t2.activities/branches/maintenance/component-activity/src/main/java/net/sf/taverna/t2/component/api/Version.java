package net.sf.taverna.t2.component.api;

import java.io.Serializable;
import java.net.URL;

import net.sf.taverna.t2.workflowmodel.Dataflow;

public interface Version {
	/** @return The version number of this version */
	Integer getVersionNumber();

	/** @return The description of this version */
	String getDescription();

	/** @return The implementation for this version */
	Dataflow getDataflow() throws RegistryException;

	/** @return The component of which this is a version */
	Component getComponent();

	/** @return The identification token for this version */
	ID getID();

	interface ID extends Serializable {
		/** @return The name of the family of the component to which we refer to */
		String getFamilyName();

		/** @return The base URL of the registry containing the component */
		URL getRegistryBase();

		/**
		 * @return The name of the component referred to, unique within its
		 *         family
		 */
		String getComponentName();

		/**
		 * @return The version number of the version of the component referred
		 *         to
		 */
		Integer getComponentVersion();

		/**
		 * Tests whether this ID is equal to the given one, <i>excluding</i> the
		 * version.
		 * 
		 * @param id
		 *            The ID to compare to.
		 * @return A boolean
		 */
		boolean mostlyEqualTo(ID id);

		/**
		 * Tests whether this ID is equal to the given component,
		 * <i>excluding</i> the version.
		 * 
		 * @param component
		 *            The component to compare to.
		 * @return A boolean
		 */
		boolean mostlyEqualTo(Component component);
	}
}
