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
package net.sf.taverna.t2.activities.rshell.views;

import uk.org.taverna.scufl2.api.port.ActivityPort;
import net.sf.taverna.t2.activities.rshell.RshellPortTypes.DataTypes;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityPortConfiguration;

/**
 *
 *
 * @author David Withers
 */
public class RshellActivityPortConfiguration extends ActivityPortConfiguration {

	private DataTypes dataType;

	/**
	 * Constructs a new <code>RshellActivityPortConfiguration</code>.
	 * @param activityPort
	 */
	public RshellActivityPortConfiguration(ActivityPort activityPort, DataTypes dataType) {
		super(activityPort);
		this.dataType = dataType;
	}

	public RshellActivityPortConfiguration(String port, DataTypes dataType) {
		super(port, dataType.getDepth(), dataType.getDepth());
		this.dataType = dataType;
	}

	public DataTypes getDataType() {
		return dataType;
	}

	public void setDataType(DataTypes dataType) {
		this.dataType = dataType;
	}

	public int getDepth() {
		return dataType.getDepth();
	}

	public int getGranularDepth() {
		return dataType.getDepth();
	}

}
