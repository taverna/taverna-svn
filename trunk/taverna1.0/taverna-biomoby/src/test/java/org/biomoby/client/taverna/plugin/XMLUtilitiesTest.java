package org.biomoby.client.taverna.plugin;

import junit.framework.TestCase;

public class XMLUtilitiesTest extends TestCase {
	public void test() {

	}

	public void testIsMultipleInvocationMessage() {
	}

	public void testIsMultipleInvocationMessage_String() {
	}

	public void testGetListOfSimples() {
	}

	public void testGetListOfSimples_String() {
	}

	public void testGetListOfCollections() {
	}

	public void testGetListOfCollections_String() {
	}

	public void testGetSimple() {
	}

	public void testGetSimple_String() {
	}

	public void testGetQueryID() {
	}

	public void testGetQueryID_String() {
	}

	public void testSetQueryID() {
	}

	public void testSetQueryID_String() {
	}

	public void testGetWrappedSimple() {
	}

	public void testGetWrappedSimple_String() {
	}

	public void testGetCollection() {
	}

	public void testGetCollection_String() {
	}

	public void testGetWrappedCollection() {
	}

	public void testGetWrappedCollection_String() {
	}

	public void testGetSimplesFromCollection() {
	}

	public void testGetSimplesFromCollection_String() {
	}

	public void testGetAllSimplesByArticleName() {
	}

	public void testGetAllSimplesByArticleName_String() {
	}

	public void testGetWrappedSimplesFromCollection() {
	}

	public void testGetWrappedSimplesFromCollection_String() {
	}

	public void testGetSingleInvokationsFromMultipleInvokations() {
	}

	public void testGetSingleInvokationsFromMultipleInvokations_String() {
	}

	public void testGetDOMDocument() {
	}

	public void testCreateServiceInput() {
	}

	public void testCreateServiceInput_String() {
	}

	public void testExtractMobyData() {
	}

	public void testRenameCollection() {
	}

	public void testRenameCollection_String() {
	}

	public void testRenameSimple() {
	}

	public void testRenameSimple_String() {
	}

	public void testCreateDomDocument() {
	}

	public void testCreateMobyDataWrapper() {
	}

	public void testCreateMobyDataElementWrapper() {
	}

	public void testCreateMobyDataElementWrapper_String() {
	}

	public void testCreateMultipleInvokations_String() {
	}

	public void testCreateMultipleInvokations() {
	}

	public void testIsWrapped() {
	}

	public void testIsWrapped_String() {
	}

	public void testIsCollection() {
	}

	public void testIsCollection_String() {
	}

	public void testIsEmpty() {
	}

	public void testIsEmpty_String() {

	}

	public void testMergeCollections() {

	}

	public void testGetServiceNotes() {

	}

	public void testGetServiceNotesString() {

	}

	public void testGetServiceNotesAsElement() {

	}

	public void testGetDirectChildByArticleNameString() {

	}

	public void testGetDirectChildByArticleName() {

	}
	
	public void TestIsThereData() {
		
	}

	private String xml = "<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">\r\n"
			+ "  <moby:mobyContent>\r\n"
			+ "    <moby:mobyData moby:queryID=\"a10\">\r\n"
			+ "      <moby:Collection moby:articleName=\"outputString\">\r\n"
			+ "      <moby:Simple moby:articleName=\"\">\r\n"
			+ "        <moby:String moby:id=\"\" moby:namespace=\"\">aa</moby:String>\r\n"
			+ "      </moby:Simple>\r\n"
			+ "      <moby:Simple moby:articleName=\"\">\r\n"
			+ "        <moby:String moby:id=\"\" moby:namespace=\"\">bb</moby:String>\r\n"
			+ "      </moby:Simple>\r\n"
			+ "      </moby:Collection >"
			+ "    </moby:mobyData>\r\n"
			+ "    <moby:mobyData moby:queryID=\"a11\">\r\n"
			+ "      <moby:Simple moby:articleName=\"outputString\">\r\n"
			+ "        <moby:String moby:id=\"\" moby:namespace=\"\">b</moby:String>\r\n"
			+ "      </moby:Simple>\r\n"
			+ "    </moby:mobyData>\r\n"
			+ "    <moby:mobyData moby:queryID=\"a12\">\r\n"
			+ "      <moby:Simple moby:articleName=\"outputString\">\r\n"
			+ "        <moby:String moby:id=\"\" moby:namespace=\"\">c</moby:String>\r\n"
			+ "      </moby:Simple>\r\n"
			+ "    </moby:mobyData>\r\n"
			+ "<moby:mobyData moby:queryID=\"my_collection\">\r\n"
			+ "		      <moby:Collection articleName=\"myCollection\">\r\n"
			+ "		      <moby:Simple>\r\n"
			+ "		        <moby:DNASequence moby:id=\"AJ012310\" moby:namespace=\"\">\r\n"
			+ "			        <moby:String moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"SequenceString\"></moby:String>\r\n"
			+ "			        <moby:Integer moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"Length\">5</moby:Integer>\r\n"
			+ "			  </moby:DNASequence>		\r\n"
			+ "		      </moby:Simple>\r\n"
			+ "		      </moby:Collection>\r\n"
			+ "		    </moby:mobyData>\r\n"
			+ "		    <moby:mobyData moby:queryID=\"my_simple\">\r\n"
			+ "		      <moby:Simple articleName=\"mySimple\">\r\n"
			+ "		        <moby:DNASequence moby:id=\"AJ012310\" moby:namespace=\"\">\r\n"
			+ "			        <moby:String moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"SequenceString\"></moby:String>\r\n"
			+ "			        <moby:Integer moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"Length\">5</moby:Integer>\r\n"
			+ "			  </moby:DNASequence>		\r\n"
			+ "		      </moby:Simple>\r\n"
			+ "		    </moby:mobyData>\r\n"
			+ "		    <moby:mobyData moby:queryID=\"myDNA\">\r\n"
			+ "		      <moby:Simple articleName=\"\">\r\n"
			+ "		        <moby:DNASequence moby:id=\"AJ012310\" moby:namespace=\"\">\r\n"
			+ "			        <moby:String moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"SequenceString\"></moby:String>\r\n"
			+ "			        <moby:Integer moby:id=\"AJ012310\" moby:namespace=\"\" articleName=\"Length\">5</moby:Integer>\r\n"
			+ "			  </moby:DNASequence>		\r\n" + "		      </moby:Simple>\r\n"
			+ "		    </moby:mobyData>" + "    <moby:mobyData moby:queryID=\"a13\">\r\n"
			+ "      <moby:Simple moby:articleName=\"outputString\">\r\n"
			+ "        <moby:String moby:id=\"\" moby:namespace=\"\">d</moby:String>\r\n"
			+ "      </moby:Simple>\r\n" + "    </moby:mobyData>\r\n" + "  </moby:mobyContent>\r\n"
			+ "</moby:MOBY>";

}
