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
 * Filename           $RCSfile: ObjectPool.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:07 $
 *               by   $Author: stain $
 * Created on 03-May-2006
 *****************************************************************/
package uk.org.mygrid.provenance.dataservice;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Class extracted from
 * {@link org.embl.ebi.escience.baclava.store.JDBCBaclavaDataService}.
 *
 * 
 * @author dturi
 * @version $Id: ObjectPool.java,v 1.1 2007-12-14 12:49:07 stain Exp $
 */
public abstract class ObjectPool {

    private long expirationTime;

    private Hashtable locked, unlocked;

    int maxObjects = 10;

    abstract Object create() throws ObjectPoolException;

    abstract boolean validate(Object o);

    abstract void expire(Object o);

    public ObjectPool() {
        expirationTime = 30000; // 30 seconds
        locked = new Hashtable();
        unlocked = new Hashtable();
    }

    synchronized Object checkOut() throws ObjectPoolException {
        try {
            return realCheckOut();
        } catch (Exception e) {
            while (true) {
                try {
                    Thread.sleep(2000);
                    return realCheckOut();
                } catch (Exception e2) {
                    throw new ObjectPoolException(e);
                }
            }
        }
    }

    synchronized Object realCheckOut() throws Exception {
        long now = System.currentTimeMillis();
        Object o;
        if (unlocked.size() > 0) {
            Enumeration e = unlocked.keys();
            while (e.hasMoreElements()) {
                o = e.nextElement();
                if ((now - ((Long) unlocked.get(o)).longValue()) > expirationTime) {
                    // object has expired
                    unlocked.remove(o);
                    expire(o);
                    o = null;
                } else {
                    if (validate(o)) {
                        unlocked.remove(o);
                        locked.put(o, new Long(now));
                        return (o);
                    } else {
                        // object failed validation
                        unlocked.remove(o);
                        expire(o);
                        o = null;
                    }
                }
            }
        }
        int currentObjects = locked.size();
        if (currentObjects < maxObjects || maxObjects == 0) {
            // no objects available, create a new one
            o = create();
            if (o != null)
                locked.put(o, new Long(now));
            return (o);
        } else {
            throw new Exception("Pool too big, refusing to grow");
        }
    }

    synchronized void checkIn(Object o) {
        if (o == null)
            return;
        locked.remove(o);
        unlocked.put(o, new Long(System.currentTimeMillis()));
    }

}
