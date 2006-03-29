/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;
public class MobyObjectTreeNode {
    
    //  name of the object == node name
    private String name = "";
    
    // description of object == tool tip text
    private String description = "";
    

    /**
     * 
     * @param name - the name of the Moby Object
     * @param description - the description of the Moby Service
     */
    public MobyObjectTreeNode(String name, String description) {
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
