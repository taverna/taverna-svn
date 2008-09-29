package org.embl.ebi.escience.testhelpers.acceptance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * superclass to the AcceptanceTestCase that holds all the testing methods and storage of any errors
 * that occur for reporting.
 *
 */

public class AcceptanceAssert 
{
	private Map errors = new HashMap();
	
	public boolean assertTrue(String msg,boolean val)
	{
		boolean result=true;
		if (!val)
		{
			recordError(msg,"assertTrue",new Boolean(true),new Boolean(false));
			result=false;
		}
		return result;
	}
	
	public boolean assertFalse(String msg,boolean val)
	{
		boolean result=true;
		if (val)
		{
			recordError(msg,"assertFalse",new Boolean(false),new Boolean(true));
			result=false;
		}
		return result;
	}
	
	public boolean assertEquals(String msg,int expected,int actual)
	{
		return assertEquals(msg,new Integer(expected),new Integer(actual));
	}
	
	public boolean assertEquals(String msg,double expected,double actual)
	{
		return assertEquals(msg,new Double(expected),new Double(actual));
	}
	
	public boolean assertEquals(String msg,float expected,float actual)
	{
		return assertEquals(msg,new Float(expected),new Float(actual));
	}
	
	public boolean assertEquals(String msg,Object expected,Object actual)
	{
		boolean result=true;
		if (!expected.equals(actual))
		{
			recordError(msg,"assertEquals",expected,actual);
			result=false;
		}
		return result;
	}
	
	public boolean assertNull(String msg,Object val)
	{
		boolean result=true;
		if (val!=null)
		{
			recordError(msg,"assertNull",null,val);
			result=false;
		}
		return result;
	}
	
	public boolean assertNotNull(String msg,Object val)
	{
		boolean result=true;
		if (val==null)
		{
			recordError(msg,"assertNotNull","not null",val);
			result=false;
		}
		return result;
	}
	
	public void fail(String msg)
	{
		recordError(msg,"fail");
	}
	
	
	public synchronized List getErrors(String testName)
	{
		List result = (List)errors.get(testName);
		if (result==null) result=new ArrayList();
		return result;
	}
	
	protected synchronized void reset()
	{
		errors = new HashMap();
	}
	
	/**
	 * Stores the error in the errors map, using the test name as the key.
	 */
	private void recordError(String msg,String testType,Object expected, Object actual)
	{
		StackTraceElement el = getTestStackTraceElement();
		String testName=el.getClassName()+"."+el.getMethodName();
		synchronized(errors)
		{
			if (errors.get(testName)==null) errors.put(testName,new ArrayList());
			((List)errors.get(testName)).add(new AcceptanceError(msg,testType,el.getClassName(),el.getMethodName(),el.getLineNumber(),expected,actual));
		}
	}
	
	/**
	 * Stores the error in the errors map, using the test name as the key.
	 */
	private void recordError(String msg,String testType)
	{
		recordError(msg,testType,null,null);
	}
	
	private StackTraceElement getTestStackTraceElement()
	{
		StackTraceElement result=null;
		StackTraceElement [] trace=new Exception().getStackTrace();
		for (int i=0;i<trace.length;i++)
		{
			if (trace[i].getMethodName().startsWith("test"))
			{
				result=trace[i];
				break;
			}				
		}
		return result;
	}
	
}
