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

import java.util.Random;

/**
 * Elementary implementation of
 * {@link uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.IDGenerator}.
 * 
 * @author chris $Id: SimpleIDGenerator.java,v 1.1 2007-12-14 12:49:15 stain Exp $
 *  
 */
public class SimpleIDGenerator implements IDGenerator {
    //private int count;

    private static Random r = new Random();

    private String prefix = "http://www.mygrid.org.uk/metadata#";

    /**
     *  
     */
    public SimpleIDGenerator() {
        super();
        //count = 0;
    }

    public SimpleIDGenerator(String prefix) {
        super();
        this.prefix = prefix;
        //count = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.metadata.IDGenerator#getNextID()
     */
    public String getNextID() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 10; i++) {
            int n = r.nextInt();
            n = (n < 0) ? -n : n;
            sb
                    .append("1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                            .charAt(n % 62));
        }
        String result = prefix + sb.toString();
        //System.out.println(result);
        return result;
    }
}