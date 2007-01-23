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

	public String getName() {
		String name = super.getName();
		if (name == null) {
			return "ArrayOf" + getElementType().getType();
		}
		return name;
	}

}
