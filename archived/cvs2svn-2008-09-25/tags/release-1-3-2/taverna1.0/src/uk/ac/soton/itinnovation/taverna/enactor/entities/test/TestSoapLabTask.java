
package uk.ac.soton.itinnovation.taverna.enactor.entities.test;

import junit.framework.TestCase;

import java.lang.String;
import java.lang.System;



public class TestSoapLabTask extends TestCase {

  public TestSoapLabTask(String s) {
    super(s);
  }

  protected void setUp() {
		/** TODO put in proxy settings here **/
		/*
		System.setProperty("http.proxyHost","<proxy-host>");
		System.setProperty("http.proxyPort","<proxy-port>");
		*/
	}

  protected void tearDown() {
  }

  public void testDoTask() {
      System.out.println("This test is deprecated following data model changes.");
      fail();
  }
  
}
