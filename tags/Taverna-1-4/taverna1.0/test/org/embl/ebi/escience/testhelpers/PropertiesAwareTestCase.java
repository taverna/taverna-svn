package org.embl.ebi.escience.testhelpers;

import java.util.Enumeration;
import java.util.ResourceBundle;

import junit.framework.TestCase;

public abstract class PropertiesAwareTestCase extends TestCase 
{
	protected void setUp() throws Exception 
	{
		ResourceBundle bundle=ResourceBundle.getBundle("mygridtest");
		Enumeration keys=bundle.getKeys();
		while(keys.hasMoreElements())
		{
			String key=(String)keys.nextElement();
			String value=(String)bundle.getString(key);
			System.getProperties().put(key,value);
		}					
	}
}
