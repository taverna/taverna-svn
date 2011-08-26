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
import java.util.*;

import java.lang.ClassLoader;
import java.lang.String;
import java.lang.StringBuffer;



/**
 * A dictionary of GO term containers used to store the
 * information to render to DOT as well as providing the
 * various static operations to get parents and children
 * of particular GO term nodes.
 * @author Tom Oinn
 */
public class TermSet {
    
    private HashMap terms;
    private static FactoryBag fb;
    private static GoTermFactory gtf;

    static {
	try {
	    fb = new DefaultFactoryBag();
	    ClassLoader cl = TermSet.class.getClassLoader();
	    DefaultFactoryBag.configure(cl.getResource("uk/ac/ebi/ego/examples/sample-config.xml"),fb);
	    gtf = (GoTermFactory)fb.getFactory(uk.ac.ebi.ego.GoTerm.class.getName());
	}
	catch (Exception e) {
	    e.printStackTrace();
	    RuntimeException re = new RuntimeException("Unable to initialize ingo");
	    re.initCause(e);
	    throw re;
	}
    }
    
    /**
     * Create a new TermSet, the first call to this will fail
     * if the static block throws any exceptions, this indicates
     * a probably factory misconfiguration.
     */
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
	    // If there is a colour defined for this term then use it otherwise
	    // use the default hit based colouring.
	    if (tc.colour != null) {
		dot.append("  fillcolor = \""+tc.colour+"\"\n");
	    }
	    else {
		if (tc.hitCount>0) {
		    dot.append("  fillcolor = \"mediumpurple1\"\n");
		}
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
    private String stripColon(String input) {
	return input.replace(':','t');
    }
    

    /** Add a term to the current state of this object. */
    public void addTerm(String geneOntologyID)
	throws DataLoadFailedException,
	       GoTermUnavailableException {
	// Pre-load the ancestors for this term
	gtf.query(null,null,new SimpleQuery(GoTermFactory.QUERY_ANCESTRY,geneOntologyID),GoTermFactory.LOAD_MASK_ALL);
	// Populate the map
	doAddTerm(gtf.fetchTerm(geneOntologyID));
	// Increment the selected term's hit count by one
	TermContainer tc = (TermContainer)terms.get(geneOntologyID);
	tc.hitCount++;
    }
    // Recursively store the term and all its ancestors
    private void doAddTerm(GoTerm g) throws DataLoadFailedException {
	storeTerm(g);
	GoRelation[] r = g.getParentRelations();
	for (int i = 0; i < r.length; i++) {
	    GoRelation relation = r[i];
	    doAddTerm(relation.getParent());
	}
    }
    // Add a term to the current state
    private void storeTerm(GoTerm g) {
	// Did we already have it?
	TermContainer tc = (TermContainer)terms.get(g.getAccession());
	if (tc != null) {
	    return;
	}
	tc = new TermContainer(g,0);
	// Store the term container in the term map
	this.terms.put(g.getAccession(),tc);
    }


    /** Mark the specified GO term in this state with the specified
	colour, does nothing if the term hasn't been added yet. */
    public void markTerm(String geneOntologyID, String colour) {
	TermContainer tc = (TermContainer)terms.get(geneOntologyID);
	if (tc != null) {
	    tc.colour = colour;
	}
    }


    /** Clear all colours */
    public void clearColours() {
	for (Iterator i = terms.entrySet().iterator(); i.hasNext(); ) {
	    TermContainer tc = (TermContainer)i.next();
	    tc.colour = null;
	}
    }


    /** Fetch all ancestors of the supplied term (STATIC) */
    public static String[] getAncestors(String geneOntologyID)
	throws DataLoadFailedException,
	       GoTermUnavailableException {
	// Pre-load the ancestors to optimise access time (if this does anything!)
	gtf.query(null, null, new SimpleQuery(GoTermFactory.QUERY_ANCESTRY, geneOntologyID), GoTermFactory.LOAD_MASK_ALL);
	Set termSet = new HashSet();
	addTermsToSet(termSet, gtf.fetchTerm(geneOntologyID));
	return (String[])termSet.toArray(new String[0]);
    }
    private static void addTermsToSet(Set s, GoTerm g) 
	throws DataLoadFailedException,
	       GoTermUnavailableException {
	s.add(g.getAccession());
	GoRelation[] r = g.getParentRelations();
	for (int i = 0; i < r.length; i++) {
	    addTermsToSet(s, r[i].getParent());
	}
    }


    /** Fetch the immediate child ids of the supplied term (STATIC) */
    public static String[] getChildren(String geneOntologyID)
	throws DataLoadFailedException,
	       GoTermUnavailableException {
	GoRelation[] r = gtf.fetchTerm(geneOntologyID).getChildRelations();
	List childIDList = new ArrayList();
	for (int i = 0; i < r.length; i++) {
	    childIDList.add(r[i].getChild().getAccession());
	}
	return (String[])childIDList.toArray(new String[0]);
    }


    /** Fetch the immediate parent ids of the supplied term (STATIC) */
    public static String[] getParents(String geneOntologyID)
	throws DataLoadFailedException,
	       GoTermUnavailableException {
	GoRelation[] r = gtf.fetchTerm(geneOntologyID).getParentRelations();
	List parentIDList = new ArrayList();
	for (int i = 0; i < r.length; i++) {
	    parentIDList.add(r[i].getParent().getAccession());
	}
	return (String[])parentIDList.toArray(new String[0]);
    }
}

/**
 * A simple container class to record the number of hits each term
 * has within the state of this object
 * @author Tom Oinn
 */
class TermContainer {
    
    GoTerm theTerm;
    int hitCount = 0;
    String colour = null;
    
    public TermContainer(GoTerm theTerm, int hitCount) {
	this.theTerm = theTerm;
	this.hitCount = hitCount;
    }

}
