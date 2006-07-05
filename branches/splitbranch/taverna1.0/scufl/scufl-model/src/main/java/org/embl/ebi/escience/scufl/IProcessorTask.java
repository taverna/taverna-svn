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
 * Filename           $RCSfile: IProcessorTask.java,v $
 * Revision           $Revision: 1.1.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-05 16:40:23 $
 *               by   $Author: davidwithers $
 * Created on 04-Jul-2006
 *****************************************************************/
package org.embl.ebi.escience.scufl;

import java.util.Collection;

import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;

/**
 * An interface representing a ProcessorTask
 * (created to break the dependancy between ProcessorTaskImpl and ProcessorTaskWorker).
 * 
 * @author Stuart Owen
 *
 */

public interface IProcessorTask {
	
	public Processor getProcessor();
	public Collection getChildren();
	public WorkflowInstance getWorkflowInstance();
}
