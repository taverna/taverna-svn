package net.sf.taverna.wsdl.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * A TypeDescriptor that specifically describes a complex type
 * 
 */
public class ComplexTypeDescriptor extends TypeDescriptor {
	private List<TypeDescriptor> elements = new ArrayList<TypeDescriptor>();

	public List<TypeDescriptor> getElements() {
		return elements;
	}

	public void setElements(List<TypeDescriptor> elements) {
		this.elements = elements;
	}
	
	public TypeDescriptor elementForName(String name) {
		TypeDescriptor result=null;
		for (TypeDescriptor desc : getElements()) {
			if (desc.getName().equals(name)) {
				result=desc;
				break;
			}
		}
		return result;
	}
}
