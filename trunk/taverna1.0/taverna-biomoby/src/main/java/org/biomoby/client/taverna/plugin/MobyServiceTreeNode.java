/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;
import java.util.ArrayList;

public class MobyServiceTreeNode {

    // name of the service == node name
    private String name = "";
    
    // list of objects that service produces
    @SuppressWarnings("unused")
	private ArrayList mobyObjectTreeNodes = null;
    
    // description of object == tool tip text
    private String description = "";
    

    /**
     * 
     * @param name - the name of the Moby Service
     * @param description - the description of the Moby Service
     */
    public MobyServiceTreeNode(String name, String description) {
        this.name = name;
        this.description = description;
    }
    /* 
     * over-ride the toString method in order to print node values
     * that make sense.
     */
    public String toString() {
        return name;
    }
    
    public String getDescription() {
        return this.description;
    }
}
