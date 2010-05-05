/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.sadi;

import java.util.Iterator;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import com.hp.hpl.jena.ontology.ComplementClass;
import com.hp.hpl.jena.ontology.ConversionException;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;

import ca.wilkinsonlab.sadi.utils.PatternSubstitution;

/**
 * 
 *
 * @author David Withers
 */
public class SADIUtils {

	private static final PatternSubstitution idPattern = new PatternSubstitution(
			".+([^:#/]+)[:#/](.+)", "$2");

	public static String uriToId(String uri) {
		if (idPattern.matches(uri)) {
			return idPattern.execute(uri);
		}
		return uri;
	}

	
	public static void buildTree(OntClass clazz, Set<OntClass> seenClasses, DefaultMutableTreeNode node) {
		if (seenClasses.contains(clazz) || clazz.equals( OWL.Thing )) {
			return;
		}
		seenClasses.add(clazz);

		if (clazz.isRestriction()) {
			Restriction restriction = clazz.asRestriction();
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(restriction);
			node.add(newNode);
		}

		if (clazz.isUnionClass()) {
			UnionClass unionClass = clazz.asUnionClass();
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(unionClass);
			node.add(newNode);
			for (Iterator<?> i = unionClass.listOperands(); i.hasNext();) {
				buildTree((OntClass)i.next(), seenClasses, newNode);
			}
		} else if ( clazz.isIntersectionClass() ) {
			IntersectionClass intersectionClass = clazz.asIntersectionClass();
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(intersectionClass);
			node.add(newNode);
			for (Iterator<?> i = intersectionClass.listOperands(); i.hasNext();) {
				buildTree((OntClass)i.next(), seenClasses, newNode);
			}
		} else if (clazz.isComplementClass()) {
			ComplementClass complementClass = clazz.asComplementClass();
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(complementClass);
			node.add(newNode);
			for (Iterator<?> i = complementClass.listOperands(); i.hasNext();) {
				buildTree((OntClass)i.next(), seenClasses, newNode);
			}
		}

		for (OntClass equivalentClass: clazz.listEquivalentClasses().toSet()) {
			buildTree(equivalentClass, seenClasses, node);
		}
		for (OntClass superClass: clazz.listSuperClasses().toSet()) {
			buildTree(superClass, seenClasses, node);
		}
	}

}
