/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.dnd;

import org.jdom.*;
import java.io.*;

/**
 * Contains a JDOM Element and methods to build processors
 * and processor factories from same.
 * @author Tom Oinn
 */
public class SpecFragment implements Serializable {
    
    private Element specElement;

    public SpecFragment(Element element) {
	specElement = element;
    }
 
    public Element getElement() {
	return specElement;
    }

}
