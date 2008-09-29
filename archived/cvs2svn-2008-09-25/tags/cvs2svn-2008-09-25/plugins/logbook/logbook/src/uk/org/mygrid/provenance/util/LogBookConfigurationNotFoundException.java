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
 * Filename           $RCSfile: LogBookConfigurationNotFoundException.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:22 $
 *               by   $Author: stain $
 * Created on 22 Nov 2006
 *****************************************************************/
package uk.org.mygrid.provenance.util;

import java.io.File;

import uk.org.mygrid.provenance.LogBookException;

public class LogBookConfigurationNotFoundException extends LogBookException {

    public LogBookConfigurationNotFoundException() {
        // default
    }

    public LogBookConfigurationNotFoundException(String message) {
        super(message);
    }

    public LogBookConfigurationNotFoundException(Throwable cause) {
        super(cause);
    }

    public LogBookConfigurationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogBookConfigurationNotFoundException(File configurationFile) {
        this("File " + configurationFile.toString() + " does not exists.");
    }

}
