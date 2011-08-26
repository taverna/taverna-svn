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

import java.util.*;

/**
 * Wraps a set of TermSet objects into a service, exposing
 * them via static methods suitable for export as a SOAP
 * interface style frontend.
 * @author Tom Oinn
 */
public class GoVizService {
    
    private static Map sessionMap = new HashMap();
    
    /**
     * Stateless method to get the ancestors of a specific term.
     */
    public static String[] getAncestors(String geneOntologyID) {
	try {
	    return TermSet.getAncestors(geneOntologyID);
	}
	catch (Exception e) {
	    RuntimeException re = new RuntimeException("Unable to fetch ancestors for "+geneOntologyID);
	    re.initCause(e);
	    throw re;
	}
    }

    /**
     * Stateless method to get the immediate parents of a specific term.
     */
    public static String[] getParents(String geneOntologyID) {
	try {
	    return TermSet.getParents(geneOntologyID);
	}
	catch (Exception e) {
	    RuntimeException re = new RuntimeException("Unable to fetch parents for "+geneOntologyID);
	    re.initCause(e);
	    throw re;
	}
    }
    
    /**
     * Stateless method to get the immediate children of a specific term.
     */
    public static String[] getChildren(String geneOntologyID) {
	try {
	    return TermSet.getChildren(geneOntologyID);
	}
	catch (Exception e) {
	    RuntimeException re = new RuntimeException("Unable to fetch children for "+geneOntologyID);
	    re.initCause(e);
	    throw re;
	}
    }

    /**
     * Create a new session, returning a session identifier.
     */
    public static String createSession() {
	try {
	    TermSet t = new TermSet();
	    String sessionID = ((Object)t).toString();
	    sessionMap.put(sessionID, t);
	    return sessionID;
	}
	catch (Exception e) {
	    RuntimeException re = new RuntimeException("Unable to create a new TermSet object!");
	    re.initCause(e);
	    throw re;
	}
    }
    
    /**
     * Remove all colour information from the given session
     */
    public static void clearColours(String sessionID) {
	try {
	    TermSet t = (TermSet)sessionMap.get(sessionID);
	    if (t == null) {
		throw new Exception("No such session ID in this service!");
	    }
	    else {
		t.clearColours();
	    }
	}
	catch (Exception e) {
	    RuntimeException re = new RuntimeException("Failed to clear colour information!");
	    re.initCause(e);
	    throw re;
	}
    }

    /**
     * Colour the specified term in the session with a particular colour value, if
     * the term does not exist then this method will do nothing.
     */
    public static void markTerm(String sessionID, String geneOntologyID, String colour) {
	try {
	    TermSet t = (TermSet)sessionMap.get(sessionID);
	    if (t == null) {
		throw new Exception("No such session ID in this service!");
	    }
	    else {
		t.markTerm(geneOntologyID, colour);
	    }
	}
	catch (Exception e) {
	    RuntimeException re = new RuntimeException("Failed to assign colour information!");
	    re.initCause(e);
	    throw re;
	}
    }

    /**
     * Destroy a session identified by session ID
     */
    public static void destroySession(String sessionID) {
	sessionMap.remove(sessionID);
    }

    /**
     * Given a session identifier, get the DOT text for the
     * current state of the session.
     */
    public static String getDot(String sessionID) {
	try {
	    TermSet t = (TermSet)sessionMap.get(sessionID);
	    if (t == null) {
		throw new Exception("No such session ID in this service!");
	    }
	    else {
		return t.toDot();
	    }
	}
	catch (Exception e) {
	    RuntimeException re = new RuntimeException("Failed to create DOT file!");
	    re.initCause(e);
	    throw re;
	}
    }
    
    /**
     * Add a term into the session
     */
    public static void addTerm(String sessionID, String geneOntologyID) {
	try {
	    TermSet t = (TermSet)sessionMap.get(sessionID);
	    if (t == null) {
		throw new Exception("No such session ID in this service!");
	    }
	    else {
		t.addTerm(geneOntologyID);
	    }
	}
	catch (Exception e) {
	    RuntimeException re = new RuntimeException("Failed to fetch GO term information!");
	    re.initCause(e);
	    throw re;
	}
    }

}
