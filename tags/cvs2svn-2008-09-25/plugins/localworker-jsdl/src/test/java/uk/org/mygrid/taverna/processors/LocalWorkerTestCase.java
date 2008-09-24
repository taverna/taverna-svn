package uk.org.mygrid.taverna.processors;

import junit.framework.TestCase;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;


/*
 * A abstract class to help in the testing of LocalWorker's. 
 */
public abstract class LocalWorkerTestCase extends TestCase 
{
	abstract protected String [] expectedInputNames();
	abstract protected String [] expectedOutputNames();
	abstract protected String [] expectedInputTypes();
	abstract protected String [] expectedOutputTypes();
	
	abstract protected LocalWorker getLocalWorker();
	
	public void testInputNames()
	{
		LocalWorker worker=getLocalWorker();
		String [] expectedInputNames = expectedInputNames();
		String [] inputNames = worker.inputNames();
		
		assertEquals("invalid number of input names",expectedInputNames.length,inputNames.length);
		for (int i=0;i<inputNames.length;i++)
		{
			assertEquals("invalid input name",expectedInputNames[i],inputNames[i]);
		}				
	}
	
	public void testOutputNames()
	{
		LocalWorker worker=getLocalWorker();
		String [] expectedOutputNames = expectedOutputNames();
		String [] outputNames = worker.outputNames();
		System.out.print("length:" +expectedOutputNames.length);
		assertEquals("invalid number of output names",expectedOutputNames.length,outputNames.length);
		for (int i=0;i<outputNames.length;i++)
		{
			System.out.println("ExepectedOutputname:"+expectedOutputNames[i]+" " +outputNames[i]);
			assertEquals("invalid output name",expectedOutputNames[i],outputNames[i]);
		}
	}
	
	public void testOutputTypes()
	{
		LocalWorker worker=getLocalWorker();
		String [] expectedOutputTypes = expectedOutputTypes();
		String [] outputTypes = worker.outputTypes();
		assertEquals("invalid number of output types",expectedOutputTypes.length,outputTypes.length);
		for (int i=0;i<outputTypes.length;i++)
		{
			assertEquals("invalid output type",expectedOutputTypes[i],outputTypes[i]);
		}
	}
	
	public void testInputTypes()
	{
		LocalWorker worker=getLocalWorker();
		String [] expectedInputTypes = expectedInputTypes();
		String [] inputTypes = worker.inputTypes();
		assertEquals("invalid number of input types",expectedInputTypes.length,inputTypes.length);
		for (int i=0;i<inputTypes.length;i++)
		{
			assertEquals("invalid input type",expectedInputTypes[i],inputTypes[i]);
		}
	}
}
