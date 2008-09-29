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
 * Filename           $RCSfile: Ontology.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:11 $
 *               by   $Author: stain $
 * Created on 02-Sep-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A simple interface for ontologies.
 * 
 * @author dturi
 * @version $Id: Ontology.java,v 1.1 2007-12-14 12:49:11 stain Exp $
 */
public interface Ontology {

    /**
     * Loads the default schema for the ontology.
     * @throws OntologyLoadSchemaException 
     */
    public void loadSchema() throws OntologyLoadSchemaException;

    /**
     * Returns the instance data as an XML-RDF String.
     * 
     * @return a {@link String}
     */
    public String getInstanceDataAsString();

    /**
     * Returns the whole model as an XML-RDF String.
     * 
     * @return a {@link String}
     */
    public String getModelAsString();

    public void loadInstanceData(String instanceData)
            throws OntologyLoadInstanceDataException;

    /**
     * Writes the ontology to <code>outFile</code>
     * 
     * @param outFile
     *            the output File
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void write(File outFile) throws IOException, FileNotFoundException;

    /**
     * Writes the ontology to standard output
     */
    public void writeOut();

    /**
     * Writes a protege-friendly version of the ontology to <code>outFile</code>
     * 
     * @param outFile
     *            the output File
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void writeForProtege(File outFile) throws IOException,
            FileNotFoundException;

}
