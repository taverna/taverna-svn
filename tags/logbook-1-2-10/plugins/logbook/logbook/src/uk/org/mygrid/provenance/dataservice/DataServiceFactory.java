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
 * Filename           $RCSfile: DataServiceFactory.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:07 $
 *               by   $Author: stain $
 * Created on 29-Sep-2005
 *****************************************************************/
package uk.org.mygrid.provenance.dataservice;

import java.util.Properties;

import org.apache.log4j.Logger;

import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

/**
 * Factory for {@link DataService}s.
 * 
 * @author dturi
 * @version $Id: DataServiceFactory.java,v 1.1 2007-12-14 12:49:07 stain Exp $
 */
public class DataServiceFactory {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger
            .getLogger(DataServiceFactory.class);

    /**
     * Creates a {@link DataService} using the value of the
     * {@link ProvenanceConfigurator#DATASERVICE_TYPE_KEY} in the properties.
     * 
     * @param configuration
     * @return a {@link DataService}
     * @throws DataServiceException
     */
    static public DataService getInstance(Properties configuration)
            throws DataServiceException {
        String storeType = configuration.getProperty(
                ProvenanceConfigurator.DATASERVICE_TYPE_KEY,
                ProvenanceConfigurator.MYSQL);
        if (storeType == null)
            throw new DataServiceCreationException(
                    "Could not create data service: property "
                            + ProvenanceConfigurator.DATASERVICE_TYPE_KEY
                            + " missing");
        DataService dataService;
        if (storeType.equals(ProvenanceConfigurator.HYPERSONIC))
            dataService = new HypersonicDataService(configuration);
        else if (storeType.equals(ProvenanceConfigurator.MYSQL))
            dataService = new MySQLDataService(configuration);
        else if (storeType.equals(ProvenanceConfigurator.DERBY))
            dataService = new DerbyDataService(configuration);
        else
            throw new DataServiceCreationException(
                    "Could not create data service: property "
                            + ProvenanceConfigurator.DATASERVICE_TYPE_KEY
                            + " = " + storeType + " is not recognised.");
        logger.debug("Created data service of type " + storeType);
        return dataService;
    }

}
