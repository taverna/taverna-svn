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
 * Filename           $RCSfile: JenaOntologyFactory.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:09 $
 *               by   $Author: stain $
 * Created on 02-Sep-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Factory for {@link uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaOntology}s.
 * 
 * @author dturi
 * @version $Id: JenaOntologyFactory.java,v 1.1 2007-12-14 12:49:09 stain Exp $
 */
public class JenaOntologyFactory {

    /**
     * Creates a JenaOntology of using <code>className</code>.
     * @param className a String
     * @return a {@link JenaOntology}
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    static public JenaOntology getInstance(String className)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        JenaOntology ontology = (JenaOntology) Class.forName(className)
                .newInstance();
        return ontology;
    }

    /**
     * Creates a JenaOntology of using <code>className</code> and sets its
     * instance data to <code>instanceData</code>.
     * @param className a String
     * @param instanceData a {@link Model}
     * @return a {@link JenaOntology}
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    static public JenaOntology getInstance(String className, Model instanceData)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        JenaOntology ontology = getInstance(className);
        ontology.loadInstances(instanceData);
        return ontology;
    }

}
