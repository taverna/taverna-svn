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
 * Filename           $RCSfile: GraphRemovalException.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:09 $
 *               by   $Author: stain $
 * Created on 16-Mar-2006
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.store;

/**
 * @author dturi
 * @version $Id: GraphRemovalException.java,v 1.1 2007-12-14 12:49:09 stain Exp $
 */
public class GraphRemovalException extends MetadataServiceException {

    /**
     * 
     */
    public GraphRemovalException() {
        super();
    }

    /**
     * @param message
     */
    public GraphRemovalException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public GraphRemovalException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public GraphRemovalException(Throwable cause) {
        super(cause);
    }

}
