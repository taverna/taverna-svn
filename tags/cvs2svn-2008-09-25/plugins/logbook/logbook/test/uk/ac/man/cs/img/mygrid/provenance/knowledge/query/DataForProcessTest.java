/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: DataForProcessTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:23 $
 *               by   $Author: stain $
 * Created on 22-Aug-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.query;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author dturi
 * @version $Id: DataForProcessTest.java,v 1.1 2007-12-14 12:53:23 stain Exp $
 */
public class DataForProcessTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DataForProcessTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDataForProcess() throws Exception {
        Map data = new DataForProcess().dataForProcess(
                "urn:www.mygrid.org/process#ColourAnimals",
                "urn:www.mygrid.org/process#ColourAnimals:string2");
        Iterator iterator = data.keySet().iterator();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            System.out.println("process = " + key);
            Collection next = (Collection) data.get(key);//values().iterator().next();
            for (Iterator iter = next.iterator(); iter.hasNext();) {
                String[] data2 = (String[]) iter.next();
                System.out.println("input = " + data2[0]);
                System.out.println("output = " + data2[1]);                
                System.out.println("------");
            }
        }
    }

}
