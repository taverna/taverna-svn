package org.embl.ebi.escience.testhelpers.acceptance;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Some tests can be time consuming, particularly due to their reliance on external services.
 * This base test case allow for multiple tests (i.e. each public method with a prefix 'test') to be run simultaneously on seperate threads.
 * Because of this multithreaded nature, care should be taken that each test does not rely on state shared between each individual test.
 * For this reason setUp() and teardown() are never called and have been made final, as their use would be dangerous. 
 * To prevent an over abundance of simultaneous tests, tests should be split between seperate TestCases, rather than have one TestCase with many tests.
 */

public abstract class AsynchronousAcceptanceTestCase extends AcceptanceTestCase 
{

	
	public final void setUp() {
		
	}

	public final void tearDown() {
		
	}

	public List run()
	{
		final List result = new ArrayList();
		List tests = getTestMethods();
		List threads = new ArrayList();
		
		for (Iterator iterator=tests.iterator();iterator.hasNext();)
		{			
			final Method testMethod = (Method)iterator.next();
			Runnable runnable = new Runnable()
			{
				public void run() 
				{
					AcceptanceTestResult testResult=invokeTest(testMethod);
					synchronized(result)
					{
						result.add(testResult);
					}
				}								
			};
			Thread thread=new Thread(runnable);
			thread.start();
			threads.add(thread);
		}
		for (Iterator iterator=threads.iterator();iterator.hasNext();)
		{
			try
			{
				((Thread)iterator.next()).join();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		reset();		
		return result;
	}
	
}
