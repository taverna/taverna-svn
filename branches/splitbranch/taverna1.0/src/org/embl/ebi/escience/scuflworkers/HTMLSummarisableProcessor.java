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
 * Filename           $RCSfile: HTMLSummarisableProcessor.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-06-22 11:31:41 $
 *               by   $Author: sowen70 $
 * Created on 16-Jun-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers;

import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.scufl.Processor;
/**
 * An inteface that when implemented indicates that this Processor is capable of generating
 * a html summary based upon all like processors in the model.
 * 
 * @author Stuart Owen
 *
 */

public interface HTMLSummarisableProcessor {

	/**
	 * Generates the html tables for a list of processors, creating a table
	 * grouped by resource host.
	 * 
	 * @param processors
	 * @return
	 */
	public String getHTMLSummary(List<HTMLSummarisableProcessor> processors, Map<String, Processor> names);

	/**
	 * int indicating the placement for this type of processor in the order of
	 * tables displayed
	 * 
	 * @return
	 */
	public int htmlTablePlacement();

}
