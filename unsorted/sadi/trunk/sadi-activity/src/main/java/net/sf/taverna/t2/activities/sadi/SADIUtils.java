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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.wilkinsonlab.sadi.utils.LSRNUtils;
import ca.wilkinsonlab.sadi.utils.OwlUtils;
import ca.wilkinsonlab.sadi.utils.PatternSubstitution;
import ca.wilkinsonlab.sadi.utils.ResourceFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.ComplementClass;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * Utility methods for SADI activities.
 * 
 * @author David Withers
 */
public class SADIUtils {

	private static final Logger logger = Logger.getLogger(SADIUtils.class);

	private static final PatternSubstitution idPattern = new PatternSubstitution(
			".+([^:#/]+)[:#/](.+)", "$2");

	private static final String NEW_LINE = System.getProperty("line.separator");

	public static String uriToId(String uri) {
		if (idPattern.matches(uri)) {
			return idPattern.execute(uri);
		}
		return uri;
	}

	public static String printTree(RestrictionNode node) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent(node.getLevel()));
		sb.append((node.isExclusive() ? "-" : "+") + node.toString() + (node.isSelected() ? "" : "!"));
		sb.append(NEW_LINE);
		for (RestrictionNode child : node.getChildren()) {
			sb.append(printTree(child));
		}
		return sb.toString();
	}

	public static void printTreeValues(String id, RestrictionNode node) {
		System.out.print(indent(node.getLevel()));
		System.out.println(node.getOntClass().getLocalName());
		System.out.print(indent(node.getLevel()) + " ");
		System.out.println(node.getValues(id));
		for (RestrictionNode child : node.getChildren()) {
			printTreeValues(id, child);
		}
	}

	public static String indent(int size) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append("  ");
		}
		return sb.toString();
	}

	public static String toString(RDFNode node) {
		String string;
		if (node.isLiteral()) {
			string = ((Literal) node).getLexicalForm();
		} else {
			Model model = ResourceUtils.reachableClosure((Resource) node);
			StringWriter stringWriter = new StringWriter();
			model.write(stringWriter, "RDF/XML-ABBREV");
			string = stringWriter.toString();
		}
		return string;
	}

	public static int getMinimumDepth(List<List<String>> paths) {
		int result = paths.size() > 0 ? paths.get(0).size() : 1;
		for (List<String> list : paths) {
			result = Math.min(result, list.size() - 1);
		}
		return result;
	}

	/**
	 * Returns the paths to the leaf {@link RestrictionNode}s from the given
	 * RestrictionNode. If a RestrictionNode is exclusive the first sibling is
	 * chosen.
	 * 
	 * @param node
	 *            the root node of the restriction tree
	 * @return the paths to the leaf nodes from the given node
	 */
	public static List<List<String>> getDefaultRestrictionPaths(RestrictionNode node) {
		return getDefaultRestrictionPaths(node, new ArrayList<String>());
	}

	private static List<List<String>> getDefaultRestrictionPaths(RestrictionNode node,
			List<String> path) {
		List<List<String>> defaultRestrictionPaths = new ArrayList<List<String>>();
		path.add(node.toString());
		if (node.isLeaf()) {
			defaultRestrictionPaths.add(path);
		} else {
			for (RestrictionNode child : node.getChildren()) {
				if (child.isExclusive()) {
					defaultRestrictionPaths.addAll(getDefaultRestrictionPaths(child, path));
					break;
				} else {
					List<String> newPath = new ArrayList<String>();
					newPath.addAll(path);
					defaultRestrictionPaths.addAll(getDefaultRestrictionPaths(child, newPath));
				}
			}
		}
		return defaultRestrictionPaths;
	}

	/**
	 * Returns the {@link RestrictionNode} at the end of the specified path,
	 * using the specified RestrictionNode as the starting point. If the path
	 * does not specify a RestrictionNode in the tree <code>null</code> is returned.
	 * 
	 * @param node
	 *            the root node of the restriction tree
	 * @param path
	 *            the path to the required node
	 * @return the restriction at the end of the specified path
	 */
	public static RestrictionNode getRestriction(RestrictionNode node, List<String> path) {
		RestrictionNode restriction = null;
		if (node.toString().equals(path.get(0))) {
			if (path.size() == 1) {
				restriction = node;
			} else {
				for (RestrictionNode child : node.getChildren()) {
					restriction = getRestriction(child, path.subList(1, path.size()));
					if (restriction != null) {
						break;
					}
				}
			}
		}
		return restriction;
	}

	/**
	 * Builds a tree of {@link RestrictionNode}s from a SADI output class. The
	 * input class is required to remove the input restrictions from the
	 * resulting restriction tree.
	 * 
	 * @param inputClass
	 *            the input class of the SADI service
	 * @param outputClass
	 *            the output class of the SADI service
	 * @return a tree of {@link RestrictionNode}s
	 */
	public static RestrictionNode buildOutputRestrictionTree(OntClass inputClass,
			OntClass outputClass) {
		RestrictionNode node = new RestrictionNode(outputClass);
		buildRestrictionTree(outputClass, new HashSet<OntClass>(), node,
				new HashMap<OntProperty, RestrictionNode>(), OwlUtils.listRestrictions(inputClass),
				false);
		return node;
	}

	/**
	 * Builds a tree of {@link RestrictionNode}s from a SADI input class.
	 * 
	 * @param inputClass
	 *            the input class of the SADI service
	 * @return a tree of {@link RestrictionNode}s
	 */
	public static RestrictionNode buildInputRestrictionTree(OntClass inputClass) {
		RestrictionNode node = new RestrictionNode(inputClass);
		buildRestrictionTree(inputClass, new HashSet<OntClass>(), node,
				new HashMap<OntProperty, RestrictionNode>(), new HashSet<Restriction>(), false);
		return node;
	}

	private static void buildRestrictionTree(OntClass clazz, Set<OntClass> seenClasses,
			RestrictionNode node, Map<OntProperty, RestrictionNode> seenProperties,
			Set<Restriction> ignore, boolean exclusive) {
		if (seenClasses.contains(clazz) || clazz.equals(OWL.Thing) || LSRNUtils.isLSRNType(clazz)) {
			return;
		}
		seenClasses.add(clazz);

		if (clazz.isRestriction()) {
			Restriction restriction = clazz.asRestriction();
			if (!ignore.contains(restriction)) {
				OwlUtils.listRestrictions(clazz);
				OntProperty property = restriction.getOnProperty();
				OntClass restrictionClass = OwlUtils.getValuesFromAsClass(restriction);
				if (restrictionClass != null) {
					RestrictionNode restrictionNode = new RestrictionNode(property,
							restrictionClass, exclusive);
					// check if we've already seen a restriction on this
					// property
					if (seenProperties.containsKey(property)) {
						RestrictionNode existingRestrictionNode = seenProperties.get(property);
						OntClass existingRestrictionClass = existingRestrictionNode.getOntClass();
						if (existingRestrictionClass.hasSubClass(restrictionClass)) {
							node.remove(existingRestrictionNode);
							node.add(restrictionNode);
							seenProperties.put(property, restrictionNode);
						} else if (existingRestrictionClass.hasSuperClass(restrictionClass)) {
							// do nothing
						} else {
							node.add(restrictionNode);
							// TODO do we need a set of restrictions here?
							seenProperties.put(property, restrictionNode);
						}
					} else {
						node.add(restrictionNode);
						seenProperties.put(property, restrictionNode);
					}
					buildRestrictionTree(restrictionClass, seenClasses, restrictionNode,
							new HashMap<OntProperty, RestrictionNode>(), ignore, false);
				}
			}
		}

		if (clazz.isUnionClass()) {
			UnionClass unionClass = clazz.asUnionClass();
			for (Iterator<?> i = unionClass.listOperands(); i.hasNext();) {
				buildRestrictionTree((OntClass) i.next(), seenClasses, node, seenProperties,
						ignore, true);
			}
		} else if (clazz.isIntersectionClass()) {
			IntersectionClass intersectionClass = clazz.asIntersectionClass();
			for (Iterator<?> i = intersectionClass.listOperands(); i.hasNext();) {
				buildRestrictionTree((OntClass) i.next(), seenClasses, node, seenProperties,
						ignore, exclusive);
			}
		} else if (clazz.isComplementClass()) {
			ComplementClass complementClass = clazz.asComplementClass();
			for (Iterator<?> i = complementClass.listOperands(); i.hasNext();) {
				buildRestrictionTree((OntClass) i.next(), seenClasses, node, seenProperties,
						ignore, exclusive);
			}
		}

		for (OntClass equivalentClass : clazz.listEquivalentClasses().toSet()) {
			buildRestrictionTree(equivalentClass, seenClasses, node, seenProperties, ignore,
					exclusive);
		}
		for (OntClass superClass : clazz.listSuperClasses().toSet()) {
			buildRestrictionTree(superClass, seenClasses, node, seenProperties, ignore, exclusive);
		}
	}

	/**
	 * Returns a list of {@link Resource}s created from values attached to the
	 * restriction tree.
	 * 
	 * @param node
	 *            the root node of the restriction tree
	 * @param id
	 *            the URI used to create the root Resource
	 * @return a list of {@link Resource}s created from values attached to the
	 *         restriction tree
	 * @throws IOException
	 */
	public static List<Resource> getInputResources(RestrictionNode node, String id)
			throws IOException {
		List<Resource> inputResources = new ArrayList<Resource>();
		Model model = ModelFactory.createDefaultModel();
		OntClass ontClass = node.getOntClass();

		if (node.getValues(id) == null) {
			Resource individidual = model.createResource(id, ontClass);
			inputResources.add(individidual);
			for (RestrictionNode child : node.getChildren()) {
				if (child.isSelected()) {
					while (addInputResources(child, individidual, id));
				}
			}
		} else {
			for (Object value : node.getValues(id)) {
				if (value instanceof RDFNode) {
					RDFNode rdfNode = (RDFNode) value;
					if (rdfNode.isURIResource()) {
						inputResources.add(model.createResource(rdfNode.asNode().getURI(), ontClass));
					} else if (rdfNode.isResource()) {
//						inputResources.add((Resource) rdfNode.inModel(model));
						inputResources.add((Resource) rdfNode);
						model.add(ResourceUtils.reachableClosure((Resource) rdfNode));
					}
				} else if (value instanceof String) {
					String string = (String) value;
					if (isAbsoluteURI(string)) {
						inputResources.add(model.createResource(string, node.getOntClass()));
					} else {
						inputResources.add(ResourceFactory.createInstance(model, node.getOntClass(), string));
					}
				}
			}
		}
		if (logger.isDebugEnabled()) {
			StringWriter stringWriter = new StringWriter();
			model.write(stringWriter, "RDF/XML-ABBREV");
			logger.debug(stringWriter);
		}
		return inputResources;
	}

	private static boolean addInputResources(RestrictionNode node, Resource resource, String id)
			throws IOException {
		boolean result = false;
		if (node.getValues(id) == null && node.isSelected()) {
			Resource type = resource.getModel().createResource(node.getOntClass());
			resource.addProperty(node.getOntProperty(), type);
			for (RestrictionNode child : node.getChildren()) {
				if (child.isSelected()) {
					if (addInputResources(child, type, id)) {
						result = true;
					}
				}
			}
		} else {
			if (addProperties(resource, node, node.getValues(id))) {
				result = true;
			}
		}
		return result;
	}

	private static boolean addProperties(Resource resource, RestrictionNode restriction,
			List<?> values) throws IOException {
		if (values.size() > 0) {
			if (values.get(0) instanceof List<?>) {
				List<?> list = (List<?>) values.get(0);
				if (!addProperties(resource, restriction, list)) {
					values.remove(0);
				}
			} else {
				for (Object value : values) {
					if (value instanceof RDFNode) {
						RDFNode rdfNode = (RDFNode) value;
						resource.addProperty(restriction.getOntProperty(), rdfNode);
					} else if (value instanceof String) {
						String string = (String) value;
						if (restriction.getOntProperty().isDatatypeProperty() || restriction.getOntProperty().isProperty()) {
							Literal literal = resource.getModel().createTypedLiteral(string,
									restriction.getOntClass().getURI());
							resource.addProperty(restriction.getOntProperty(), literal);
						} else {
							if (isAbsoluteURI(string)) {
								Resource newResource = resource.getModel().createResource(string,
										restriction.getOntClass());
								resource.addProperty(restriction.getOntProperty(), newResource);
							} else {
								Resource newResource = ResourceFactory.createInstance(resource
										.getModel(), restriction.getOntClass(), string);
								resource.addProperty(restriction.getOntProperty(), newResource);
							}
						}
					} else {
						return false;
					}
				}
				values.clear();
			}
		}
		return values.size() > 0;
	}

	/**
	 * Attaches values, created from the output triples, to the restriction
	 * tree.
	 * 
	 * @param node
	 *            the root node of the restriction tree
	 * @param outputTriples
	 *            the triples output by the service
	 * @param inputResources
	 *            the input to the service
	 */
	public static void putOutputResources(RestrictionNode node, Collection<Triple> outputTriples,
			List<Resource> inputResources, String id) {
		Model model = ModelFactory.createDefaultModel();
		List<RDFNode> rdfNodes = new ArrayList<RDFNode>();
		int[] pos = new int[node.getDepth()];
		for (int i = 0; i < inputResources.size(); i++) {
			pos[node.getLevel()] = i;
			Node inputNode = inputResources.get(i).asNode();
//			Resource output = model.createResource(node.getOntClass());
			Resource output = model.createResource(inputNode.getURI(), node.getOntClass());
			rdfNodes.add(output);
			for (RestrictionNode child : node.getChildren()) {
				putOutputResources(child, output, inputNode, outputTriples,
						pos, id);
			}
		}
		node.setValues(id, rdfNodes);
		if (logger.isDebugEnabled()) {
			StringWriter stringWriter = new StringWriter();
			model.write(stringWriter, "RDF/XML-ABBREV");
			logger.debug(stringWriter);
		}
	}

	private static void putOutputResources(RestrictionNode node, Resource resource, Node subject,
			Collection<Triple> outputTriples, int[] pos, String id) {
		List<RDFNode> rdfNodes = new ArrayList<RDFNode>();
		if (node.isLeaf()) {
			List<Triple> triples = filterTriples(outputTriples, subject, node.getOntProperty()
					.asNode());
			for (Triple triple : triples) {
				if (triple.getObject().isLiteral()) {
					Literal literal = resource.getModel().createTypedLiteral(
							triple.getObject().getLiteralValue());
					resource.addLiteral(node.getOntProperty(), literal);
					rdfNodes.add(literal);
				} else if (triple.getObject().isURI()) {
					Resource newResource = resource.getModel().createResource(
							triple.getObject().getURI(), node.getOntClass());
					resource.addProperty(node.getOntProperty(), newResource);
					rdfNodes.add(newResource);
				}
			}
		} else {
			List<Triple> triples = filterTriples(outputTriples, subject, node.getOntProperty()
					.asNode());
			for (int i = 0; i < triples.size(); i++) {
				Resource type = resource.getModel().createResource(node.getOntClass());
				resource.addProperty(node.getOntProperty(), type);
				rdfNodes.add(type);
				for (RestrictionNode child : node.getChildren()) {
					pos[node.getLevel()] = i;
					putOutputResources(child, type, triples.get(i).getObject(), outputTriples, pos, id);
				}
			}
		}
		addValue(node, rdfNodes, pos, id);
	}

	@SuppressWarnings("unchecked")
	private static void addValue(RestrictionNode node, List<RDFNode> rdfNodes, int[] pos, String id) {
		int level = node.getLevel() - 1;
		List<Object> values = (List<Object>) node.getValues(id);
		if (values == null) {
			values = new ArrayList<Object>();
			node.setValues(id, values);
		}
		for (int i = 0; i < pos.length; i++) {
			if (i == level) {
				values.add(rdfNodes);
			} else if (i < level) {
				int index = pos[i];
				while (values.size() <= index) {
					values.add(new ArrayList<Object>());
				}
				values = (List<Object>) values.get(index);
			}
		}
	}

	/**
	 * Returns true if <code>id</code> is a URI and it is absolute.
	 * 
	 * @param id
	 *            the id to test
	 * @return true if <code>id</code> is a URI and it is absolute
	 */
	public static boolean isAbsoluteURI(String id) {
		try {
			return new URI(id).isAbsolute();
		} catch (URISyntaxException e) {
			return false;
		}
	}

	/**
	 * Returns the list of triples that match the give subject and predicate. If
	 * the predicate is null any triple with the same subject will be returned.
	 * 
	 * @param triples
	 *            the triples to filter
	 * @param subject
	 *            the subject to match
	 * @param predicate
	 *            the predicate to match
	 * @return the list of triples that match the give subject and predicate
	 */
	public static List<Triple> filterTriples(Collection<Triple> triples, Node subject,
			Node predicate) {
		List<Triple> resultTriples = new ArrayList<Triple>();
		for (Triple triple : triples) {
			if (triple.subjectMatches(subject)) {
				if (predicate == null || triple.predicateMatches(predicate)) {
					resultTriples.add(triple);
				}
			}
		}
		return resultTriples;
	}

}
