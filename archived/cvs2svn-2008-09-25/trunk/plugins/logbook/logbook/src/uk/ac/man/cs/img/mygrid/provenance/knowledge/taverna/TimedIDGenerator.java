/*
 * Created on 29-Apr-2004
 * 
 * Copyright (C) 2003 The University of Manchester
 * 
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate. Authorship of the modifications
 * may be determined from the ChangeLog placed at the end of this file.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *  
 */
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna;


/**
 * Implementation of
 * {@link uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.IDGenerator}
 * using time to provide unique ids.
 * 
 * @author dturi $Id: TimedIDGenerator.java,v 1.1 2007-12-14 12:49:16 stain Exp $
 *  
 */
public class TimedIDGenerator implements IDGenerator {
    
    private String prefix;

    public TimedIDGenerator(String prefix) {
        this.prefix = prefix;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.metadata.IDGenerator#getNextID()
     */
    public String getNextID() {
        String result = prefix+System.currentTimeMillis();
        return result;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}