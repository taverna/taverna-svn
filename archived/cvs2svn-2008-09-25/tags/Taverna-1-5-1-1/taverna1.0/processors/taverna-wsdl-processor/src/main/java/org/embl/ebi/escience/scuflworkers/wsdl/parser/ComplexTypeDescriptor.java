package org.embl.ebi.escience.scuflworkers.wsdl.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * A TypeDescriptor that specifically describes a complex type
 * 
 */
public class ComplexTypeDescriptor extends TypeDescriptor {
	private List elements = new ArrayList();

	public List getElements() {
		return elements;
	}

	public void setElements(List elements) {
		this.elements = elements;
	}
}