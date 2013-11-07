/**
 * 
 */
package net.sf.taverna.t2.component.registry.myexperiment;

import org.jdom.Element;

import net.sf.taverna.t2.component.api.License;

/**
 * @author alson
 * 
 */
class MyExperimentLicense implements License {
	private String name;
	private String description;
	private String abbreviation;

	public MyExperimentLicense(MyExperimentComponentRegistry componentRegistry,
			String uri) {
		Element licenseElement = componentRegistry.getResource(uri);
		name = licenseElement.getChildTextTrim("title");
		description = licenseElement.getChildTextTrim("description");
		abbreviation = licenseElement.getChildTextTrim("unique-name");
	}

	@Override
	public String getAbbreviation() {
		return abbreviation;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
