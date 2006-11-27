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
 * Filename           $RCSfile: PerspectiveRegistry.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-11-27 10:35:42 $
 *               by   $Author: sowen70 $
 * Created on 8 Nov 2006
 *****************************************************************/
package net.sf.taverna.perspectives;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.embl.ebi.escience.utils.TavernaSPIRegistry;

/**
 * SPI registry responsible for finding PerspectiveSPI's which define a UI perspective
 * @author Stuart Owen
 *
 */
public class PerspectiveRegistry extends TavernaSPIRegistry<PerspectiveSPI>{
	private static PerspectiveRegistry instance=new PerspectiveRegistry();
	
	private PerspectiveRegistry() {
		super(PerspectiveSPI.class);
	}
	
	public static PerspectiveRegistry getInstance() {
		return instance;
	}
	
	/**
	 * Returns a list of the discovered Perspectives, sorted by increasing positionHint 
	 * @return
	 */
	public List<PerspectiveSPI> getPerspectives() {
		List<PerspectiveSPI> result = findComponents();
		Collections.sort(result, new Comparator<PerspectiveSPI>() {
			public int compare(PerspectiveSPI o1, PerspectiveSPI o2) {
				return new Integer(o1.positionHint()).compareTo(new Integer(o2.positionHint()));
			}			
		});
		return result;
	}
}
