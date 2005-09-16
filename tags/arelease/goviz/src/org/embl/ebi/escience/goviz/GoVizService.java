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
	    t.addTerm(geneOntologyID);
	}
	catch (Exception e) {
	    RuntimeException re = new RuntimeException("Failed to create DOT file!");
	    re.initCause(e);
	    throw re;
	}
    }

}
