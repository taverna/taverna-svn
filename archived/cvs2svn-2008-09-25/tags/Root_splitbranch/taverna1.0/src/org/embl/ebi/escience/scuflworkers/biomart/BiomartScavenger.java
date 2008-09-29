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
 * Filename           $RCSfile: BiomartScavenger.java,v $
 * Revision           $Revision: 1.6 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-05-22 10:49:41 $
 *               by   $Author: sowen70 $
 * Created on 17-Mar-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.biomart;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.biomart.martservice.MartDataset;
import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartRegistry;
import org.biomart.martservice.MartService;
import org.biomart.martservice.MartURLLocation;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

/**
 * 
 * @author David Withers
 */
public class BiomartScavenger extends Scavenger {

	private static Logger logger=Logger.getLogger(BiomartScavenger.class);
	
	public BiomartScavenger(String registryURL)
			throws ScavengerCreationException {
		super("Biomart service @ " + registryURL);
		
		logger.info("Initialising Biomart Scavenger for URL:"+registryURL);
		
		URL registryLocation;
		try {
			registryLocation = new URL(getBiomartServiceLocation(registryURL));
		} catch (MalformedURLException mue) {
			throw new ScavengerCreationException("Not a valid URL : "
					+ mue.getMessage());
		}
		try {
			MartService martService = new MartService(registryLocation
					.toString());
			MartRegistry registry = martService.getRegistry();
			MartURLLocation[] martURLLocations = registry.getMartURLLocations();
			for (int i = 0; i < martURLLocations.length; i++) {
				if (martURLLocations[i].isVisible()) {
					DefaultMutableTreeNode adaptorNode = new DefaultMutableTreeNode(
							martURLLocations[i].getDisplayName());
					add(adaptorNode);
					MartDataset[] datasets = martService
							.getDatasets(martURLLocations[i]);
					Arrays.sort(datasets, MartDataset.getDisplayComparator());
					for (int j = 0; j < datasets.length; j++) {
						if (datasets[j].isVisible()) {
							MartQuery biomartQuery = new MartQuery(martService,
									datasets[j]);
							BiomartProcessorFactory bpf = new BiomartProcessorFactory(
									biomartQuery);
							adaptorNode.add(new DefaultMutableTreeNode(bpf));
						}
					}
				}
			}
		} catch (Exception ex) {
			ScavengerCreationException sce = new ScavengerCreationException(
					"Cannot create Biomart scavenger!\n" + ex.getMessage());
			sce.initCause(ex);
			throw sce;
		}
	}

	/**
	 * Attempts to construct a valid MartService URL fron the location given.
	 * 
	 * @param biomartLocation
	 * @return a (hopefully) valid MartService URL
	 */
	private String getBiomartServiceLocation(String biomartLocation) {
		StringBuffer sb = new StringBuffer();
		if (biomartLocation.endsWith("martservice")) {
			sb.append(biomartLocation);
		} else if (biomartLocation.endsWith("martview")) {
			sb.append(biomartLocation.substring(0, biomartLocation
					.lastIndexOf("martview")));
			sb.append("martservice");
		} else if (biomartLocation.endsWith("/")) {
			sb.append(biomartLocation);
			sb.append("martservice");
		} else {
			sb.append(biomartLocation);
			sb.append("/martservice");
		}
		return sb.toString();
	}

}
