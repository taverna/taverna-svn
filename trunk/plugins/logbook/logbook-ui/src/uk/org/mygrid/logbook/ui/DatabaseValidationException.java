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
 * Filename           $RCSfile: DatabaseValidationException.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:48:39 $
 *               by   $Author: stain $
 * Created on 28 Nov 2006
 *****************************************************************/
package uk.org.mygrid.logbook.ui;

import uk.org.mygrid.provenance.LogBookException;


public class DatabaseValidationException extends LogBookException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String databaseName;
    
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public DatabaseValidationException() {
        // default
    }

    public DatabaseValidationException(String message) {
        super(message);
    }

    public DatabaseValidationException(Throwable cause) {
        super(cause);
    }

    public DatabaseValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
