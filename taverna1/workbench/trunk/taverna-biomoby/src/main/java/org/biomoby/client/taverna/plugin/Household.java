/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.util.ArrayList;
/**
 * This class contains some methods that used to be in the source tree
 * of jmoby.
 * 
 * @author Edward Kawas
 * 
 */
public class Household {
    String name = "";
    ArrayList<String> list = new ArrayList<String>();
    public Household(String obj, ArrayList ch) {
	if (obj != null)
	    name = obj;
	if (ch != null)
	    list = ch;
    }

    public ArrayList getChildren() {
	return list;
    }

    public void setChildren(ArrayList ch) {
	if (ch != null)
	    list = ch;
	
    }

}
