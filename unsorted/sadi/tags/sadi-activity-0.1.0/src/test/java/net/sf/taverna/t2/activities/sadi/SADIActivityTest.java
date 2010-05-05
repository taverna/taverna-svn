/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

import org.apache.avalon.framework.activity.Suspendable;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ca.wilkinsonlab.sadi.utils.OwlUtils;

import com.hp.hpl.jena.ontology.AllDifferent;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Unit tests for SADIActivity.
 * 
 * @author David Withers
 * 
 */
public class SADIActivityTest {

	private SADIActivity activity;

	private SADIActivityConfigurationBean configurationBean;

	@Before
	public void setUp() throws Exception {
		activity = new SADIActivity();
		configurationBean = new SADIActivityConfigurationBean();
		// for live tests - move to integration tests
		configurationBean.setSparqlEndpoint("http://biordf.net/sparql");
		configurationBean.setGraphName("http://sadiframework.org/registry/");
		configurationBean.setServiceURI("http://sadiframework.org/examples/uniprotInfo");
	}

	@Test
	public void testExampleActivity() {
		assertNotNull(new SADIActivity());
	}

	@Test
	public void testConfigure() throws Exception {
		activity.configure(configurationBean);
	}

	@Test
	@Ignore
	public void testExecuteAsynch() throws Exception {
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("UniProt_Record", Arrays.asList("P68871", "Q7Z591"));
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
//		expectedOutputs.put("UniProt_Record", String.class);
		expectedOutputs.put("hasName", Statement.class);
		expectedOutputs.put("belongsToOrganism", String.class);

		activity.configure(configurationBean);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs,
				expectedOutputs);

		assertTrue(outputs.containsKey("hasName"));
		Object output = outputs.get("hasName");
		assertTrue(output instanceof List<?>);
		for (Object x : (List<?>) output) {
			System.out.println(x);
		}
		assertEquals(4, ((List<?>) output).size());
		assertTrue(((List<?>) output).contains("Hemoglobin subunit beta"));
		assertTrue(((List<?>) output).contains("Hemoglobin beta chain"));
		assertTrue(((List<?>) output).contains("Beta-globin"));
		assertTrue(((List<?>) output).contains("AT-hook-containing transcription factor"));

		assertTrue(outputs.containsKey("belongsToOrganism"));
		output = outputs.get("belongsToOrganism");
		assertTrue(output instanceof List<?>);
		assertEquals(2, ((List<?>) output).size());
		assertTrue(((List<?>) output).contains("Homo sapiens"));
		assertTrue(((List<?>) output).contains("Homo sapiens"));
	}

	@Test
	@Ignore
	public void testConfigureExampleActivityConfigurationBean() throws Exception {
		Set<String> expectedInputs = new HashSet<String>();
		expectedInputs.add("input");
		Set<String> expectedOutputs = new HashSet<String>();
		expectedOutputs.add("output");

		activity.configure(configurationBean);

		Set<ActivityInputPort> inputPorts = activity.getInputPorts();
		assertEquals(expectedInputs.size(), inputPorts.size());
		for (ActivityInputPort inputPort : inputPorts) {
			assertTrue("Wrong output : " + inputPort.getName(), expectedInputs.remove(inputPort
					.getName()));
		}

		Set<OutputPort> outputPorts = activity.getOutputPorts();
		assertEquals(expectedOutputs.size(), outputPorts.size());
		for (OutputPort outputPort : outputPorts) {
			assertTrue("Wrong output : " + outputPort.getName(), expectedOutputs.remove(outputPort
					.getName()));
		}
	}
	
	@Test
	@Ignore
	public void testComplexInput() throws Exception {
		configurationBean.setServiceURI("http://sadiframework.org/examples/linear");
//		configurationBean.setServiceURI("http://sadiframework.org/examples/ermineJgo");
//		configurationBean.setServiceURI("http://sadiframework.org/services/getGOTerm");
		activity.configure(configurationBean);

		System.out.println("Super classes : ");
		for (OntClass superClass : activity.getService().getInputClass().listSuperClasses()
				.toList()) {
			System.out.println(" " + superClass);
		}
		
		System.out.println("Input Restrictions: ");
		printRestrictions(activity.getService().getInputClass(), "");

		System.out.println("Output Restrictions: ");
		printRestrictions(activity.getService().getOutputClass(), "");
		
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(activity.getService().getInputClass());
		SADIUtils.buildTree(activity.getService().getInputClass(), new HashSet<OntClass>(), node);

//		System.out.println("RestrictedProperties : ");
//		for (Property x: OwlUtils.listRestrictedProperties(activity.getService().getInputClass())) {
//			System.out.println(x);
//		}
//
//		System.out.println(activity.getService().getInputClass().getLocalName());
//		printHierarchy(activity.getService().getInputClass(), "  ", new HashSet<OntClass>(), true);
//
//		
//		System.out.println(activity.getService().getOutputClass().getLocalName());
//		printHierarchy(activity.getService().getOutputClass(), "  ", new HashSet<OntClass>(), false);
	}

	private static final Resource nothing = ResourceFactory
			.createResource("http://www.w3.org/2002/07/owl#Nothing");

	private void printHierarchy(OntClass ontClass, String indent, Set<OntClass> seenClasses, boolean input) {
		if (!seenClasses.contains(ontClass)) {
			seenClasses.add(ontClass);	
			if (ontClass.isRestriction()) {
				Restriction restriction = ontClass.asRestriction();
				System.out.println(indent + "Restriction on : " + restriction.getOnProperty().getLocalName());
				OntClass restrictionClass = OwlUtils.getValuesFromAsClass(restriction);
				System.out.println(indent + "Restriction class : " + restrictionClass);
				printHierarchy(restrictionClass, indent + "  ", seenClasses, input);
			} else if (ontClass.isDatatypeProperty()) {
				DatatypeProperty datatypeProperty = ontClass.asDatatypeProperty();
				System.out.println(indent + "DatatypeProperty" + datatypeProperty);
			} else if (ontClass.isObjectProperty()) {
				ObjectProperty objectProperty = ontClass.asObjectProperty();
				System.out.println(indent + "ObjectProperty" + objectProperty);
			} else if (ontClass.isUnionClass()) {
				System.out.println(indent + "Union {");
				for (OntClass operand : ontClass.asUnionClass().listOperands().toList()) {
					if (!operand.isAnon()) {
						System.out.println(indent + "  " + operand.getLocalName());
					}
					printHierarchy(operand, indent + "  ", seenClasses, input);
				}
				System.out.println(indent + "}");
			} else if (ontClass.isIntersectionClass()) {
				System.out.println(indent + "Intersection { ");
				for (OntClass operand : ontClass.asIntersectionClass().listOperands().toList()) {
					if (!operand.isAnon()) {
						System.out.println(indent + "  " + operand.getLocalName());
					}
					printHierarchy(operand, indent + "  ", seenClasses, input);
				}
				System.out.println(indent + "}");
			} else if (ontClass.isComplementClass()) {
				System.out.println(indent + "Complement { ");
				for (OntClass operand : ontClass.asComplementClass().listOperands().toList()) {
					if (!operand.isAnon()) {
						System.out.println(indent + "  " + operand.getLocalName());
					}
					printHierarchy(operand, indent + "  ", seenClasses, input);
				}
				System.out.println(indent + "}");
			}
//			if (input) {
				printEquvalentClass(ontClass, indent, seenClasses, input);
//			} else {
//				printSubClass(ontClass, indent, seenClasses, input);
//			}
		}
	}

	private void printEquvalentClass(OntClass ontClass, String indent, Set<OntClass> seenClasses, boolean input) {
		for (OntClass equivalentClass : ontClass.listEquivalentClasses().toList()) {
			if (!seenClasses.contains(equivalentClass) && !equivalentClass.equals(nothing)) {
				if (!equivalentClass.isAnon()) {
					System.out.println(indent + "EquivalentClass : " + equivalentClass.getLocalName());
				}
				printHierarchy(equivalentClass, indent + "  ", seenClasses, input);
			}
		}
	}

	private void printSubClass(OntClass ontClass, String indent, Set<OntClass> seenClasses, boolean input) {
		for (OntClass subClass : ontClass.listSubClasses(false).toList()) {
			if (!seenClasses.contains(subClass)  && !subClass.equals(nothing)) {
				if (!subClass.isAnon()) {
					System.out.println(indent + "SubClass : " + subClass.getLocalName());
				}
				printHierarchy(subClass, indent + "  ", seenClasses, input);
			}
		}
	}
	
	private void printRestrictions(OntClass ontClass, String indent) {
		System.out.println(indent + ontClass.getLocalName());
		Set<Restriction> restrictions = OwlUtils.listRestrictions(ontClass);
		Map<OntProperty, Set<OntClass>> properties = new HashMap<OntProperty, Set<OntClass>>();
		for (Restriction restriction : restrictions) {
			OntProperty property = restriction.getOnProperty();
			if (properties.containsKey(property)) {
				Set<OntClass> classes = properties.get(property);
				OntClass valuesFromClass = OwlUtils.getValuesFromAsClass(restriction);
				boolean addClass = true;
				for(Iterator<OntClass> iterator = classes.iterator(); iterator.hasNext();) {
					OntClass restrictionClass = iterator.next();
					if (valuesFromClass.hasSubClass(restrictionClass)) {
						addClass = false;
						break;
					} else if (valuesFromClass.hasSuperClass(restrictionClass)) {
						iterator.remove();
					}
				}
				if (addClass) {
					classes.add(valuesFromClass);
				}
			} else {
				Set<OntClass> classes = new HashSet<OntClass>();
				classes.add(OwlUtils.getValuesFromAsClass(restriction));
				properties.put(property, classes);
			}
		}
		for (Entry<OntProperty, Set<OntClass>> entry : properties.entrySet()) {
			OntProperty property = entry.getKey();
			System.out.print(indent + "  " + property.getLocalName());
			for (OntProperty subProperty : property.listSubProperties().toSet()) {
				if (!subProperty.equals(property)) {
					System.out.print(" " + subProperty.getLocalName());
				}
			}
			System.out.println("");
			for (OntClass restrictionClass : entry.getValue()) {
				printRestrictions(restrictionClass, indent + "    ");
			}
		}
	}
		
}
