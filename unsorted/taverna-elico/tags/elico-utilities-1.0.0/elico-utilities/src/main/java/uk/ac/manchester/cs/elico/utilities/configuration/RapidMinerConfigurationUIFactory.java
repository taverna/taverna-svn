package uk.ac.manchester.cs.elico.utilities.configuration;

import javax.swing.JPanel;
import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Rishi Ramgolam<br>
 * Date: Jul 13, 2011<br>
 * The University of Manchester<br>
 **/

public class RapidMinerConfigurationUIFactory implements ConfigurationUIFactory {

	public boolean canHandle(String uuid) {
		return uuid.equals(getConfigurable().getUUID());	}

	public Configurable getConfigurable() {
		return RapidMinerPluginConfiguration.getInstance();
	}

	public JPanel getConfigurationPanel() {
		return new RapidMinerPluginPreferencesPlugin();
	}

}
