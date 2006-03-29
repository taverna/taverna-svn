package org.embl.ebi.escience.scuflworkers.wsdl.parser;

import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Operation;

import junit.framework.TestCase;

public class WSDLParserTest extends TestCase {

	public void testGetOperations() throws Exception {
		WSDLParser parser = new WSDLParser("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils_lite.wsdl");
		List operations = parser.getOperations();
		assertEquals("wrong number of operations found (wsdl may have changed)", 12, operations.size());
		Operation op = (Operation) operations.get(0);
		assertEquals("wrong name for first operation", "run_eGquery", op.getName());
		assertEquals("wrong style", "document", parser.getStyle());
	}

	public void testGetOperationParameters() throws Exception {
		WSDLParser parser = new WSDLParser("http://www.ebi.ac.uk/ws/WSFasta.wsdl");
		List inputs = new ArrayList();
		List outputs = new ArrayList();
		parser.getOperationParameters("doFasta", inputs, outputs);
		assertEquals("wrong number of inputs", 2, inputs.size());
		assertTrue("wrong class type for descriptor - should be ComplexTypeDescriptor",
				inputs.get(0) instanceof ComplexTypeDescriptor);
		assertEquals("wrong type", "inputParams", ((TypeDescriptor) inputs.get(0)).getType());
		assertEquals("wrong name", "params", ((TypeDescriptor) inputs.get(0)).getName());

		List inputParamsElements = ((ComplexTypeDescriptor) inputs.get(0)).getElements();

		assertEquals("wrong number of elements", 18, inputParamsElements.size());

		assertEquals("wrong name for first element", "program", ((TypeDescriptor) inputParamsElements.get(0)).getName());
		assertEquals("wrong name for last element", "email", ((TypeDescriptor) inputParamsElements.get(17)).getName());
		assertEquals("wrong type for first element", "string", ((TypeDescriptor) inputParamsElements.get(0)).getType());
		assertEquals("wrong type for last element", "string", ((TypeDescriptor) inputParamsElements.get(17)).getType());

		assertTrue("wrong class type for descriptor - should be TypeDescriptor",
				inputs.get(1) instanceof TypeDescriptor);
		assertEquals("wrong type", "base64binary", ((TypeDescriptor) inputs.get(1)).getType());
		assertEquals("wrong name", "content", ((TypeDescriptor) inputs.get(1)).getName());

		assertEquals("wrong number of outputs", 1, outputs.size());
		assertTrue("wrong class type for descriptor - should be TypeDescriptor",
				outputs.get(0) instanceof TypeDescriptor);
		assertEquals("wrong type", "base64binary", ((TypeDescriptor) outputs.get(0)).getType());
		assertEquals("wrong name", "result", ((TypeDescriptor) outputs.get(0)).getName());

	}

	public void testComplexTypeFromImport() throws Exception {
		WSDLParser parser = new WSDLParser("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils_lite.wsdl");
		List inputs = new ArrayList();
		List outputs = new ArrayList();
		parser.getOperationParameters("run_eInfo", inputs, outputs);
		assertEquals("wrong number of inputs", 1, inputs.size());
		assertEquals("wrong number of outputs", 1, outputs.size());
		assertTrue("input should be complex", inputs.get(0) instanceof ComplexTypeDescriptor);
		ComplexTypeDescriptor complexTypeDesc = (ComplexTypeDescriptor) inputs.get(0);
		assertEquals("wrong name", "parameters", complexTypeDesc.getName());
		assertEquals("wrong number of elements", 3, complexTypeDesc.getElements().size());

		TypeDescriptor typeDesc = (TypeDescriptor) complexTypeDesc.getElements().get(0);

		assertEquals("wrong name", "db", typeDesc.getName());
		assertEquals("wrong type", "string", typeDesc.getType());
		assertTrue("db should be optional", typeDesc.isOptional());
		assertFalse("db should not be unbounded", typeDesc.isUnbounded());

		typeDesc = (TypeDescriptor) complexTypeDesc.getElements().get(1);
		assertEquals("wrong name", "tool", typeDesc.getName());
		assertEquals("wrong type", "string", typeDesc.getType());
		assertTrue("tool should be optional", typeDesc.isOptional());
		assertFalse("tool should not be unbounded", typeDesc.isUnbounded());

		typeDesc = (TypeDescriptor) complexTypeDesc.getElements().get(2);
		assertEquals("wrong name", "email", typeDesc.getName());
		assertEquals("wrong type", "string", typeDesc.getType());
		assertTrue("email should be optional", typeDesc.isOptional());
		assertFalse("email should not be unbounded", typeDesc.isUnbounded());
	}

	public void testNestedComplexTypes() throws Exception {
		WSDLParser parser = new WSDLParser("http://soap.bind.ca/wsdl/bind.wsdl");
		List inputs = new ArrayList();
		List outputs = new ArrayList();
		parser.getOperationParameters("BIVGetComplexRecord", inputs, outputs);

		assertEquals("wrong number of inputs", 1, inputs.size());
		assertEquals("wrong number of outputs", 1, outputs.size());

		assertEquals("wrong name for input", "bid", ((TypeDescriptor) inputs.get(0)).getName());
		assertEquals("wrong type for input", "int", ((TypeDescriptor) inputs.get(0)).getType());

		assertEquals("wrong name for output", "BIVComplex", ((TypeDescriptor) outputs.get(0)).getName());
		assertEquals("wrong type for output", "BIVComplex", ((TypeDescriptor) outputs.get(0)).getType());
		assertTrue("wrong descriptor class for output", outputs.get(0) instanceof ComplexTypeDescriptor);

		/* Commented out, outputs are currently unparsed
		ComplexTypeDescriptor typeDesc = (ComplexTypeDescriptor) outputs.get(0);
		assertEquals("wrong number of inner elements", 3, typeDesc.getElements().size());
		assertEquals("wrong name for first element", "bid", ((TypeDescriptor) typeDesc.getElements().get(0)).getName());
		assertEquals("wrong name for 2nd element", "spokeModel", ((TypeDescriptor) typeDesc.getElements().get(1))
				.getName());
		assertEquals("wrong name for 3rd element", "subunit", ((TypeDescriptor) typeDesc.getElements().get(2))
				.getName());

		assertTrue("3rd element should be instance of ArrayTypeDescriptor",
				typeDesc.getElements().get(2) instanceof ArrayTypeDescriptor);
		ArrayTypeDescriptor arrayTypeDesc = (ArrayTypeDescriptor) typeDesc.getElements().get(2);

		assertEquals("wrong type for 3rd element", "BIVMolecule", arrayTypeDesc.getType());

		typeDesc = (ComplexTypeDescriptor) arrayTypeDesc.getElementType();

		assertEquals("wrong type for 3rd element", "BIVMolecule", typeDesc.getType());

		assertEquals("wrong number of elements in nested complex type", 7, typeDesc.getElements().size());
		assertEquals("wrong name for first element", "id", ((TypeDescriptor) typeDesc.getElements().get(0)).getName());
		assertEquals("wrong type for first element", "int", ((TypeDescriptor) typeDesc.getElements().get(0)).getType());

		assertEquals("wrong name for last element", "smid-hits", ((TypeDescriptor) typeDesc.getElements().get(6))
				.getName());
		assertEquals("wrong type for last element", "int", ((TypeDescriptor) typeDesc.getElements().get(6)).getType());
		*/
	}

	public void testSimpleTypes() throws Exception {
		WSDLParser parser = new WSDLParser("http://soap.bind.ca/wsdl/bind.wsdl");
		List inputs = new ArrayList();
		List outputs = new ArrayList();
		parser.getOperationParameters("BIVGetRecord", inputs, outputs);
		assertEquals("wrong number of inputs", 1, inputs.size());
		assertTrue("should not be base type", inputs.get(0) instanceof BaseTypeDescriptor);
		assertEquals("wrong name", "bid", ((TypeDescriptor) inputs.get(0)).getName());
		assertEquals("wrong type", "int", ((TypeDescriptor) inputs.get(0)).getType());
	}

	public void testArrayType() throws Exception {
		WSDLParser parser = new WSDLParser("http://soap.bind.ca/wsdl/bind.wsdl");
		List inputs = new ArrayList();
		List outputs = new ArrayList();
		parser.getOperationParameters("BIVGetRecords", inputs, outputs);
		assertEquals("wrong number of inputs", 1, inputs.size());
		assertTrue("input should be of AArrayTypeDescriptor", inputs.get(0) instanceof ArrayTypeDescriptor);

		ArrayTypeDescriptor arrayTypeDesc = (ArrayTypeDescriptor) inputs.get(0);

		assertEquals("wrong name", "ids", arrayTypeDesc.getName());
		assertEquals("wrong type", "ArrayOf_xsd_int", arrayTypeDesc.getType());

		TypeDescriptor typeDesc = arrayTypeDesc.getElementType();

		assertTrue("element should be of type BaseTypeDescriptor", typeDesc instanceof BaseTypeDescriptor);
		assertEquals("wrong type", "int", typeDesc.getType());

		assertEquals("wrong number of outputs", 1, outputs.size());
		
//		assertTrue("output should be of ArrayTypeDescriptor", outputs.get(0) instanceof ArrayTypeDescriptor);
//
//		arrayTypeDesc = (ArrayTypeDescriptor) outputs.get(0);
//		assertEquals("wrong name", "BIVRecords", arrayTypeDesc.getName());
//		assertEquals("wrong type", "ArrayOfBIVRecord", arrayTypeDesc.getType());
//
//		typeDesc = arrayTypeDesc.getElementType();
//
//		assertEquals("wrong type", "BIVRecord", typeDesc.getType());
	}

	public void testGoVizNoOutputs() throws Exception {
		WSDLParser parser = new WSDLParser("http://www.ebi.ac.uk/collab/mygrid/service1/goviz/GoViz.jws?wsdl");
		List inputs = new ArrayList();
		List outputs = new ArrayList();
		parser.getOperationParameters("destroySession", inputs, outputs);

		assertEquals("wrong number of inputs", 1, inputs.size());
		assertEquals("wrong number of outputs", 0, outputs.size());

		TypeDescriptor typeDesc = (TypeDescriptor) inputs.get(0);
		assertTrue("input should be BaseType", typeDesc instanceof BaseTypeDescriptor);
		assertEquals("wrong name", "sessionID", typeDesc.getName());
		assertEquals("wrong type", "string", typeDesc.getType());
	}

	public void testGetOperationDocumentation() throws Exception
	{
		WSDLParser parser = new WSDLParser("http://services.serkoonline.com/serkoservices2.asmx?WSDL");
		String doc = parser.getOperationDocumentation("Login");
		assertTrue("this service should contain some documentation, and it should contain the word 'password'",doc.indexOf("password")!=-1);				
	}
}
