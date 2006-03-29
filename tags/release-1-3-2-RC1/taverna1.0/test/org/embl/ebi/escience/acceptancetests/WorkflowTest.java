package org.embl.ebi.escience.acceptancetests;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.testhelpers.acceptance.WorkflowTestCase;


public class WorkflowTest extends WorkflowTestCase
{		
	
	public void testSimpleWorkflow() throws Exception
	{
		Map inputs=new HashMap();
		Map outputs=new HashMap();
		outputs.put("out","bob");
		performTest("SimpleWorkflow",inputs,outputs);
	}	
	
	
	public void testCompareXandYWorkflow() throws Exception
	{
		Map outputs = executeWorkflow("CompareXandYFunctions",new HashMap(),defaultWorkflowEventListener());
		if (assertEquals("wrong number of outputs",1,outputs.size()))
		{
			assertNotNull("output 'Graph' does not exist",outputs.get("Graph"));
		}		
	}
		
	public void testGenscanShimExample() throws Exception
	{				
		Map inputs = new HashMap();
		String data = readInputDataFromFile("genscan_shim_example2","INPUT_fasta.txt");
		inputs.put("dna", DataThingFactory.bake(data));				
		
		Map outputs=executeWorkflow("genscan_shim_example2",inputs, defaultWorkflowEventListener());
		
		assertEquals("wrong number of outputs",5,outputs.size());
		
		boolean ok=true;
		ok = ok && assertNotNull("no blast_out output",outputs.get("blast_out"));
		ok = ok && assertNotNull("no cds output",outputs.get("cds"));
		ok = ok && assertNotNull("no genscan_report output",outputs.get("genscan_report"));
		ok = ok && assertNotNull("no peptides output",outputs.get("peptides"));
		ok = ok && assertNotNull("no Prosite_matches output",outputs.get("Prosite_matches"));
		
		if (ok)
		{
			data=readOutputDataFromFile("genscan_shim_example2","cds.text");
			DataThing thing = (DataThing)outputs.get("cds");
			assertEquals("cds results dont match",data,thing.getDataObject());			
			
			data=readOutputDataFromFile("genscan_shim_example2","genscan_report.text");
			thing = (DataThing)outputs.get("genscan_report");
			assertEquals("genscan_report results dont match",data,thing.getDataObject());
			
			data=readOutputDataFromFile("genscan_shim_example2","peptides.text");
			thing = (DataThing)outputs.get("peptides");
			assertEquals("peptides results dont match",data,thing.getDataObject());								
		}		
	}
	
	
	public void testIterationStrategyExample() throws Exception
	{
		Map outputs=executeWorkflow("IterationStrategyExample",new HashMap(),defaultWorkflowEventListener());
		assertEquals("wrong number of outputs",1,outputs.size());
		
		if (assertNotNull("no output called Output",outputs.get("Output")))
		{
			DataThing thing = (DataThing)outputs.get("Output");
			List list = (List)thing.getDataObject();
			assertEquals("wrong number of elements in array",3,list.size());
			List list1 = (List)list.get(0);
			List list2 = (List)list.get(1);
			List list3 = (List)list.get(2);
			if (assertEquals("wrong number of elements in first list",2,list1.size()))
			{
				assertEquals("incorrect result","square red cat",list1.get(0));
				assertEquals("incorrect result","square greenrabbit",list1.get(1));
			}
			
			if (assertEquals("wrong number of elements in second list",2,list2.size()))
			{
				assertEquals("incorrect result","circular red cat",list2.get(0));
				assertEquals("incorrect result","circular greenrabbit",list2.get(1));
			}
			
			if (assertEquals("wrong number of elements in third list",2,list3.size()))
			{
				assertEquals("incorrect result","triangularred cat",list3.get(0));
				assertEquals("incorrect result","triangulargreenrabbit",list3.get(1));
			}			
		}		
	}
	
	public void testSeqVistaRendering() throws Exception
	{
		Map outputs = executeWorkflow("SeqVistaRendering", new HashMap(), defaultWorkflowEventListener());
		assertEquals("wrong number of outputs",1,outputs.size());
		
		if (assertNotNull("no output called seq",outputs.get("seq")))
		{
			DataThing thing = (DataThing)outputs.get("seq");					
			String type = thing.getSyntacticType();
			assertEquals("Syntactic type is incorrect","'text/plain,chemical/x-embl-dl-nucleotide'",type);
			
			String data = (String)thing.getDataObject();
			String expected = readOutputDataFromFile("SeqVistaRendering","seq.text");
			System.out.println("***** "+data.length()+", "+expected.length());
			assertEquals("data stored in seq.text does not match output",expected,data);
			
		}
	}
				
}
