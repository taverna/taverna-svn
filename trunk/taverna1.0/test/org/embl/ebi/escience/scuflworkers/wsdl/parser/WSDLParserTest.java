package org.embl.ebi.escience.scuflworkers.wsdl.parser;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class WSDLParserTest extends TestCase 
{
		
	public void testGetOperations() throws Exception
	{
		WSDLParser parser = new WSDLParser("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils_lite.wsdl");
		List operations = parser.getOperations();
		assertEquals("wrong number of operations found (wsdl may have changed)",12,operations.size());
		WSDLParser.WSDLOperation op = (WSDLParser.WSDLOperation)operations.get(0);
		assertEquals("wrong name for first operation","run_eGquery",op.getName());
		assertEquals("wrong style","document",op.getStyle());
	}
	
	public void testGetOperationParameters() throws Exception
	{
		WSDLParser parser = new WSDLParser("http://www.ebi.ac.uk/ws/WSFasta.wsdl");
		List inputs = new ArrayList();
		List outputs = new ArrayList();
		parser.getOperationParameters("doFasta",inputs,outputs);
		assertEquals("wrong number of inputs",2,inputs.size());
		assertTrue("wrong class type for descriptor - should be ComplexTypeDescriptor",inputs.get(0) instanceof WSDLParser.ComplexTypeDescriptor);
		assertEquals("wrong type","inputParams",((WSDLParser.TypeDescriptor)inputs.get(0)).getType());
		assertEquals("wrong name","params",((WSDLParser.TypeDescriptor)inputs.get(0)).getName());		
		
		List inputParamsElements = ((WSDLParser.ComplexTypeDescriptor)inputs.get(0)).getElements();
		
		assertEquals("wrong number of elements",18,inputParamsElements.size());
		
		assertEquals("wrong name for first element","program",((WSDLParser.TypeDescriptor)inputParamsElements.get(0)).getName());
		assertEquals("wrong name for last element","email",((WSDLParser.TypeDescriptor)inputParamsElements.get(17)).getName());
		assertEquals("wrong type for first element","string",((WSDLParser.TypeDescriptor)inputParamsElements.get(0)).getType());
		assertEquals("wrong type for last element","string",((WSDLParser.TypeDescriptor)inputParamsElements.get(17)).getType());
		
		assertTrue("wrong class type for descriptor - should be TypeDescriptor",inputs.get(1) instanceof WSDLParser.TypeDescriptor);
		assertEquals("wrong type","base64Binary",((WSDLParser.TypeDescriptor)inputs.get(1)).getType());
		assertEquals("wrong name","content",((WSDLParser.TypeDescriptor)inputs.get(1)).getName());
		
		assertEquals("wrong number of outputs",1,outputs.size());
		assertTrue("wrong class type for descriptor - should be TypeDescriptor",outputs.get(0) instanceof WSDLParser.TypeDescriptor);
		assertEquals("wrong type","base64Binary",((WSDLParser.TypeDescriptor)outputs.get(0)).getType());
		assertEquals("wrong name","result",((WSDLParser.TypeDescriptor)outputs.get(0)).getName());
						
	}
	
	public void testComplexTypeFromImport() throws Exception
	{
		WSDLParser parser = new WSDLParser("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils_lite.wsdl");
		List inputs = new ArrayList();
		List outputs = new ArrayList();
		parser.getOperationParameters("run_eInfo",inputs,outputs);
		assertEquals("wrong number of inputs",1,inputs.size());
		assertEquals("wrong number of outputs",1,outputs.size());
		assertTrue("input should be complex",inputs.get(0) instanceof WSDLParser.ComplexTypeDescriptor);
		WSDLParser.ComplexTypeDescriptor complexTypeDesc = (WSDLParser.ComplexTypeDescriptor)inputs.get(0);
		assertEquals("wrong name","parameters",complexTypeDesc.getName());
		assertEquals("wrong number of elements",3,complexTypeDesc.getElements().size());
		
		WSDLParser.TypeDescriptor typeDesc = (WSDLParser.TypeDescriptor) complexTypeDesc.getElements().get(0);
		
		assertEquals("wrong name","db",typeDesc.getName());
		assertEquals("wrong type","string",typeDesc.getType());
		assertTrue("db should be optional",typeDesc.isOptional());
		assertFalse("db should not be unbounded",typeDesc.isUnbounded());
		
		typeDesc = (WSDLParser.TypeDescriptor) complexTypeDesc.getElements().get(1);
		assertEquals("wrong name","tool",typeDesc.getName());
		assertEquals("wrong type","string",typeDesc.getType());
		assertTrue("tool should be optional",typeDesc.isOptional());
		assertFalse("tool should not be unbounded",typeDesc.isUnbounded());
		
		typeDesc = (WSDLParser.TypeDescriptor) complexTypeDesc.getElements().get(2);
		assertEquals("wrong name","email",typeDesc.getName());
		assertEquals("wrong type","string",typeDesc.getType());
		assertTrue("email should be optional",typeDesc.isOptional());
		assertFalse("email should not be unbounded",typeDesc.isUnbounded());				
	}
	
	
	public void testNestedComplexTypes() throws Exception
	{
		WSDLParser parser=new WSDLParser("http://soap.bind.ca/wsdl/bind.wsdl");
		List inputs = new ArrayList();
		List outputs = new ArrayList();
		parser.getOperationParameters("BIVGetComplexRecord",inputs,outputs);
		
		assertEquals("wrong number of inputs",1,inputs.size());
		assertEquals("wrong number of outputs",1,outputs.size());
		
		assertEquals("wrong name for input","bid",((WSDLParser.TypeDescriptor)inputs.get(0)).getName());
		assertEquals("wrong type for input","int",((WSDLParser.TypeDescriptor)inputs.get(0)).getType());
		
		assertEquals("wrong name for output","BIVComplex",((WSDLParser.TypeDescriptor)outputs.get(0)).getName());
		assertEquals("wrong type for output","BIVComplex",((WSDLParser.TypeDescriptor)outputs.get(0)).getType());
		assertTrue("wrong descriptor class for output", outputs.get(0) instanceof WSDLParser.ComplexTypeDescriptor);
		
		WSDLParser.ComplexTypeDescriptor typeDesc = (WSDLParser.ComplexTypeDescriptor)outputs.get(0);
		assertEquals("wrong number of inner elements",3,typeDesc.getElements().size());
		assertEquals("wrong name for first element","bid",((WSDLParser.TypeDescriptor)typeDesc.getElements().get(0)).getName());
		assertEquals("wrong name for 2nd element","spokeModel",((WSDLParser.TypeDescriptor)typeDesc.getElements().get(1)).getName());
		assertEquals("wrong name for 3rd element","subunit",((WSDLParser.TypeDescriptor)typeDesc.getElements().get(2)).getName());
		
		assertTrue("3rd element should be instance of ArrayTypeDescriptor",typeDesc.getElements().get(2) instanceof WSDLParser.ArrayTypeDescriptor);		
		WSDLParser.ArrayTypeDescriptor arrayTypeDesc = (WSDLParser.ArrayTypeDescriptor)typeDesc.getElements().get(2);
		
		assertEquals("wrong type for 3rd element","BIVMolecule",arrayTypeDesc.getType());
		
		typeDesc=(WSDLParser.ComplexTypeDescriptor)arrayTypeDesc.getElementType();
		
		assertEquals("wrong type for 3rd element","BIVMolecule",typeDesc.getType());
		
		assertEquals("wrong number of elements in nested complex type",7,typeDesc.getElements().size());
		assertEquals("wrong name for first element","id",((WSDLParser.TypeDescriptor)typeDesc.getElements().get(0)).getName());
		assertEquals("wrong type for first element","int",((WSDLParser.TypeDescriptor)typeDesc.getElements().get(0)).getType());
		
		assertEquals("wrong name for last element","smid-hits",((WSDLParser.TypeDescriptor)typeDesc.getElements().get(6)).getName());
		assertEquals("wrong type for last element","int",((WSDLParser.TypeDescriptor)typeDesc.getElements().get(6)).getType());	
	}
	
	public void testSimpleTypes() throws Exception
	{
		WSDLParser parser=new WSDLParser("http://soap.bind.ca/wsdl/bind.wsdl");
		List inputs = new ArrayList();
		List outputs = new ArrayList();
		parser.getOperationParameters("BIVGetRecord",inputs,outputs);
		assertEquals("wrong number of inputs",1,inputs.size());
		assertTrue("should not be base type",inputs.get(0) instanceof WSDLParser.BaseTypeDescriptor);
		assertEquals("wrong name","bid",((WSDLParser.TypeDescriptor)inputs.get(0)).getName());
		assertEquals("wrong type","int",((WSDLParser.TypeDescriptor)inputs.get(0)).getType());		
	}
	
	
	public void testArrayType() throws Exception
	{
		WSDLParser parser=new WSDLParser("http://soap.bind.ca/wsdl/bind.wsdl");
		List inputs = new ArrayList();
		List outputs = new ArrayList();
		parser.getOperationParameters("BIVGetRecords",inputs,outputs);
		assertEquals("wrong number of inputs",1,inputs.size());		
		assertTrue("input should be of AArrayTypeDescriptor",inputs.get(0) instanceof WSDLParser.ArrayTypeDescriptor);
		
		WSDLParser.ArrayTypeDescriptor arrayTypeDesc = (WSDLParser.ArrayTypeDescriptor)inputs.get(0);
		
		assertEquals("wrong name","ids",arrayTypeDesc.getName());
		assertEquals("wrong type","ArrayOf_xsd_int",arrayTypeDesc.getType());
				
		
		WSDLParser.TypeDescriptor typeDesc = arrayTypeDesc.getElementType();
		
		assertTrue("element should be of type BaseTypeDescriptor",typeDesc instanceof WSDLParser.BaseTypeDescriptor);
		assertEquals("wrong type","int",typeDesc.getType());
		
		assertEquals("wrong number of outputs",1,outputs.size());
		assertTrue("output should be of ArrayTypeDescriptor",outputs.get(0) instanceof WSDLParser.ArrayTypeDescriptor);
		
		arrayTypeDesc = (WSDLParser.ArrayTypeDescriptor)outputs.get(0);
		assertEquals("wrong name","BIVRecords",arrayTypeDesc.getName());
		assertEquals("wrong type","ArrayOfBIVRecord",arrayTypeDesc.getType());
		
		typeDesc=arrayTypeDesc.getElementType();
		
		assertEquals("wrong type","BIVRecord",typeDesc.getType());				
	}			
	
}
