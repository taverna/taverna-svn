package org.embl.ebi.escience.scuflworkers.wsdl.parser;

import java.io.IOException;
import java.util.*;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.description.TypeDesc;
import org.apache.axis.wsdl.gen.NoopFactory;
import org.apache.axis.wsdl.symbolTable.BaseType;
import org.apache.axis.wsdl.symbolTable.BindingEntry;
import org.apache.axis.wsdl.symbolTable.CollectionType;
import org.apache.axis.wsdl.symbolTable.DefinedElement;
import org.apache.axis.wsdl.symbolTable.DefinedType;
import org.apache.axis.wsdl.symbolTable.ElementDecl;
import org.apache.axis.wsdl.symbolTable.Parameter;
import org.apache.axis.wsdl.symbolTable.Parameters;
import org.apache.axis.wsdl.symbolTable.SymTabEntry;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.symbolTable.Type;
import org.apache.axis.wsdl.symbolTable.TypeEntry;
import org.xml.sax.SAXException;


import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;


/**
 * A Parser for processing WSDL files to determine information about available services and the required types
 * needed to invoke that particular service. Handles Complex Types and wsdl imports.
 * 
 */

public class WSDLParser 
{
	private String wsdlLocation;
	private SymbolTable symbolTable;
	private List operations;
	
	/**
	 * Constructor which takes the location of the base wsdl file, and begins to process it
	 * 
	 * @param wsdlLocation - the location of the wsdl file
	 * @throws ParserConfigurationException
	 * @throws WSDLException
	 * @throws IOException
	 * @throws SAXException
	 */
	public WSDLParser(String wsdlLocation) throws 	ParserConfigurationException,
													WSDLException,
													IOException,
													SAXException
	{
		this.wsdlLocation=wsdlLocation;
		this.symbolTable=new SymbolTable(new NoopFactory().getBaseTypeMapping(),true,false,false);
		symbolTable.populate(wsdlLocation);
		this.operations = determineOperations();
	}
	
	/**
	 * @return a list of WSDLOperations for all operations for this service, 
	 */
	public List getOperations()
	{
		return this.operations;
	}
	
	/**
	 * @return the wsdl location for which this parser was constructed
	 */
	public String getWSDLLocation()
	{
		return wsdlLocation;
	}
	
	/**
	 * @return the Definition for this service
	 */
	public Definition getDefinition()
	{
		return symbolTable.getDefinition();
	}
	
	/**
	 * Populates the lists for inputs and outputs, with TypeDescriptors for the parameters that the service requires
	 * or responds with respectively. //TODO needs modifying to handle overloaded operations
	 * 
	 * @param operationName
	 * @param inputs List of TypeDescriptor
	 * @param outputs List of TypeDescriptor
	 * @throws UnknownOperationException if no operation matches the name
	 * @throws IOException
	 */
	public void getOperationParameters(String operationName,List inputs, List outputs) throws UnknownOperationException, IOException
	{
		WSDLOperation operation = getOperation(operationName);
		if (operation==null)
		{
			throw new UnknownOperationException("operation called "+operationName+" does not exist for this wsdl");
		}
		
		Parameters parameters=symbolTable.getOperationParameters(operation.getOperation(),"",new BindingEntry(operation.getBinding()));
				
		for (Iterator iterator=parameters.list.iterator();iterator.hasNext();)
		{
			Parameter param=(Parameter)iterator.next();
			inputs.add(processParameter(param));
			
		}		
		outputs.add(processParameter(parameters.returnParam));		
	}
	
	/**
	 * Returns a WSDLOperation descriptor for an operation that matches the operationName. 
	 * @param operationName
	 * @return a matching WSDLOperation descriptor
	 * @throws UnknowOperationException if no operation matches the name
	 */
	public WSDLOperation getOperation(String operationName) throws UnknownOperationException
	{
		WSDLOperation result = null;
		
		for (Iterator iterator=getOperations().iterator();iterator.hasNext();)
		{
			WSDLOperation op = (WSDLOperation)iterator.next();
			if (op.getName().equals(operationName))
			{
				result=op;
				break;
			}
		}
		if (result==null) throw new UnknownOperationException("No operation named: "+operationName+" exists");
		return result;
	}
	
	private List determineOperations()
	{
		List result = new ArrayList();
		Map bindings=symbolTable.getDefinition().getBindings();
		for (Iterator iterator=bindings.values().iterator();iterator.hasNext();)
		{
			Binding binding = (Binding)iterator.next();
			List extensibilityElementList = binding.getExtensibilityElements();
		    for (Iterator k = extensibilityElementList.iterator(); k.hasNext(); ) 
		    {
		    	ExtensibilityElement ee = (ExtensibilityElement)k.next();
		    	if (ee instanceof SOAPBindingImpl)
		    	{
		    		SOAPBinding soapBinding = (SOAPBinding)ee;
		    		PortType portType = binding.getPortType();
		    		for (Iterator opIterator=portType.getOperations().iterator();opIterator.hasNext();)
		    		{
		    			Operation op=(Operation)opIterator.next();
		    			result.add(new WSDLOperation(op,binding,portType,soapBinding.getStyle()));
		    		}
		    	}
		    }			
		}
		
		return result;		
	}		
	
	private TypeDescriptor processParameter(Parameter param)
	{
		TypeDescriptor typeDesc=constructType(param.getType());
				
		typeDesc.setName(param.getName());
						
		return typeDesc;
	}
	
	private TypeDescriptor constructType(TypeEntry type)
	{
		TypeDescriptor result=null;
		if (type instanceof CollectionType)
		{
			result=constructArrayType(type);
			result.setType(type.getRefType().getQName().getLocalPart());
		}
		else if (type instanceof DefinedType || type instanceof DefinedElement)
		{
			if (type.getComponentType()==null)
			{
				if (type instanceof DefinedElement)
				{
					result=constructComplexType((DefinedElement)type);
				}
				else
				{
					result=constructComplexType((DefinedType)type);
				}
			}
			else
			{
				result=constructArrayType(type);				
			}
		}
		else
		{
			result=constructBaseType(type);			
		}				
		
		return result;
	}
	
	private ComplexTypeDescriptor constructComplexType(DefinedElement type)
	{
		ComplexTypeDescriptor result = new ComplexTypeDescriptor();
		result.setType(type.getQName().getLocalPart());
		List containedElements = type.getRefType().getContainedElements();
		if (containedElements!=null)
		{
			result.getElements().addAll(constructElements(containedElements));
		}
		
		return result;
	}
	
	private ComplexTypeDescriptor constructComplexType(DefinedType type)
	{
		ComplexTypeDescriptor result = new ComplexTypeDescriptor();
		result.setType(type.getQName().getLocalPart());
		List containedElements = type.getContainedElements();
		if (containedElements!=null)
		{
			result.getElements().addAll(constructElements(containedElements));
		}
		
		return result;
	}
	
	private List constructElements(List elements)
	{
		List result = new ArrayList();
		
		for (Iterator iterator=elements.iterator();iterator.hasNext();)
		{
			ElementDecl el = (ElementDecl)iterator.next();
			TypeDescriptor elType = constructType(el.getType());
			elType.setOptional(el.getOptional() || el.getMinOccursIs0());
			elType.setUnbounded(el.getMaxOccursIsUnbounded());
			elType.setName(el.getQName().getLocalPart());
			result.add(elType);
		}
		
		return result;
	}
	
	private ArrayTypeDescriptor constructArrayType(TypeEntry type)
	{
		ArrayTypeDescriptor result=new ArrayTypeDescriptor();		
		result.setElementType(constructType(type.getRefType()));
		result.setType(type.getQName().getLocalPart());
		
		return result;
	}
	
	private TypeDescriptor constructBaseType(TypeEntry type)
	{
		TypeDescriptor result = new BaseTypeDescriptor();
		result.setType(type.getQName().getLocalPart());
		return result;
	}				
			
	
	/**
	 * Value class to hold information about a particular webservice operation
	 *
	 */
	class WSDLOperation
	{		
		private Operation operation;
		private Binding binding;
		private PortType portType;
		private String style;
		
		public WSDLOperation(Operation operation,Binding binding, PortType portType, String style)
		{			
			this.operation=operation;
			this.binding=binding;
			this.portType=portType;
			this.style=style;
		}

		public Binding getBinding() 
		{
			return binding;
		}

		public String getName() 
		{
			return operation.getName();
		}

		public Operation getOperation() 
		{
			return operation;
		}

		public PortType getPortType() 
		{
			return portType;
		}

		public String getStyle() 
		{
			return style;
		}					
	}
	
	/**
	 * A TypeDescriptor that specifically describes a complex type 
	 *
	 */
	class ComplexTypeDescriptor extends TypeDescriptor
	{		
		private List elements=new ArrayList();
		public List getElements()
		{
			return elements;
		}
	}
	
	/**
	 * A TypeDescriptor that specifically describes an array type
	 * 
	 */
	class ArrayTypeDescriptor extends TypeDescriptor
	{
		private TypeDescriptor elementType;

		public TypeDescriptor getElementType() {
			return elementType;
		}

		public void setElementType(TypeDescriptor elementType) {
			this.elementType = elementType;
		}		
	}
	
	/**
	 * A TypeDescriptor specifically for basic types (e.g. string, float, int, base64binary)
	 *
	 */
	class BaseTypeDescriptor extends TypeDescriptor
	{
		
	}
	
	/**
	 * Base class for all descriptors for type
	 *
	 */
	class TypeDescriptor
	{
		private String name;
		private String type;
		private boolean optional;
		private boolean unbounded;
				
		
		public String getName() {
			return name;
		}
		public void setName(String name) 
		{
			int i;
			if ((i=name.lastIndexOf('>'))!=-1)
			{
				this.name=name.substring(i+1);
			}
			else
			{
				this.name = name;
			}
		}
		public boolean isOptional() {
			return optional;
		}
		public void setOptional(boolean optional) {
			this.optional = optional;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public boolean isUnbounded() {
			return unbounded;
		}
		public void setUnbounded(boolean unbounded) {
			this.unbounded = unbounded;
		}
		public String toString() 
		{
			return name+":"+type;
		}				
	}
}
