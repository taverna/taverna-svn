/*********************************************************
 * This source file is a component of the Gene Ontology  *
 * visualization toolset. This project is developed      *
 * by the eScience group at the European Bioinformatics  *
 * Institute, unless otherwise stated this file is       *
 * copyright Tom Oinn (tmo@ebi.ac.uk), and released      *
 * under the GNU GPL.                                    *
 *********************************************************
 */
package org.embl.ebi.escience.goviz;

import uk.ac.ebi.ego.GoRelation;
import uk.ac.ebi.ego.GoTerm;
import uk.ac.ebi.ego.GoTermFactory;
import uk.ac.ebi.ego.GoTermUnavailableException;
import uk.ac.ebi.factory.FactoryBag;
import uk.ac.ebi.factory.FactoryConfigurationException;
import uk.ac.ebi.factory.FactoryNotFoundException;
import uk.ac.ebi.factory.implementation.DefaultFactoryBag;
import uk.ac.ebi.factory.toolkit.DataLoadFailedException;
import uk.ac.ebi.factory.toolkit.SimpleQuery;

// Utility Imports
import java.util.HashMap;
import java.util.Iterator;

import java.lang.ClassLoader;
import java.lang.String;
import java.lang.StringBuffer;



/**
 * A dictionary of GO term containers used to store the
 * information to render to DOT
 * @author Tom Oinn
 */
public class TermSet {
    
    private HashMap terms;
    private static FactoryBag fb;
    private static GoTermFactory gtf;

    static {
	try {
	    fb = new DefaultFactoryBag();
	    DefaultFactoryBag.configure(ClassLoader.getSystemResource("uk/ac/ebi/ego/examples/sample-config.xml"),fb);
	    gtf = (GoTermFactory)fb.getFactory(uk.ac.ebi.ego.GoTerm.class.getName());
	}
	catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
    
    public TermSet() throws 
	FactoryConfigurationException,
	FactoryNotFoundException {
	super();
	this.terms = new HashMap();
    }
    
    /**
     * Return the Dot representation of the term set
     */
    public String toDot() throws DataLoadFailedException {
	StringBuffer dot = new StringBuffer();
	dot.append("digraph go_graph {\n");
	dot.append(" graph [             \n");
	dot.append("  style=\"\"         \n");
	dot.append(" ];                  \n"); 
	// Overall node style
	dot.append(" node [              \n");
	dot.append("  shape=\"box\",\n");
	dot.append("  height=\"0\",\n");
	dot.append("  width=\"0\",\n");
	dot.append("  fontname=\"Courier\",         \n");
	dot.append("  fontsize=\"10\",              \n");
	dot.append("  fontcolor=\"black\",  \n");
	dot.append("  color=\"black\",               \n");
	dot.append("  fillcolor=\"lightgoldenrodyellow\",\n");
	dot.append("  style=\"filled\"  \n");
	dot.append(" ];\n\n");
	// Overall edge style
	dot.append(" edge [                         \n");
	dot.append("  fontname=\"Courier\",         \n");
	dot.append("  fontsize=\"10\",              \n");
	dot.append("  fontcolor=\"black\",  \n");
	dot.append("  color=\"black\"                \n");
	dot.append(" ];\n\n");
	
	// Show terms and edges
	for (Iterator i = this.terms.values().iterator(); i.hasNext(); ) {
	    TermContainer tc = (TermContainer)i.next();
	    dot.append(" "+stripColon(tc.theTerm.getAccession())+" [\n");
	    dot.append("  label = \""+tc.theTerm.getName().replaceAll(" ","\\\\n")+"\\n"+tc.theTerm.getAccession()+"\"\n");
	    if (tc.hitCount>0) {
		dot.append("  fillcolor = \"mediumpurple1\"\n");
	    }
	    dot.append(" ];\n");
	    // Show edges
	    GoRelation[] relations = tc.theTerm.getParentRelations();
	    for (int j = 0; j < relations.length; j++) {
		dot.append(" "+stripColon(relations[j].getParent().getAccession())+"->"+stripColon(tc.theTerm.getAccession())+" [\n");
		dot.append("  label = \""+relations[j].getRelationType()+"\"\n");
		dot.append(" ];\n");
	    }
	}
	
	dot.append("}");
	return dot.toString();
    }

    String stripColon(String input) {
	return input.replace(':','t');
    }

    public void addTerm(String geneOntologyID)
	throws DataLoadFailedException,
	       GoTermUnavailableException {
	// Pre-load the ancestors for this term
	gtf.query(null,null,new SimpleQuery(GoTermFactory.QUERY_ANCESTRY,geneOntologyID),GoTermFactory.LOAD_MASK_ALL);
	// Populate the map
	doTerm(gtf.fetchTerm(geneOntologyID));
	// Increment the selected term's hit count by one
	TermContainer tc = (TermContainer)terms.get(geneOntologyID);
	tc.hitCount++;
    }

    // Recursively store the term and all its ancestors
    void doTerm(GoTerm g) throws DataLoadFailedException {
	storeTerm(g);
	GoRelation[] r = g.getParentRelations();
	for (int i = 0; i < r.length; i++) {
	    GoRelation relation = r[i];
	    doTerm(relation.getParent());
	}
    }

    void storeTerm(GoTerm g) {
	// Did we already have it?
	TermContainer tc = (TermContainer)terms.get(g.getAccession());
	if (tc != null) {
	    return;
	}
	tc = new TermContainer(g,0);
	// Store the term container in the term map
	this.terms.put(g.getAccession(),tc);
    }


}

class TermContainer {
    
    GoTerm theTerm;
    int hitCount = 0;

    public TermContainer(GoTerm theTerm, int hitCount) {
	this.theTerm = theTerm;
	this.hitCount = hitCount;
    }

}
