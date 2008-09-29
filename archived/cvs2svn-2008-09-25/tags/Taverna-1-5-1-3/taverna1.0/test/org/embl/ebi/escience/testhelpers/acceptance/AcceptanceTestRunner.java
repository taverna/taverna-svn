package org.embl.ebi.escience.testhelpers.acceptance;


import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Discovers all AcceptanceTestCase type classes that exist in the package org.embl.ebi.escience.acceptancetests.
 * Each test case is then invoked sequentially, and the results stored for a final report.
 *
 */


public class AcceptanceTestRunner 
{
	public static void main(String [] args)
	{
		if (args.length==1) System.setProperty("taverna.home",args[0]);
		new AcceptanceTestRunner().run();
		System.exit(0);
	}
	
	public void run()
	{
		List tests=getTestClasses();
		List results=new ArrayList();
		for(Iterator iterator=tests.iterator();iterator.hasNext();)
		{
			Class testClass=(Class)iterator.next();
			try
			{
				AcceptanceTestCase test = (AcceptanceTestCase)testClass.newInstance();
				results.addAll(test.run());
			}
			catch(Exception e)
			{
				results.add(new AcceptanceTestResult(testClass.getName(),"",new ArrayList(),e));				
			}
		}
		
		reportResults(results);
	}		
	
	/**
	 * Temporarily just dumps out to screen for now, until we know what integration testing mechanism we are using.
	 * @param results
	 */
	protected void reportResults(List results)
	{
		for (Iterator resultsiterator=results.iterator();resultsiterator.hasNext();)
		{	
			AcceptanceTestResult result = (AcceptanceTestResult)resultsiterator.next();
			if (result.isSuccess())
			{
				System.out.println("PASS: "+result.getTestClass()+"."+result.getTestMethod()+", passed with no errors");
			}
			else
			{
				System.out.println("FAIL: "+result.getTestClass()+"."+result.getTestMethod()+", had errors");
				for (Iterator iterator = result.getErrors().iterator();iterator.hasNext();)
				{
					AcceptanceError error = (AcceptanceError)iterator.next();
					System.out.println("\t"+error.getClassName()+"."+error.getMethodName()+":"+error.getLineNumber()+", "+error.getTestType());
					System.out.println("\t"+error.getMessage());
					if (error.isUnexpectedResult())
					{
						System.out.println("\texpected <"+error.getExpected()+">");
						System.out.println("\tbut got <"+error.getActual()+">");
					}
				}
				if (result.getThrown()!=null)
				{
					Throwable thrown = result.getThrown();				
					System.out.println("\tException was thrown: "+thrown.toString());
					StackTraceElement [] els = thrown.getStackTrace();
					for (int i=0;i<els.length;i++)
					{
						System.out.println("\t\t"+els[i].toString());
					}				
				}
			}
			System.out.println("");
		}
		
	}
	
	protected List getTestClasses()
	{		
		List result=new ArrayList();				
		
		String pac="org.embl.ebi.escience.acceptancetests";
			
		URL url=AcceptanceTestRunner.class.getResource("/"+pac.replace('.','/')+"/");
		if (url!=null)
		{
			File dir = new File(url.getFile());
			if (dir.exists())
			{
				String files[]=dir.list();
				for (int j=0;j<files.length;j++)
				{
					String file=files[j];
					if (file.endsWith(".class"))
					{
						String classname=file.substring(0,file.length()-6);						
						try
						{
							Class c = Class.forName(pac+"."+classname);								
							if (AcceptanceTestCase.class.isAssignableFrom(c))
							{
								result.add(c);
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}						
					}
				}
			}
		}		
		return result;
	}
	
	
	protected List getTests()
	{
		ArrayList result=new ArrayList();
		List classes = getTestClasses();
		
		for (Iterator iterator = classes.iterator();iterator.hasNext();)
		{
			
			Class testClass = (Class)iterator.next();			
			try
			{				
				result.add(testClass.newInstance());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return result;		
	}
}
