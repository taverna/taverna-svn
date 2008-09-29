package org.embl.ebi.escience.testhelpers.acceptance;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * Test Case class for the purpose of running Acceptance Tests, rather than Unit Tests.
 * Works in a similar way to unit tests, but rather than failing at the first error, will continue
 * with the tests and gather the errors up for a final report.
 * Tests are added by subclassing this class and adding tests as methods with the prefix 'test' (as with the Junit TestCase).
 * @author sowen
 *
 */
public abstract class AcceptanceTestCase extends AcceptanceAssert
{	
	private static Logger logger = Logger.getLogger(AcceptanceTestCase.class);
	/*
	 * Should be overridden to provide pre-test initialisation
	 */
	public void setUp()
	{
		
	}
	
	/*
	 * Should be overridden to provide post-test clean up
	 */
	public void tearDown()
	{
		
	}
	
	/*
	 * Gathers all the tests (i.e. methods with the prefix 'test'), runs each one, and returns a list of the results
	 */
	
	public List run()
	{
		List result = new ArrayList();
		List tests = getTestMethods();
		
		for (Iterator iterator=tests.iterator();iterator.hasNext();)
		{			
			Method testMethod = (Method)iterator.next();
			setUp();
			result.add(invokeTest(testMethod));
			tearDown();																			
		}
		reset();		
		return result;
	}
	
	protected AcceptanceTestResult invokeTest(Method testMethod)
	{
		AcceptanceTestResult result = null;
		Throwable exception = null;
		Object [] args = {};
		try
		{			
			System.out.print("### Running "+testMethod.getName()+" ... ");
			testMethod.invoke(this, args);				
		}		
		catch(InvocationTargetException e)
		{
			exception = e.getTargetException();
		}
		catch(Exception e)
		{
			exception = e;
		}
		finally
		{
			String testName=testMethod.getDeclaringClass().getName()+"."+testMethod.getName();
			result = new AcceptanceTestResult(this.getClass().getName(),testMethod.getName(),getErrors(testName),exception);
			System.out.print(result.isSuccess() ? "PASS" : "FAIL");
			System.out.println();
		}
				
		return result;
	}		
	
	protected List getTestMethods()
	{
		List result=new ArrayList();
		Method [] methods=this.getClass().getMethods();
		for (int i=0;i<methods.length;i++)
		{
			if (methods[i].getName().startsWith("test")) result.add(methods[i]);
		}
		return result;
	}

}
