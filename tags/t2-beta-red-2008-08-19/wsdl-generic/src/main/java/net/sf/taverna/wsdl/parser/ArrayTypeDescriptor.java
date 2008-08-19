package net.sf.taverna.wsdl.parser;


/**
 * A TypeDescriptor that specifically describes an array type
 * 
 */
public class ArrayTypeDescriptor extends TypeDescriptor {
	private TypeDescriptor elementType;
	private boolean wrapped;
	
	public boolean isWrapped() {
		return wrapped;		
	}

	public void setWrapped(boolean wrapped) {
		this.wrapped = wrapped;
	}

	public TypeDescriptor getElementType() {
		return elementType;
	}

	public void setElementType(TypeDescriptor elementType) {
		this.elementType = elementType;
	}

	@Override
	public String getName() {
		String name = super.getName();
		if (name == null) {
			return "ArrayOf" + getElementType().getType();
		}
		return name;
	}

}
