package org.embl.ebi.escience.testhelpers.acceptance;

import java.util.List;
/**
 * Holder of the results of an individual test, including all errors that occurred(due to failed assertions), and any Throwable that is thrown from the test.
 * A test is concidered successful if no errors occur and no exception is thrown.
 *
 */

public class AcceptanceTestResult 
{
	private List errors;	
	private String testClass;
	private String testMethod;
	private Throwable throwable;
	
	public AcceptanceTestResult(String testClass, String testMethod,List errors,Throwable throwable) 
	{		
		this.errors = errors;		
		this.testClass = testClass;
		this.testMethod = testMethod;
		this.throwable = throwable;
	}
	
	public boolean isSuccess()
	{
		return (errors!=null && errors.size()==0 && throwable==null);
	}

	public List getErrors() 
	{
		return errors;
	}

	public String getTestClass() 
	{
		return testClass;
	}

	public String getTestMethod() 
	{
		return testMethod;
	}
	public Throwable getThrown()
	{
		return throwable;
	}
	
	
}
