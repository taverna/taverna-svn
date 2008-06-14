package net.sf.taverna.t2.reference.impl;

import net.sf.taverna.t2.reference.StackTraceElementBean;
import net.sf.taverna.t2.reference.h3.HibernateComponentClass;

public class StackTraceElementBeanImpl implements StackTraceElementBean,
		HibernateComponentClass {

	private String className;
	private String fileName;
	private String methodName;
	private int lineNumber;
	
	public StackTraceElementBeanImpl() {
		//
	}
	
	public String getClassName() {
		return this.className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}

	public String getFileName() {
		return this.fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getLineNumber() {
		return lineNumber;
	}
	
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getMethodName() {
		return this.methodName;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

}
