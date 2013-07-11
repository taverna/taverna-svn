/**
 * 
 */
package net.sf.taverna.t2.component.registry.myexperiment;

import org.jdom.Element;

import net.sf.taverna.t2.component.registry.License;

/**
 * @author alson
 *
 */
public class MyExperimentLicense implements License {

	private String name;
	private String description;
	private String abbreviation;

	public MyExperimentLicense(
			MyExperimentComponentRegistry componentRegistry,
			String uri) {
				Element licenseElement = componentRegistry.getResource(uri);
				name = licenseElement.getChildTextTrim("title");
				description = licenseElement.getChildTextTrim("description");
				abbreviation = licenseElement.getChildTextTrim("unique-name");
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.License#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.component.registry.License#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

}
