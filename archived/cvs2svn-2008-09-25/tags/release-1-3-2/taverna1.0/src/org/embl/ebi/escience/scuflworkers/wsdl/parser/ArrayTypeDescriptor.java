package org.embl.ebi.escience.scuflworkers.wsdl.parser;

/**
 * A TypeDescriptor that specifically describes an array type
 * 
 */
public class ArrayTypeDescriptor extends TypeDescriptor {
	private TypeDescriptor elementType;

	public TypeDescriptor getElementType() {
		return elementType;
	}

	public void setElementType(TypeDescriptor elementType) {
		this.elementType = elementType;
	}
}
