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
 * Filename           $RCSfile: JenaOntologyAdapter.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:09 $
 *               by   $Author: stain $
 * Created on 24-Jun-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;

/**
 * Abstract implementation of
 * {@link uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaOntology}.
 * 
 * @author dturi
 * @version $Id: JenaOntologyAdapter.java,v 1.1 2007-12-14 12:49:09 stain Exp $
 */
public abstract class JenaOntologyAdapter implements JenaOntology {
    /**
     * Logger for this class
     */
    public static final Logger logger = Logger
            .getLogger(JenaOntologyAdapter.class);

    // public String ontologyPrefix = "provenance";
    //
    // public String ontologyURL = System
    // .getProperty("mygrid.kave.generate.people.ontology");
    //
    // public String namespace = "http://www.mygrid.org/people.owl";
    //
    // public String fullNameSpace = namespace + "#";

    protected OntModel model;

    private boolean isSchemaLoaded;

    /**
     * Creates an in-memory copy of the provenance ontology at
     * {@link #ontologyURL}. It contains no instance data. To populate this
     * ontology use the <code>addXXX()</code> methods in this class.
     */
    public JenaOntologyAdapter() {
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM_RDFS_INF);

        model = ModelFactory.createOntologyModel(spec, ModelFactory
                .createDefaultModel());

        model.getDocumentManager()
                .addAltEntry(getNamespace(), getOntologyURL());
        model.getDocumentManager().addPrefixMapping(getFullNamespace(),
                getOntologyPrefix());
        model.getDocumentManager().setUseDeclaredPrefixes(true);
        // model.read(PROVENANCE_OWL_URL);
        // model.setNsPrefix(PROVENANCE_PREFIX, PROVENANCE_OWL_URL);

    }

    /**
     * Creates a version of the provenance ontology schema one can reason about
     * and populated with the instance data at <code>instanceDataURI</code>.
     * 
     * @param instanceDataURI
     *            the {@link URI}of the instance data.
     */
    public JenaOntologyAdapter(URI instanceDataURI) {
        Model tBox = ModelFactory.createDefaultModel();
        tBox.read(getOntologyURL());
        Model aBox = ModelFactory.createDefaultModel();
        aBox.read(instanceDataURI.toString());

        Reasoner reasoner = ReasonerRegistry.getOWLReasoner().bindSchema(tBox);

        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM_RULE_INF);
        spec.setReasoner(reasoner);
        model = ModelFactory.createOntologyModel(spec, aBox);
        model.getDocumentManager().addPrefixMapping(getFullNamespace(),
                getOntologyPrefix());
        model.getDocumentManager().setUseDeclaredPrefixes(true);
    }

    /**
     * Creates an in-memory copy of the provenance ontology at
     * {@link #ontologyURL}and then adds <code>model</code> to it.
     * 
     * @param rdf
     *            a {@link Model}.
     */
    public JenaOntologyAdapter(Model rdf) {
        this();
        loadInstances(rdf);
    }
    
    /**
     * Creates ontology and adds <code>instanceData</code> to it.
     * @param instanceData a String representing instance data in XML-RDF form.
     */
    public JenaOntologyAdapter(String instanceData) {
        this();
        loadInstanceData(instanceData);
    }

    public void loadInstanceData(String instanceData) {
        Model aBox = ModelFactory.createDefaultModel();
        aBox.read(new StringReader(instanceData), getNamespace());
        loadInstances(aBox);
    }

    public void loadInstances(Model rdf) {
        loadSchema();
        model.add(rdf);
    }

    public void loadInstances(String rdf) {
        Model aBox = ModelFactory.createDefaultModel();
        aBox.read(new StringReader(rdf), getNamespace());
        loadInstances(aBox);
    }
    
    public void loadSchema() {
        if (isSchemaLoaded)
            return;
        model.read(getOntologyURL());
        isSchemaLoaded = true;
    }

    /**
     * Creates an in-memory copy of the provenance ontology at
     * {@link #ontologyURL}and then adds each of the <code>models</code> to
     * it.
     * 
     * @param models
     *            a Collection of {@link Model}.
     */
    public JenaOntologyAdapter(Collection models) {
        this();
        loadInstances(models);
    }

    /**
     * Populates the ontology with each of the <code>models</code> (expected
     * to consist of instance data).
     * 
     * @param models
     *            a Collection of {@link Model}s.
     */
    public void loadInstances(Collection models) {
        loadSchema();
        for (Iterator iter = models.iterator(); iter.hasNext();) {
            Model rdf = (Model) iter.next();
            model.add(rdf);
        }
    }

    /**
     * Returns the instance data in the ontology.
     * 
     * @return a {@link Model}.
     */
    public Model getInstanceData() {
        return model.getBaseModel();
    }

    /**
     * Returns the {@link OntModel}.
     * 
     * @return an {@link OntModel}
     */
    public OntModel getOntModel() {
        return model;
    }

    /**
     * Returns the {@link OntModel} as a simple {@link Model}.
     * 
     * @return a {@link Model}
     */
    public Model getModel() {
        return model;
    }
    
    /* (non-Javadoc)
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.Ontology#getInstanceDataAsString()
     */
    public String getInstanceDataAsString() {
        Model instanceData = getInstanceData();
        StringWriter writer = new StringWriter();
        instanceData.write(writer, "RDF/XML", getNamespace());
        return writer.toString();
    }
    
    /* (non-Javadoc)
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.Ontology#getModelAsString()
     */
    public String getModelAsString() {
        Model model = getModel();
        StringWriter writer = new StringWriter();
        model.write(writer, "RDF/XML", getNamespace());
        return writer.toString();
    }

    /**
     * Returns the provenance ontology fetching it from {@link #ontologyURL}.
     * 
     * @return an {@link OntModel}.
     */
    public OntModel getSchema() {
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM_RDFS_INF);
        OntModel schema = ModelFactory.createOntologyModel(spec);
        schema.read(getOntologyURL());
        return schema;
    }
    
    public void writeOut() {
        model.write(System.out, "RDF/XML", getNamespace());
    }

    /**
     * Writes the ontology to <code>outFile</code>
     * 
     * @param outFile
     *            the output File
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void write(File outFile) throws IOException, FileNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug("writeForProtege(File outFile = " + outFile
                    + ") - start");
        }

        Writer writer = new FileWriter(outFile);
        model.write(writer, "RDF/XML", getNamespace());
        writer.close();

        if (logger.isDebugEnabled()) {
            logger.debug("writeForProtege(File) - end");
        }
    }

    /**
     * Writes a protege-friendly version of the ontology to <code>outFile</code>
     * 
     * @param outFile
     *            the output File
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void writeForProtege(File outFile) throws IOException,
            FileNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug("writeForProtege(File outFile = " + outFile
                    + ") - start");
        }

        File tmp = File.createTempFile("tmp", ".owl");
        Writer writer = new FileWriter(tmp);

        model.write(writer, "RDF/XML", getNamespace());

        // RDFWriter w = model.getWriter("RDF/XML");
        // w.setProperty("xmlbase", "http://www.mygrid.org/provenance");
        // //w.setProperty("relativeURIs","same-document,relative");
        // w.write(model, writer, "http://www.mygrid.org/provenance");

        writer.close();

        File tmp1 = File.createTempFile("tmp", ".owl");
        File tmp2 = File.createTempFile("tmp", ".owl");
        File tmp3 = File.createTempFile("tmp", ".owl");
        File tmp4 = File.createTempFile("tmp", ".owl");
        tmp.deleteOnExit();
        tmp1.deleteOnExit();
        tmp2.deleteOnExit();
        tmp3.deleteOnExit();
        tmp4.deleteOnExit();
        replace("xmlns=", "xml:base=\"" + getNamespace() + "\" xmlns=", tmp,
                tmp1);
        replace("rdf:about", "rdf:ID", tmp1, tmp2);
        replace("rdf:ID=\"" + getFullNamespace(), "rdf:ID=\"", tmp2, tmp3);
        replace("rdf:ID=\"#", "rdf:ID=\"", tmp3, tmp4);
        replace("rdf:resource=\"" + getFullNamespace(), "rdf:resource=\"#", tmp4,
                outFile);

        if (logger.isDebugEnabled()) {
            logger.debug("writeForProtege(File) - end");
        }
    }

    static public void replace(String pattern, String replacement, File in, File out)
            throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(in));
        PrintWriter writer = new PrintWriter(new FileWriter(out));
        String line;
        Pattern p = Pattern.compile(pattern);
        while ((line = reader.readLine()) != null) {
            StringBuffer sb = new StringBuffer();
            Matcher m = p.matcher(line);
            boolean result = m.find();
            while (result) {
                m.appendReplacement(sb, replacement);
                result = m.find();
            }
            m.appendTail(sb);
            writer.println(sb.toString());
        }
        reader.close();
        writer.close();
    }

    public static String bracketify(String uri) {
        return "<" + uri + ">";
    }

    public static String stringLiteral(String value) {
        return "\"" + value + "\"^^xsd:string";
    }

    public abstract String getNamespace();

    public abstract String getOntologyPrefix();

    public abstract String getOntologyURL();

    public String getFullNamespace() {
        return getNamespace() + "#";
    }
}