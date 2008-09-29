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
 * Filename           $RCSfile: MetadataServiceFactory.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:08 $
 *               by   $Author: stain $
 * Created on 29-Sep-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

/**
 * Factory for {@link MetadataService}s.
 * 
 * @author dturi
 * @version $Id: NamedRDFGraphsPersisterFactory.java,v 1.1 2005/10/12 11:06:04
 *          turid Exp $
 */
public class MetadataServiceFactory {
    /**
     * Logger for this class
     */
    private static Logger logger = Logger
            .getLogger(MetadataServiceFactory.class);

    private static Map<String, MetadataService> persisters = new HashMap<String, MetadataService>();

    /**
     * Creates a {@link MetadataService} using <code>className</code>.
     * 
     * @param className
     *            a String
     * @return {@link MetadataService}
     * @throws MetadataServiceCreationException
     */
    static public MetadataService getInstance(String className)
            throws MetadataServiceCreationException {
        synchronized (persisters) {
            MetadataService persister = persisters.get(className);
            if (persister != null) {
                return persister;
            }
            try {
                persister = (MetadataService) Class.forName(className)
                        .newInstance();
            } catch (InstantiationException e) {
                logger.error(e);
                throw new MetadataServiceCreationException(e);
            } catch (IllegalAccessException e) {
                logger.error(e);
                throw new MetadataServiceCreationException(e);
            } catch (ClassNotFoundException e) {
                logger.error(e);
                throw new MetadataServiceCreationException(e);
            }
            persisters.put(className, persister);
            return persister;
        }
    }

    /**
     * Creates a {@link MetadataService} using the value of
     * {@link ProvenanceConfigurator#KAVE_TYPE_KEY} in the properties.
     * 
     * @param configuration
     * @return a {@link MetadataService}
     * @throws MetadataServiceCreationException
     */
    static public MetadataService getInstance(Properties configuration)
            throws MetadataServiceCreationException {
        String storeType = configuration
                .getProperty(ProvenanceConfigurator.KAVE_TYPE_KEY,
                        ProvenanceConfigurator.DEFAULT_KAVE_TYPE);
        if (storeType == null)
            throw new MetadataServiceCreationException(
                    "Could not create metadata service since the property "
                            + ProvenanceConfigurator.KAVE_TYPE_KEY
                            + " is missing.\nCheck provenance.properties file in conf folder.");
        MetadataService metadataService;
        try {
            if (storeType.equals(ProvenanceConfigurator.JENA)
                    || storeType.equals(ProvenanceConfigurator.JENA_MYSQL))
                metadataService = (MetadataService) Class
                        .forName(
                                "uk.ac.man.cs.img.mygrid.provenance.knowledge.store.JenaMetadataService")
                        .newInstance();
            else if (storeType.toLowerCase().startsWith(ProvenanceConfigurator.BOCA.toLowerCase()))
                metadataService = (MetadataService) Class
                        .forName(
                                "uk.org.mygrid.logbook.metadataservice.BocaRemoteMetadataService")
                        .newInstance();
            else if (storeType.equals(ProvenanceConfigurator.SESAME))
                metadataService = (MetadataService) Class
                        .forName(
                                "uk.ac.man.cs.img.mygrid.provenance.knowledge.store.SesameNativeMetadataService")
                        .newInstance();
            else
                throw new MetadataServiceCreationException(
                        "Could not create metadata service: property "
                                + ProvenanceConfigurator.KAVE_TYPE_KEY + " = "
                                + storeType + " is not recognised.");
            metadataService.setConfiguration(configuration);
            metadataService.initialise();
            logger.debug("Created metadata service of type " + storeType);
            return metadataService;
        } catch (InstantiationException e) {
            throw new MetadataServiceCreationException(e);
        } catch (IllegalAccessException e) {
            throw new MetadataServiceCreationException(e);
        } catch (ClassNotFoundException e) {
            throw new MetadataServiceCreationException(e);
        } catch (MetadataServiceException e) {
            throw new MetadataServiceCreationException(e);
        }
    }

}
