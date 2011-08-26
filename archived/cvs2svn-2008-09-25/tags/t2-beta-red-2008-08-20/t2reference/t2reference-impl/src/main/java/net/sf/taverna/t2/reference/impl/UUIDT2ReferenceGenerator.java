package net.sf.taverna.t2.reference.impl;

import java.util.UUID;

import net.sf.taverna.t2.reference.T2ReferenceGenerator;

/**
 * A T2ReferenceGenerator based on UUIDs. Not as fast as
 * {@link SimpleT2ReferenceGenerator}, but IDs will be globally unique.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class UUIDT2ReferenceGenerator extends AbstractT2ReferenceGenerator
		implements T2ReferenceGenerator {

	public String getNamespace() {
		return "uuid";
	}

	@Override
	protected String getNextLocalPart() {
		return UUID.randomUUID().toString();
	}

}
