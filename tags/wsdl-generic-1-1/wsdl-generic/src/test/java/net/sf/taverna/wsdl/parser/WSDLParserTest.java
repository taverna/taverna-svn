package net.sf.taverna.wsdl.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.wsdl.Operation;

import org.junit.Test;

public class WSDLParserTest {

	public static final String WSDL_TEST_BASE="http://www.mygrid.org.uk/taverna-tests/testwsdls/";
	
	@Test
	public void testGetOperations() throws Exception {
		WSDLParser parser = new WSDLParser(
				WSDL_TEST_BASE+"eutils/eutils_lite.wsdl");
		List<Operation> operations = parser.getOperations();
		assertEquals(
				"wrong number of operations found (wsdl may have changed)", 12,
				operations.size());
		Operation op = (Operation) operations.get(0);
		assertEquals("wrong name for first operation", "run_eGquery", op
				.getName());
		assertEquals("wrong style", "document", parser.getStyle());
	}

	@Test
	public void testGetActionURI() throws Exception {
		WSDLParser parser = new WSDLParser(
				WSDL_TEST_BASE+"eutils/eutils_lite.wsdl");
		String actionURI = parser.getSOAPActionURI("run_eInfo");
		assertEquals("action uri is wrong", "einfo", actionURI);
	}

	@Test
	public void testMissingStyleInBinding() throws Exception {
		WSDLParser parser = new WSDLParser(
				WSDL_TEST_BASE+"SBWReader.wsdl");
		assertEquals("Style should default to document if missing","document",parser.getStyle());
	}
	
	@Test
	public void testComplexTypeFromImport() throws Exception {
		WSDLParser parser = new WSDLParser(
				WSDL_TEST_BASE+"eutils/eutils_lite.wsdl");

		List<TypeDescriptor> inputs = parser.getOperationInputParameters("run_eInfo");
		List<TypeDescriptor> outputs = parser.getOperationOutputParameters("run_eInfo");
		assertEquals("wrong number of inputs", 1, inputs.size());
		assertEquals("wrong number of outputs", 1, outputs.size());
		assertTrue("input should be complex",
				inputs.get(0) instanceof ComplexTypeDescriptor);
		ComplexTypeDescriptor complexTypeDesc = (ComplexTypeDescriptor) inputs
				.get(0);
		assertEquals("wrong name", "parameters", complexTypeDesc.getName());
		assertEquals("wrong number of elements", 3, complexTypeDesc
				.getElements().size());

		TypeDescriptor typeDesc = (TypeDescriptor) complexTypeDesc
				.getElements().get(0);

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

	@Test
	public void testNestedComplexTypes() throws Exception {
		WSDLParser parser = new WSDLParser(WSDL_TEST_BASE+"bind.wsdl");

		List<TypeDescriptor> inputs = parser.getOperationInputParameters("BIVGetComplexRecord");
		List<TypeDescriptor> outputs = parser
				.getOperationOutputParameters("BIVGetComplexRecord");

		assertEquals("wrong number of inputs", 1, inputs.size());
		assertEquals("wrong number of outputs", 1, outputs.size());

		assertEquals("wrong name for input", "bid", ((TypeDescriptor) inputs
				.get(0)).getName());
		assertEquals("wrong type for input", "int", ((TypeDescriptor) inputs
				.get(0)).getType());

		assertEquals("wrong name for output", "BIVComplex",
				((TypeDescriptor) outputs.get(0)).getName());
		assertEquals("wrong type for output", "BIVComplex",
				((TypeDescriptor) outputs.get(0)).getType());
		assertTrue("wrong descriptor class for output",
				outputs.get(0) instanceof ComplexTypeDescriptor);

		ComplexTypeDescriptor typeDesc = (ComplexTypeDescriptor) outputs.get(0);
		assertEquals("wrong number of inner elements", 3, typeDesc
				.getElements().size());
		assertEquals("wrong name for first element", "bid",
				((TypeDescriptor) typeDesc.getElements().get(0)).getName());
		assertEquals("wrong name for 2nd element", "spokeModel",
				((TypeDescriptor) typeDesc.getElements().get(1)).getName());
		assertEquals("wrong name for 3rd element", "subunit",
				((TypeDescriptor) typeDesc.getElements().get(2)).getName());

		assertTrue("3rd element should be instance of ArrayTypeDescriptor",
				typeDesc.getElements().get(2) instanceof ArrayTypeDescriptor);
		ArrayTypeDescriptor arrayTypeDesc = (ArrayTypeDescriptor) typeDesc
				.getElements().get(2);

		assertEquals("wrong type for 3rd element", "BIVMolecule", arrayTypeDesc
				.getType());

		typeDesc = (ComplexTypeDescriptor) arrayTypeDesc.getElementType();

		assertEquals("wrong type for 3rd element", "BIVMolecule", typeDesc
				.getType());

		assertEquals("wrong number of elements in nested complex type", 7,
				typeDesc.getElements().size());
		assertEquals("wrong name for first element", "id",
				((TypeDescriptor) typeDesc.getElements().get(0)).getName());
		assertEquals("wrong type for first element", "int",
				((TypeDescriptor) typeDesc.getElements().get(0)).getType());

		assertEquals("wrong name for last element", "smid-hits",
				((TypeDescriptor) typeDesc.getElements().get(6)).getName());
		assertEquals("wrong type for last element", "int",
				((TypeDescriptor) typeDesc.getElements().get(6)).getType());
	}

	@Test
	public void testBaseTypes() throws Exception {
		WSDLParser parser = new WSDLParser(WSDL_TEST_BASE+"bind.wsdl");

		List<TypeDescriptor> inputs = parser.getOperationInputParameters("BIVGetRecord");
		assertEquals("wrong number of inputs", 1, inputs.size());
		assertTrue("should not be base type",
				inputs.get(0) instanceof BaseTypeDescriptor);
		assertEquals("wrong name", "bid", ((TypeDescriptor) inputs.get(0))
				.getName());
		assertEquals("wrong type", "int", ((TypeDescriptor) inputs.get(0))
				.getType());
	}

	@Test
	public void testArrayType() throws Exception {
		WSDLParser parser = new WSDLParser(WSDL_TEST_BASE+"bind.wsdl");

		List<TypeDescriptor> inputs = parser.getOperationInputParameters("BIVGetRecords");
		List<TypeDescriptor> outputs = parser.getOperationOutputParameters("BIVGetRecords");
		assertEquals("wrong number of inputs", 1, inputs.size());
		assertTrue("input should be of AArrayTypeDescriptor",
				inputs.get(0) instanceof ArrayTypeDescriptor);

		ArrayTypeDescriptor arrayTypeDesc = (ArrayTypeDescriptor) inputs.get(0);

		assertEquals("wrong name", "ids", arrayTypeDesc.getName());
		assertEquals("wrong type", "ArrayOf_xsd_int", arrayTypeDesc.getType());

		TypeDescriptor typeDesc = arrayTypeDesc.getElementType();

		assertTrue("element should be of type BaseTypeDescriptor",
				typeDesc instanceof BaseTypeDescriptor);
		assertEquals("wrong type", "int", typeDesc.getType());

		assertEquals("wrong number of outputs", 1, outputs.size());

		assertTrue("output should be of ArrayTypeDescriptor",
				outputs.get(0) instanceof ArrayTypeDescriptor);

		arrayTypeDesc = (ArrayTypeDescriptor) outputs.get(0);
		assertEquals("wrong name", "BIVRecords", arrayTypeDesc.getName());
		assertEquals("wrong type", "ArrayOfBIVRecord", arrayTypeDesc.getType());

		typeDesc = arrayTypeDesc.getElementType();

		assertEquals("wrong type", "BIVRecord", typeDesc.getType());
	}

	@Test
	public void testGoVizNoOutputs() throws Exception {
		WSDLParser parser = new WSDLParser(
				WSDL_TEST_BASE+"GoViz.wsdl");

		List<TypeDescriptor> inputs = parser.getOperationInputParameters("destroySession");
		List<TypeDescriptor> outputs = parser.getOperationOutputParameters("destroySession");

		assertEquals("wrong number of inputs", 1, inputs.size());
		assertEquals("wrong number of outputs", 0, outputs.size());

		TypeDescriptor typeDesc = (TypeDescriptor) inputs.get(0);
		assertTrue("input should be BaseType",
				typeDesc instanceof BaseTypeDescriptor);
		assertEquals("wrong name", "sessionID", typeDesc.getName());
		assertEquals("wrong type", "string", typeDesc.getType());
	}

	@Test
	public void testGetUseEncoded() throws Exception {
		WSDLParser parser = new WSDLParser(WSDL_TEST_BASE+"bind.wsdl");
		String use = parser.getUse("BIVGetRecords");
		assertEquals("use should be encoded", "encoded", use);
	}

	@Test
	public void testGetUseLiteral() throws Exception {
		WSDLParser parser = new WSDLParser(
				WSDL_TEST_BASE+"eutils/eutils_lite.wsdl");
		String use = parser.getUse("run_eInfo");
		assertEquals("use should be literal", "literal", use);
	}

	@Test
	public void testGetOperationNamespace() throws Exception {
		WSDLParser parser = new WSDLParser(
				WSDL_TEST_BASE+"CurrencyExchangeService.wsdl");
		String operationNamespace = parser.getOperationNamespaceURI("getRate");
		assertEquals("operation namespace is wrong",
				"urn:xmethods-CurrencyExchange", operationNamespace);
	}

	@Test
	public void testGetOperationNamespace2() throws Exception {
		WSDLParser parser = new WSDLParser(
				WSDL_TEST_BASE+"eutils/eutils_lite.wsdl");
		String operationNamespace = parser
				.getOperationNamespaceURI("run_eInfo");
		assertEquals("operation namespace is wrong",
				"http://www.ncbi.nlm.nih.gov/soap/eutils/einfo",
				operationNamespace);
	}
}
