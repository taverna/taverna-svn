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
 * Filename           $RCSfile: ProcessRun.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:14 $
 *               by   $Author: stain $
 * Created on 16-Aug-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.util;

/**
 * @author dturi
 * @version $Id: ProcessRun.java,v 1.1 2007-12-14 12:49:14 stain Exp $
 */
public class ProcessRun {

    String processRun;

    Process process;

    String endTime;

    boolean failed;

    public ProcessRun(String processRun) {
        this.processRun = processRun;
    }

    /**
     *  
     */
    public ProcessRun() {
        super();
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public String getProcessRun() {
        return processRun;
    }

    public void setProcessRun(String processRun) {
        this.processRun = processRun;
    }
}
