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


/**
 * Attempt to load a few terms and render the Dot file
 * @author Tom Oinn
 */
public class Test {

    public static void main(String[] args) throws Exception {
	TermSet ts = new TermSet();
	ts.addTerm("GO:0007601");
	ts.addTerm("GO:0000001");
	ts.addTerm("GO:0016021");
	ts.addTerm("GO:0042218");
	System.out.println(ts.toDot());
    }

}
