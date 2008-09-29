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
 * Filename           $RCSfile: LogLevel.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:17 $
 *               by   $Author: stain $
 * Created on 4 Oct 2006
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna;

public class LogLevel {

    final public static int NOTHING = 0;

    final public static int IO = 3;

    final public static int PROCESS = 6;

    final public static int NESTED = 1;

    final public static int ITERATION = 8;

    final public static int ALL = 10;

    public static final String NOTHING_STRING = "NOTHING";

    public static final String IO_STRING = "IO";

    public static final String PROCESS_STRING = "PROCESS";

    public static final String NESTED_STRING = "NESTED";

    public static final String ITERATION_STRING = "ITERATION";

    public static final String ALL_STRING = "ALL";

    public static final String[] LEVELS_LIST = { NOTHING_STRING, NESTED_STRING,
            IO_STRING, PROCESS_STRING, ITERATION_STRING, ALL_STRING };

    public static final String[] VISIBLE_LEVELS_LIST = { NOTHING_STRING,
        IO_STRING, PROCESS_STRING, ALL_STRING };
    
    public static final String DEFAULT_LEVEL = IO_STRING;

    /**
     * The default (also for null) is {@link #IO}.
     * 
     * @param logLevel
     * @return
     */
    public static int toLogLevel(String logLevel) {
        if (logLevel != null) {
            if (logLevel.equals(NOTHING_STRING))
                return NOTHING;
            if (logLevel.equals(IO_STRING))
                return IO;
            if (logLevel.equals(PROCESS_STRING))
                return PROCESS;
            if (logLevel.equals(NESTED_STRING))
                return NESTED;
            if (logLevel.equals(ITERATION_STRING))
                return ITERATION;
            if (logLevel.equals(ALL_STRING))
                return ALL;
        }
        return IO;
    }
    
    public static String getLabel(String logLevel) {
        if (logLevel != null) {
            if (logLevel.equals(NOTHING_STRING))
                return "Nothing";
            if (logLevel.equals(IO_STRING))
                return "Workflow Inputs and Outputs";
            if (logLevel.equals(PROCESS_STRING))
                return "All intermediate steps, except iterations";
            if (logLevel.equals(ALL_STRING))
                return "Everything";
        }
        return "undefined label";
    }

}
