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
 * Filename           $RCSfile: LockTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:52:57 $
 *               by   $Author: stain $
 * Created on 05-Apr-2006
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

public class LockTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(LockTest.class);
    }

    List<String> locks = Collections.synchronizedList(new ArrayList<String>());

    static String testValue;

    static String lock = "lock";

    public void testLock() throws Exception {
        FirstThread firstThread = new FirstThread();
        SecondThread secondThread = new SecondThread();
        firstThread.start();
        secondThread.start();
        firstThread.join();
        assertEquals("second", testValue);
    }

    class FirstThread extends Thread {
        public void run() {
//            try {
//                sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            if (!locks.contains(lock))
                locks.add(lock);
            synchronized (locks.get(locks.indexOf(lock))) {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                testValue = "first";
                System.out.println(testValue);
            }
        }
    }

    class SecondThread extends Thread {
        public void run() {
            if (!locks.contains(lock))
                locks.add(lock);
            synchronized (locks.get(locks.indexOf(lock))) {
                testValue = "second";
                System.out.println(testValue);
            }
        }
    }

}
