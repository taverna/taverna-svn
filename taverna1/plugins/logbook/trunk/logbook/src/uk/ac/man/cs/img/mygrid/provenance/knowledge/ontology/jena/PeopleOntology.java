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
 * Filename           $RCSfile: PeopleOntology.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:10 $
 *               by   $Author: stain $
 * Created on 24-Jun-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena;

import java.net.URI;
import java.util.Collection;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.util.Experimenter;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Wrapper for the People ontology.
 * 
 * @author dturi
 * @version $Id: PeopleOntology.java,v 1.1 2007-12-14 12:49:10 stain Exp $
 */
public class PeopleOntology extends JenaOntologyAdapter {
    public static final String DEFAULT_ONTOLOGY_URL = "http://www.cs.man.ac.uk/~dturi/ontologies/people.owl";
    
    //private static final Logger logger = Logger.getLogger(PeopleOntology.class);

    public String getNamespace() {
        return "http://www.mygrid.org/people.owl";
    }

    public String getOntologyPrefix() {
        return "people";
    }

    public String getOntologyURL() {
        String ontologyURL = System.getProperty("mygrid.kave.ontology.people",
                DEFAULT_ONTOLOGY_URL);
        return ontologyURL;
    }

    /**
     * Creates an in-memory copy of the provenance ontology at
     * {@link #PEOPLE_OWL_URL}. It contains no instance data. To populate this
     * ontology use the <code>addXXX()</code> methods in this class.
     */
    public PeopleOntology() {
        super();
    }

    /**
     * Creates a version of the provenance ontology schema one can reason about
     * and populated with the instance data at <code>instanceDataURI</code>.
     * 
     * @param instanceDataURI
     *            the {@link URI}of the instance data.
     */
    public PeopleOntology(URI instanceDataURI) {
        super(instanceDataURI);
    }

    /**
     * Creates an in-memory copy of the provenance ontology at
     * {@link #PEOPLE_OWL_URL}and then adds <code>model</code> to it.
     * 
     * @param rdf
     *            a {@link Model}.
     */
    public PeopleOntology(Model rdf) {
        super(rdf);
    }

    /**
     * Creates an in-memory copy of the provenance ontology at
     * {@link #PEOPLE_OWL_URL}and then adds each of the <code>models</code>
     * to it.
     * 
     * @param models
     *            a Collection of {@link Model}.
     */
    public PeopleOntology(Collection models) {
        super(models);
    }

    public void addPerson(String lsid, String firstName, String middleName,
            String familyName) {
        Individual person = model.createIndividual(lsid, PeopleVocab.PERSON);
        model.add(person, PeopleVocab.FIRST_NAME, model
                .createTypedLiteral(firstName));
        model.add(person, PeopleVocab.MIDDLE_NAME, model
                .createTypedLiteral(middleName));
        model.add(person, PeopleVocab.FAMILY_NAME, model
                .createTypedLiteral(familyName));

    }

    public void addPerson(String lsid, String userName, String cryptedPassword,
            String firstName, String middleName, String familyName,
            String organization, String group) {
        Individual person = model.createIndividual(lsid, PeopleVocab.PERSON);
        model.add(person, PeopleVocab.USER_NAME, model
                .createTypedLiteral(userName));
        model.add(person, PeopleVocab.CRYPTED_PASSWORD, model
                .createTypedLiteral(cryptedPassword));
        model.add(person, PeopleVocab.FIRST_NAME, model
                .createTypedLiteral(firstName));
        model.add(person, PeopleVocab.MIDDLE_NAME, model
                .createTypedLiteral(middleName));
        model.add(person, PeopleVocab.FAMILY_NAME, model
                .createTypedLiteral(familyName));
        model.add(person, PeopleVocab.ORGANIZATION, model
                .createTypedLiteral(organization));
        model.add(person, PeopleVocab.GROUP, model.createTypedLiteral(group));
    }

    public String getPropertyValue(String sourceIndividual, String propertyName) {
        Individual individual = model.getIndividual(sourceIndividual);
        Property property = model.getProperty(propertyName);
        RDFNode propertyValue = individual.getPropertyValue(property);
        if (propertyValue == null)
            return null;
        String[] split = propertyValue.toString().split("\\^");
        return split[0];
    }

    public String getUsername(String personLSID) {
        String value = getPropertyValue(personLSID, PeopleVocab.USER_NAME
                .getURI());
        return value;
    }

    public String getCryptedPassword(String personLSID) {
        String value = getPropertyValue(personLSID,
                PeopleVocab.CRYPTED_PASSWORD.getURI());
        return value;
    }

    public String getFirstName(String personLSID) {
        String value = getPropertyValue(personLSID, PeopleVocab.FIRST_NAME
                .getURI());
        return value;
    }

    public String getMiddleName(String personLSID) {
        String value = getPropertyValue(personLSID, PeopleVocab.MIDDLE_NAME
                .getURI());
        return value;
    }

    public String getFamilyName(String personLSID) {
        String value = getPropertyValue(personLSID, PeopleVocab.FAMILY_NAME
                .getURI());
        return value;
    }

    public String getOrganization(String personLSID) {
        String value = getPropertyValue(personLSID, PeopleVocab.ORGANIZATION
                .getURI());
        return value;
    }

    public String getGroup(String personLSID) {
        String value = getPropertyValue(personLSID, PeopleVocab.GROUP.getURI());
        return value;
    }

    public Experimenter toExperimenter(String personLSID) {
        Experimenter experimenter = new Experimenter();
        experimenter.setUsername(getUsername(personLSID));
        experimenter.setCryptedPassword(getCryptedPassword(personLSID));
        experimenter.setFirstName(getFirstName(personLSID));
        experimenter.setMiddleName(getMiddleName(personLSID));
        experimenter.setFamilyName(getFamilyName(personLSID));
        experimenter.setOrganization(getOrganization(personLSID));
        experimenter.setGroup(getGroup(personLSID));
        return experimenter;
    }
}