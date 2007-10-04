/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.view;

import java.util.Arrays;

import junit.framework.TestCase;

import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;

/**
 * Attempts to load data into a ScuflModel from the same source that the
 * XScuflParserTest uses, then to print out the dot representation of the model
 * to stdout.
 * 
 * @author Tom Oinn
 * @author Stian Soiland
 */
public class DotViewTest extends TestCase {

	ScuflModel model;
	DotView view;
	
	public void setUp() {
		model = new ScuflModel();
		view = new DotView(model);
	}
	
	void addInputPort(String name) throws DuplicatePortNameException, PortCreationException {
		model.getWorkflowSourceProcessor().addPort(
				new OutputPort(model.getWorkflowSourceProcessor(), name));
		model.forceUpdate();
	}
	
	void addOutputPort(String name) throws DuplicatePortNameException, PortCreationException {
		model.getWorkflowSinkProcessor().addPort(
				new InputPort(model.getWorkflowSinkProcessor(), name));
		model.forceUpdate();
	}
	
	void linkPorts(String from, String to) throws UnknownPortException, DataConstraintCreationException {
		Port fromPort = model.getWorkflowSourceProcessor().locatePort(from);
		Port toPort = model.getWorkflowSinkProcessor().locatePort(to);
		model.addDataConstraint(new DataConstraint(model, fromPort, toPort));
	}
	
	
	public void testSimpleGraph() throws ScuflException {
		addInputPort("input4");
		addOutputPort("output5");
		String dot = view.getDot();
		//System.out.println(dot);
		assertTrue(dot.startsWith("digraph scufl_graph {"));
		assertTrue(dot.contains("\"Workflow Inputs\""));
		assertTrue(dot.contains("label=\"input4\""));
		assertTrue(dot.contains("label=\"output5\""));
		assertTrue(dot.contains("\"Workflow Outputs\""));
		String LINK = "\"WORKFLOWINTERNALSOURCE_input4\"->\"WORKFLOWINTERNALSINK_output5\"";
		assertFalse(dot.contains(LINK));
		assertFalse(dot.contains("text/plain"));
		// But if we add the link, we should also get it in the DOT
		linkPorts("input4", "output5");
		assertTrue(view.getDot().contains(LINK));
	}

	public void testBoring() throws ScuflException { 
		addInputPort("input7");
		addOutputPort("output8");
		linkPorts("input7", "output8");
		model.getWorkflowSourceProcessor().setBoring(true);
		String LINK = "\"WORKFLOWINTERNALSOURCE_input7\"->\"WORKFLOWINTERNALSINK_output8\"";
		assertTrue(view.getDot().contains(LINK));
		view.setBoring(false);
		// Should no longer show links from WORKFLOWINTERNALSOURCE
		assertFalse(view.getDot().contains(LINK));
		// Unless it is not boring anymore!
		model.getWorkflowSourceProcessor().setBoring(false);
		assertTrue(view.getDot().contains(LINK));
		
	}
	public void testGetViewSettings() {
		DotViewSettings settings = view.getViewSettings();
		isSame(settings);
		// Invert all in view
		view.setPortDisplay(24689);
		view.setAlignment(! view.getAlignment());
		view.setBoring(! view.getShowBoring());
		view.setExpandWorkflow(! view.getExpandWorkflow());
		view.setTypeLabelDisplay(! view.getTypeLabelDisplay());
		view.setFillColours(new String[] {"something", "else"});
		
		// Should not be updated
		isNotSame(settings);
		settings = view.getViewSettings();
		isSame(settings);
	}

	void isNotSame(DotViewSettings settings) {
		assertNotSame(settings.getShowBoring(), view.getShowBoring());
		assertNotSame(settings.getAlignment(), view.getAlignment());
		assertNotSame(settings.getExpandWorkflow(), view.getExpandWorkflow());
		assertNotSame(settings.getTypeLabelDisplay(), view.getTypeLabelDisplay());
		assertFalse(settings.getPortDisplay() == view.getPortDisplay());
		assertFalse(Arrays.equals(settings.getFillColours().toArray(), 
				view.getFillColours()));
	}
	
	void isSame(DotViewSettings settings) {
		assertSame(settings.getShowBoring(), view.getShowBoring());
		assertSame(settings.getAlignment(), view.getAlignment());
		assertSame(settings.getExpandWorkflow(), view.getExpandWorkflow());
		assertSame(settings.getTypeLabelDisplay(), view.getTypeLabelDisplay());
		assertEquals(settings.getPortDisplay(), view.getPortDisplay());
		assertTrue(Arrays.equals(settings.getFillColours().toArray(), view.getFillColours()));
		// Should be copy!
		assertNotSame(settings.getFillColours(), view.getFillColours());
	}
	
	
	public void testSetViewSettings() {
		DotViewSettings settings = view.getViewSettings();
		// Invert all settings
		settings.setPortDisplay(-1337);
		settings.setAlignment(! settings.getAlignment());
		settings.setShowBoring(! settings.getShowBoring());
		settings.setExpandWorkflow(! settings.getExpandWorkflow());
		settings.setTypeLabelDisplay(! settings.getTypeLabelDisplay());
		settings.getFillColours().set(0, "messed_up");
		isNotSame(settings);
		view.setViewSettings(settings);
		isSame(settings);
	}

}
