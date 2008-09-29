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
 * Filename           $RCSfile: WorkflowRun.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:13 $
 *               by   $Author: stain $
 * Created on 16-Aug-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.util;

import java.util.HashSet;
import java.util.Set;

/**
 * @author dturi
 * @version $Id: WorkflowRun.java,v 1.1 2007-12-14 12:49:13 stain Exp $
 */
public class WorkflowRun {

    String workflowRun;

    String workflow;

    String startTime;

    String endTime;

    String launcher;

    boolean failed;

    Set processRuns = new HashSet();

    public WorkflowRun(String workflowRun) {
        this.workflowRun = workflowRun;
    }

    /**
     *  
     */
    public WorkflowRun() {
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

    public String getLauncher() {
        return launcher;
    }

    public void setLauncher(String launcher) {
        this.launcher = launcher;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public String getWorkflowRun() {
        return workflowRun;
    }

    public void setWorkflowRun(String workflowRun) {
        this.workflowRun = workflowRun;
    }

    public void addProcessRun(ProcessRun processRun) {
       processRuns.add(processRun);
    }
}
