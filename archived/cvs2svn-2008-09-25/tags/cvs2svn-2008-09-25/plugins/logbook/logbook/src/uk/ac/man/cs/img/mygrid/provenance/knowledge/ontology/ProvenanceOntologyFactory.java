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
 * Filename           $RCSfile: ProvenanceOntologyFactory.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:12 $
 *               by   $Author: stain $
 * Created on 02-Sep-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology;

import java.util.Properties;

import org.apache.log4j.Logger;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

/**
 * Factory for
 * {@link uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaOntology}s.
 * 
 * @author dturi
 * @version $Id: ProvenanceOntologyFactory.java,v 1.3 2006/05/24 14:29:40
 *          soilands Exp $
 */
public class ProvenanceOntologyFactory {

    private static Logger logger = Logger
            .getLogger(ProvenanceOntologyFactory.class);

    public static final String PROVENANCE_ONTOLOGY_KEY = "mygrid.kave.model";

    public static final String DEFAULT_PROVENANCE_ONTOLOGY = // "uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology";
    "uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.sesame.SesameProvenanceOntology";

    /**
     * Creates a ProvenanceOntology using <code>className</code>.
     * 
     * @param className
     *            a String
     * @return a {@link ProvenanceOntology}
     * @throws ProvenanceOntologyCreationException
     */
    static public ProvenanceOntology getInstance(String className)
            throws ProvenanceOntologyCreationException {
        ProvenanceOntology ontology;
        try {
            ontology = (ProvenanceOntology) Class.forName(className)
                    .newInstance();
            logger.debug("Ontology = " + ontology.getClass());
        } catch (InstantiationException e) {
            throw new ProvenanceOntologyCreationException(e);
        } catch (IllegalAccessException e) {
            throw new ProvenanceOntologyCreationException(e);
        } catch (ClassNotFoundException e) {
            throw new ProvenanceOntologyCreationException(e);
        }
        return ontology;
    }

    /**
     * Creates a ProvenanceOntology using the value of the
     * {@link ProvenanceConfigurator#KAVE_TYPE_KEY} system property. Default is
     * {@link JenaProvenanceOntology}
     * 
     * @return a {@link ProvenanceOntology}
     * @throws ProvenanceOntologyCreationException
     */
    static public ProvenanceOntology getInstance(Properties configuration)
            throws ProvenanceOntologyCreationException {
//        String storeType = configuration.getProperty(
//                ProvenanceConfigurator.KAVE_TYPE_KEY,
//                ProvenanceConfigurator.DEFAULT_KAVE_TYPE);
        ProvenanceOntology ontology;
//        if (storeType.equals(ProvenanceConfigurator.JENA)
//                || storeType.equals(ProvenanceConfigurator.JENA_MYSQL))
            ontology = new JenaProvenanceOntology();
//        else
//            try {
//                ontology = new JenaProvenanceOntology();
//            } catch (IOException e) {
//                throw new ProvenanceOntologyCreationException(e);
//            } catch (RDFParseException e) {
//                throw new ProvenanceOntologyCreationException(e);
//            } catch (SailUpdateException e) {
//                throw new ProvenanceOntologyCreationException(e);
//            } catch (SailInitializationException e) {
//                throw new ProvenanceOntologyCreationException(e);
//            }
        return ontology;
    }

}
