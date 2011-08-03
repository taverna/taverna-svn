/*******************************************************************************
 * Copyright (C) 2009-2010 The University of Manchester
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
package net.sf.taverna.t2.workbench;

import java.util.Comparator;

import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

/**
 * SPI for components/plugins that want to be able to perform some configuration
 * or similar initialization on Workbench startup.
 *
 * @see ShutdownSPI
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 */
public interface StartupSPI {

	/**
	 * Called when the Workbench is starting up for implementations of this
	 * interface to perform any configuration on start up.
	 * <p>
	 * When the configuration process has finished this method should return
	 * <code>true</code>.
	 * <p>
	 * Return <code>false</code> if and only if failure in this method will
	 * cause Workbench not to function at all.
	 *
	 */
	public boolean startup();

	/**
	 * Provides a hint for the order in which the startup hooks (that implement
	 * this interface) will be called. The lower the number, the earlier will
	 * the startup hook be invoked.
	 * <p>
	 * Custom plugins are recommended to start with a value > 100.
	 */
	public int positionHint();

	public class StartupComparator implements Comparator<StartupSPI> {
		public int compare(StartupSPI o1, StartupSPI o2) {
			return o1.positionHint() - o2.positionHint();
		}
	}

}
