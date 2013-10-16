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
		
	}
}
