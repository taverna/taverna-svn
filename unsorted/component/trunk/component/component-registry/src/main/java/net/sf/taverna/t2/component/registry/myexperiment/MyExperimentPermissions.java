/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.component.registry.myexperiment;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author David Withers
 */
public class MyExperimentPermissions {

	public static final MyExperimentPermissions PRIVATE = new MyExperimentPermissions(Sharing.PUBLIC);
	public static final MyExperimentPermissions PUBLIC = new MyExperimentPermissions(Sharing.PRIVATE);

	public enum Sharing {PUBLIC, PRIVATE}

	private final Sharing sharing;
	private final List<MyExperimentGroup> groups;

	public MyExperimentPermissions(Sharing sharing) {
		this(sharing, new ArrayList<MyExperimentGroup>());
	}

	public MyExperimentPermissions(Sharing sharing, List<MyExperimentGroup> groups) {
		this.sharing = sharing;
		this.groups = groups;
	}

	public Sharing getSharing() {
		return sharing;
	}

	public List<MyExperimentGroup> getGroups() {
		return groups;
	}

}
