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
 * Filename           $RCSfile: MartServiceQueryConfigUIFactory.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-01-31 14:12:08 $
 *               by   $Author: davidwithers $
 * Created on 04-Apr-2006
 *****************************************************************/
package org.biomart.martservice.config.ui;

import java.awt.Component;

import org.biomart.martservice.MartDataset;
import org.biomart.martservice.MartService;
import org.biomart.martservice.MartServiceException;
import org.biomart.martservice.config.QueryConfigController;
import org.ensembl.mart.lib.config.AttributeCollection;
import org.ensembl.mart.lib.config.AttributeDescription;
import org.ensembl.mart.lib.config.AttributeGroup;
import org.ensembl.mart.lib.config.AttributeList;
import org.ensembl.mart.lib.config.AttributePage;
import org.ensembl.mart.lib.config.FilterCollection;
import org.ensembl.mart.lib.config.FilterDescription;
import org.ensembl.mart.lib.config.FilterGroup;
import org.ensembl.mart.lib.config.FilterPage;

/**
 * Implementation of the <code>QueryConfigUIFactory</code> interface that
 * creates a UI which looks like the Biomart web interface.
 * 
 * @author David Withers
 */
public class MartServiceQueryConfigUIFactory implements QueryConfigUIFactory {
	private QueryConfigUIFactory factory;

	public MartServiceQueryConfigUIFactory(MartService martService,
			QueryConfigController controller, MartDataset martDataset)
			throws MartServiceException {
		String version = controller.getMartQuery().getQuery().getSoftwareVersion();
		if (version == null || "0.4".equals(version)) {
			factory = new MartServiceQueryConfigUIFactory04(martService,
					controller, martDataset);
		} else if ("0.5".equals(version)) {
			factory = new MartServiceQueryConfigUIFactory05(martService,
					controller, martDataset);
		} else {
//			throw new MartServiceException("Unknown software version '"
//					+ version + "'");
			
			//try 0.4 because dictymart reports wierd versions
			factory = new MartServiceQueryConfigUIFactory04(martService,
					controller, martDataset);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getDatasetConfigUI(org.ensembl.mart.lib.config.DatasetConfig)
	 */
	public Component getDatasetConfigUI() throws MartServiceException {
		return factory.getDatasetConfigUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributePagesUI(org.ensembl.mart.lib.config.AttributePage[])
	 */
	public Component getAttributePagesUI(AttributePage[] attributePages,
			Object data) throws MartServiceException {
		return factory.getAttributePagesUI(attributePages, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributePageUI(org.ensembl.mart.lib.config.AttributePage)
	 */
	public Component getAttributePageUI(AttributePage attributePage, Object data)
			throws MartServiceException {
		return factory.getAttributePageUI(attributePage, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeGroupsUI(org.ensembl.mart.lib.config.AttributeGroup[])
	 */
	public Component getAttributeGroupsUI(AttributeGroup[] attributeGroups,
			Object data) throws MartServiceException {
		return factory.getAttributeGroupsUI(attributeGroups, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeGroupUI(org.ensembl.mart.lib.config.AttributeGroup)
	 */
	public Component getAttributeGroupUI(AttributeGroup attributeGroup,
			Object data) throws MartServiceException {
		return factory.getAttributeGroupUI(attributeGroup, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeCollectionsUI(org.ensembl.mart.lib.config.AttributeCollection[])
	 */
	public Component getAttributeCollectionsUI(
			AttributeCollection[] attributeCollections, Object data)
			throws MartServiceException {
		return factory.getAttributeCollectionsUI(attributeCollections, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeCollectionUI(org.ensembl.mart.lib.config.AttributeCollection)
	 */
	public Component getAttributeCollectionUI(
			AttributeCollection attributeCollection, Object data)
			throws MartServiceException {
		return factory.getAttributeCollectionUI(attributeCollection, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeDescriptionsUI(org.ensembl.mart.lib.config.AttributeDescription[],
	 *      int)
	 */
	public Component getAttributeDescriptionsUI(
			AttributeDescription[] attributeDescriptions, Object data)
			throws MartServiceException {
		return factory.getAttributeDescriptionsUI(attributeDescriptions, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeDescriptionUI(org.ensembl.mart.lib.config.AttributeDescription,
	 *      int)
	 */
	public Component getAttributeDescriptionUI(
			AttributeDescription attributeDescription, Object data)
			throws MartServiceException {
		return factory.getAttributeDescriptionUI(attributeDescription, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeListsUI(org.ensembl.mart.lib.config.AttributeList[],
	 *      int)
	 */
	public Component getAttributeListsUI(
			AttributeList[] attributeLists, Object data)
			throws MartServiceException {
		return factory.getAttributeListsUI(attributeLists, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getAttributeListUI(org.ensembl.mart.lib.config.AttributeList,
	 *      int)
	 */
	public Component getAttributeListUI(
			AttributeList attributeList, Object data)
			throws MartServiceException {
		return factory.getAttributeListUI(attributeList, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterPagesUI(org.ensembl.mart.lib.config.FilterPage[])
	 */
	public Component getFilterPagesUI(FilterPage[] filterPages, Object data)
			throws MartServiceException {
		return factory.getFilterPagesUI(filterPages, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterPageUI(org.ensembl.mart.lib.config.FilterPage)
	 */
	public Component getFilterPageUI(FilterPage filterPage, Object data)
			throws MartServiceException {
		return factory.getFilterPageUI(filterPage, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterGroupsUI(org.ensembl.mart.lib.config.FilterGroup[])
	 */
	public Component getFilterGroupsUI(FilterGroup[] filterGroups, Object data)
			throws MartServiceException {
		return factory.getFilterGroupsUI(filterGroups, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterGroupUI(org.ensembl.mart.lib.config.FilterGroup)
	 */
	public Component getFilterGroupUI(FilterGroup filterGroup, Object data)
			throws MartServiceException {
		return factory.getFilterGroupUI(filterGroup, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterCollectionsUI(org.ensembl.mart.lib.config.FilterCollection[])
	 */
	public Component getFilterCollectionsUI(
			FilterCollection[] filterCollections, Object data)
			throws MartServiceException {
		return factory.getFilterCollectionsUI(filterCollections, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterCollectionUI(org.ensembl.mart.lib.config.FilterCollection)
	 */
	public Component getFilterCollectionUI(FilterCollection filterCollection,
			Object data) throws MartServiceException {
		return factory.getFilterCollectionUI(filterCollection, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterDescriptionsUI(org.ensembl.mart.lib.config.FilterDescription[],
	 *      int)
	 */
	public Component getFilterDescriptionsUI(
			FilterDescription[] filterDescriptions, Object data)
			throws MartServiceException {
		return factory.getFilterDescriptionsUI(filterDescriptions, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.biomartservice.config.QueryConfigUIFactory#getFilterDescriptionUI(org.ensembl.mart.lib.config.FilterDescription,
	 *      int)
	 */
	public Component getFilterDescriptionUI(
			FilterDescription filterDescription, Object data)
			throws MartServiceException {
		return factory.getFilterDescriptionUI(filterDescription, data);
	}

}