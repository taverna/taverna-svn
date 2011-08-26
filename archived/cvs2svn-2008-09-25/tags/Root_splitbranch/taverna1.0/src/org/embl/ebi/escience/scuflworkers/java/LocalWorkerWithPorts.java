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
 * Filename           $RCSfile: LocalWorkerWithPorts.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-05 11:15:44 $
 *               by   $Author: sowen70 $
 * Created on 05-Jul-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.java;

import java.util.List;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;

/**
 * Interface that defines LocalWorker's that need to generate their own InputPorts and OutputPorts.
 * Examples are XMLInputSplitter and XMLOutputSplitter, which need to generate Input/Output Ports that
 * are XML splittable.
 * 
 * @author Stuart Owen
 * @see XMLInputSplitter
 * @see XMLOutputSplitter
 *
 */

public interface LocalWorkerWithPorts extends LocalWorker {
	
	/**
	 * The InputPorts for this localworker, to be used by LocalServiceProcessor
	 */
	public List<InputPort> inputPorts(LocalServiceProcessor processor) throws DuplicatePortNameException, PortCreationException;
	
	/**
	 * The OutputPorts for this localworker, to be used by LocalServiceProcessor
	 */
	public List<OutputPort> outputPorts(LocalServiceProcessor processor) throws DuplicatePortNameException, PortCreationException;

}
