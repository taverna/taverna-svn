package org.embl.ebi.escience.testhelpers.acceptance;

/**
 * Storage class for information about a particular test failure that occurs when running Acceptance Tests.
 * 
 */

public class AcceptanceError 
{
	private String className;
	private String methodName;
	private String testType;
	private int lineNumber;
	private Object expected;
	private Object actual;
	private String message;
	
	public AcceptanceError(String message,String testType,String className,String methodName,int line,Object expected, Object actual)
	{
		this.className=className;
		this.methodName=methodName;
		this.lineNumber=line;
		this.expected=expected;
		this.actual=actual;
		this.message=message;
		this.testType=testType;
	}

	public String getTestType()
	{
		return testType;
	}
	
	public Object getActual() {
		return actual;
	}

	public String getClassName() {
		return className;
	}

	public Object getExpected() {
		return expected;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getMessage() {
		return message;
	}

	public String getMethodName() {
		return methodName;
	}
	
	/**
	 * Indicates that the expected result and actual result do not match.
	 * @return boolean
	 */
	public boolean isUnexpectedResult()
	{
		if (getExpected()==null && getActual()==null) return false;
		if (getExpected()!=null)
		{
			return !getExpected().equals(getActual());
		}
		else
		{
			return true;
		}
	}
	
	
	
}
