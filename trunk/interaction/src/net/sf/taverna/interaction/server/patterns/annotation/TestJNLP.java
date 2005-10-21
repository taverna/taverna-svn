/*
 * Copyright 2005 Anders Lanzén CBU, BCCS
 * 
 * Created on Oct 21, 2005 by Anders Lanzén, Computational Biology Group, 
 * Bergen Center for Computationoal Science, UiB Norway.
 *
 */
package net.sf.taverna.interaction.server.patterns.annotation;

import java.io.IOException;

import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @author andersl
 * 
 */
public class TestJNLP {

    /**
     * 
     */
    public static void main (String [] args) {
        ArtemisInteractionJNLP sune = new ArtemisInteractionJNLP("123", "http://skihickory:8080/int/");
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());        
        try {
            outputter.output(sune, System.out);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    

}
