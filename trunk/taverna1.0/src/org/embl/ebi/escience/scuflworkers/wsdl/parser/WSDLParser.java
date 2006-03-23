package org.embl.ebi.escience.scuflworkers.wsdl.parser;

import java.io.IOException;
import java.util.*;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.wsdl.gen.NoopFactory;
import org.apache.axis.wsdl.symbolTable.BindingEntry;
import org.apache.axis.wsdl.symbolTable.CollectionType;
import org.apache.axis.wsdl.symbolTable.DefinedElement;
import org.apache.axis.wsdl.symbolTable.DefinedType;
import org.apache.axis.wsdl.symbolTable.ElementDecl;
import org.apache.axis.wsdl.symbolTable.Parameter;
import org.apache.axis.wsdl.symbolTable.Parameters;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.symbolTable.TypeEntry;
import org.apache.log4j.Logger;
import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
import org.apache.wsif.util.WSIFPluggableProviders;
import org.xml.sax.SAXException;

import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;

/**
 * A Parser for processing WSDL files to determine information about available
 * services and the required types needed to invoke that particular service.
 * Handles Complex Types and wsdl imports.
 * 
 */

public class WSDLParser {
	private String wsdlLocation;

	private static Logger logger = Logger.getLogger(WSDLParser.class);

	/**
	 * Cache for SymbolTable to remove the need for reprocessing each time.
	 */
	private static Map symbolTableMap = Collections.synchronizedMap(new HashMap());

	/**
	 * Cache for operations, to remove the need for reprocessing each time.
	 */
	private static Map operationMap = Collections.synchronizedMap(new HashMap());

	private static Map bindingMap = Collections.synchronizedMap(new HashMap());

	private static Map styleMap = Collections.synchronizedMap(new HashMap());

	private static Map portTypeMap = Collections.synchronizedMap(new HashMap());

	private Map cachedComplexTypes = Collections.synchronizedMap(new HashMap());

	/**
	 * Constructor which takes the location of the base wsdl file, and begins to
	 * process it
	 * 
	 * @param wsdlLocation -
	 *            the location of the wsdl file
	 * @throws ParserConfigurationException
	 * @throws WSDLException
	 * @throws IOException
	 * @throws SAXException
	 */
	public WSDLParser(String wsdlLocation) throws ParserConfigurationException, WSDLException, IOException,
			SAXException {

		this.wsdlLocation = wsdlLocation;

		WSIFPluggableProviders.overrideDefaultProvider("http://schemas.xmlsoap.org/wsdl/soap/",
				new WSIFDynamicProvider_ApacheAxis());

		if (!symbolTableMap.containsKey(wsdlLocation)) {
			SymbolTable symbolTable = new SymbolTable(new NoopFactory().getBaseTypeMapping(), true, false, false);
			symbolTable.populate(wsdlLocation);
			symbolTableMap.put(wsdlLocation, symbolTable);
			operationMap.put(wsdlLocation, determineOperations());
		}
	}

	/**
	 * @return a list of WSDLOperations for all operations for this service,
	 */
	public List getOperations() {
		return (List) operationMap.get(getWSDLLocation());
	}

	/**
	 * @return the wsdl location for which this parser was constructed
	 */
	public String getWSDLLocation() {
		return wsdlLocation;
	}

	/**
	 * @return the Definition for this service
	 */
	public Definition getDefinition() {
		return getSymbolTable().getDefinition();
	}

	public Binding getBinding() {
		return (Binding) bindingMap.get(getWSDLLocation());
	}

	public String getStyle() {
		return (String) styleMap.get(getWSDLLocation());
	}

	public PortType getPortType() {
		return (PortType) portTypeMap.get(getWSDLLocation());
	}

	/**
	 * Populates the lists for inputs and outputs, with TypeDescriptors for the
	 * parameters that the service requires or responds with respectively.
	 * //TODO needs modifying to handle overloaded operations
	 * 
	 * @param operationName
	 * @param inputs
	 *            List of TypeDescriptor
	 * @param outputs
	 *            List of TypeDescriptor
	 * @throws UnknownOperationException
	 *             if no operation matches the name
	 * @throws IOException
	 */
	public void getOperationParameters(String operationName, List inputs, List outputs)
			throws UnknownOperationException, IOException {
		Operation operation = getOperation(operationName);
		if (operation == null) {
			throw new UnknownOperationException("operation called " + operationName + " does not exist for this wsdl");
		}

		Parameters parameters = getSymbolTable().getOperationParameters(operation, "", new BindingEntry(getBinding()));

		for (Iterator iterator = parameters.list.iterator(); iterator.hasNext();) {
			Parameter param = (Parameter) iterator.next();
			inputs.add(processParameter(param));

		}
		if (parameters.returnParam != null) {
			if (parameters.returnParam.getType().isBaseType())
				outputs.add(processParameter(parameters.returnParam));
			else
			{
				ComplexTypeDescriptor complex=new ComplexTypeDescriptor();
				complex.setName(parameters.returnParam.getName());
				complex.setType(parameters.returnParam.getType().getQName().getLocalPart());
				outputs.add(complex);
			}
				
		}
		
		cachedComplexTypes.clear();
	}

	/**
	 * Provides the documentation for the given operation name, or returns an
	 * empty string if no documentation is provided by the WSDL.
	 * 
	 * @param operationName
	 * @return
	 * @throws UnknownOperationException
	 */
	public String getOperationDocumentation(String operationName) throws UnknownOperationException {
		String result = "";

		Operation operation = getOperation(operationName);
		if (operation.getDocumentationElement() != null) {
			if (operation.getDocumentationElement().getFirstChild() != null) {
				result = operation.getDocumentationElement().getFirstChild().getNodeValue();
			}
		}

		return result;
	}

	/**
	 * Returns a WSDLOperation descriptor for an operation that matches the
	 * operationName.
	 * 
	 * @param operationName
	 * @return a matching WSDLOperation descriptor
	 * @throws UnknowOperationException
	 *             if no operation matches the name
	 */
	public Operation getOperation(String operationName) throws UnknownOperationException {
		Operation result = null;

		for (Iterator iterator = getOperations().iterator(); iterator.hasNext();) {
			Operation op = (Operation) iterator.next();
			if (op.getName().equals(operationName)) {
				result = op;
				break;
			}
		}
		if (result == null)
			throw new UnknownOperationException("No operation named: " + operationName + " exists");
		return result;
	}

	private SymbolTable getSymbolTable() {
		return (SymbolTable) symbolTableMap.get(getWSDLLocation());
	}

	private List determineOperations() {
		List result = new ArrayList();
		Map bindings = getSymbolTable().getDefinition().getBindings();
		for (Iterator iterator = bindings.values().iterator(); iterator.hasNext();) {
			Binding binding = (Binding) iterator.next();
			List extensibilityElementList = binding.getExtensibilityElements();
			for (Iterator k = extensibilityElementList.iterator(); k.hasNext();) {
				ExtensibilityElement ee = (ExtensibilityElement) k.next();
				if (ee instanceof SOAPBindingImpl) {
					SOAPBinding soapBinding = (SOAPBinding) ee;
					PortType portType = binding.getPortType();

					bindingMap.put(getWSDLLocation(), binding);
					styleMap.put(getWSDLLocation(), soapBinding.getStyle());
					portTypeMap.put(getWSDLLocation(), portType);

					for (Iterator opIterator = portType.getOperations().iterator(); opIterator.hasNext();) {
						Operation op = (Operation) opIterator.next();
						result.add(op);
					}
				}
			}
		}

		return result;
	}

	private TypeDescriptor processParameter(Parameter param) {
		TypeDescriptor typeDesc = constructType(param.getType());

		typeDesc.setName(param.getName());

		return typeDesc;
	}

	private TypeDescriptor constructType(TypeEntry type) {
		TypeDescriptor result = null;
		if (type instanceof CollectionType) {
			result = constructArrayType(type);
			result.setType(type.getRefType().getQName().getLocalPart());
		} else if (type instanceof DefinedType || type instanceof DefinedElement) {
			if (type.getComponentType() == null) {
				if (type instanceof DefinedElement) {
					if (type.isBaseType()) {
						result = constructBaseType((DefinedElement) type);
					} else {
						result = constructComplexType((DefinedElement) type);
					}
				} else {
					result = constructComplexType((DefinedType) type);
				}
			} else {
				result = constructArrayType(type);
			}
		} else {
			result = constructBaseType(type);
		}
		
		return result;
	}

	private ComplexTypeDescriptor constructComplexType(DefinedElement type) {
		
		ComplexTypeDescriptor result = new ComplexTypeDescriptor();

		if (cachedComplexTypes.get(type.getQName().toString()) != null) {
			result = (ComplexTypeDescriptor) cachedComplexTypes.get(type.getQName().toString());
		} else {
			logger.debug("Constructing complex type: " + type.getQName().getLocalPart());
			// caching the type is not really to improve performance, but is
			// to handle types that contain elements that reference
			// itself or another parent. Without the caching, this could lead to infinate
			// recursion.
			if (cachedComplexTypes.get(type.getQName().toString()) == null)
				cachedComplexTypes.put(type.getQName().toString(), result);

			result.setType(type.getQName().getLocalPart());
			List containedElements = type.getRefType().getContainedElements();
			if (containedElements != null) {
				result.getElements().addAll(constructElements(containedElements));
			}
		}

		return result;
	}

	private ComplexTypeDescriptor constructComplexType(DefinedType type) {
		ComplexTypeDescriptor result = new ComplexTypeDescriptor();
		result.setType(type.getQName().getLocalPart());
		List containedElements = type.getContainedElements();
		if (containedElements != null) {
			result.getElements().addAll(constructElements(containedElements));
		}

		return result;
	}

	private List constructElements(List elements) {
		List result = new ArrayList();

		for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
			ElementDecl el = (ElementDecl) iterator.next();
			TypeDescriptor elType = constructType(el.getType());
			elType.setOptional(el.getOptional() || el.getMinOccursIs0());
			elType.setUnbounded(el.getMaxOccursIsUnbounded());
			elType.setName(el.getQName().getLocalPart());
			result.add(elType);
		}

		return result;
	}

	private ArrayTypeDescriptor constructArrayType(TypeEntry type) {
		ArrayTypeDescriptor result = new ArrayTypeDescriptor();
		result.setElementType(constructType(type.getRefType()));
		result.setType(type.getQName().getLocalPart());

		return result;
	}

	private BaseTypeDescriptor constructBaseType(TypeEntry type) {
		BaseTypeDescriptor result = new BaseTypeDescriptor();
		result.setType(type.getQName().getLocalPart());
		return result;
	}

	private BaseTypeDescriptor constructBaseType(DefinedElement type) {
		BaseTypeDescriptor result = null;
		if (type.getRefType() == null) {
			result = constructBaseType((TypeEntry) type);
		} else {
			result = new BaseTypeDescriptor();
			result.setType(type.getRefType().getQName().getLocalPart());
		}
		return result;
	}

}
