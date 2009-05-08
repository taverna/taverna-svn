package net.sf.taverna.t2.reference.impl;

import net.sf.taverna.t2.reference.T2ReferenceGenerator;
import net.sf.taverna.t2.reference.T2ReferenceGeneratorFactory;

/**
 * Implementation of T2ReferenceGeneratorFactory which returns
 * UUIDT2ReferenceGenerator instances
 * 
 * @author Tom Oinn
 */
public class T2ReferenceGeneratorFactoryImpl implements
		T2ReferenceGeneratorFactory {

	public T2ReferenceGenerator getGeneratorWithNamespace(String namespace) {
		return new UUIDT2ReferenceGenerator(namespace);
	}

}
