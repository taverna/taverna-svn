/**
 * 
 */
package uk.org.taverna.scufl2.validation.correctness;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import uk.org.taverna.scufl2.api.common.NamedSet;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;
import uk.org.taverna.scufl2.validation.correctness.ReportCorrectnessValidationListener.NullFieldProblem;

/**
 * @author alanrw
 *
 */
public class TestPorted {
	
	@Test
	public void testCorrectnessOfMissingInputPorts() {
		DummyWorkflow dw = new DummyWorkflow();
		dw.setOutputPorts(new NamedSet<OutputWorkflowPort>());
		
		CorrectnessValidator cv = new CorrectnessValidator();
		ReportCorrectnessValidationListener rcvl = new ReportCorrectnessValidationListener();
		
		cv.checkCorrectness(dw, false, rcvl);
		
		Set<NullFieldProblem> nullFieldProblems = rcvl.getNullFieldProblems();
		assertEquals(Collections.EMPTY_SET, nullFieldProblems); // only done when completeness check
	}
	
	@Test
	public void testCompletenessOfMissingInputPorts() {
		DummyWorkflow dw = new DummyWorkflow();
		dw.setOutputPorts(new NamedSet<OutputWorkflowPort>());
		
		CorrectnessValidator cv = new CorrectnessValidator();
		ReportCorrectnessValidationListener rcvl = new ReportCorrectnessValidationListener();
		
		cv.checkCorrectness(dw, true, rcvl);
		
		Set<NullFieldProblem> nullFieldProblems = rcvl.getNullFieldProblems();
		assertFalse(nullFieldProblems.isEmpty());
		boolean problem = false;
		for (NullFieldProblem nlp : nullFieldProblems) {
			if (nlp.getBean().equals(dw) && nlp.getFieldName().equals("inputPorts")) {
				problem = true;
			}
		}
		assertTrue(problem);

	}
	
	@Test
	public void testCompletenessOfSpecifiedInputPorts() {
		DummyWorkflow dw = new DummyWorkflow();
		dw.setInputPorts(new NamedSet<InputWorkflowPort>());
		dw.setOutputPorts(new NamedSet<OutputWorkflowPort>());
		
		CorrectnessValidator cv = new CorrectnessValidator();
		ReportCorrectnessValidationListener rcvl = new ReportCorrectnessValidationListener();
		
		cv.checkCorrectness(dw, true, rcvl);
		
		Set<NullFieldProblem> nullFieldProblems = rcvl.getNullFieldProblems();
		boolean problem = false;
		for (NullFieldProblem nlp : nullFieldProblems) {
			if (nlp.getBean().equals(dw) && nlp.getFieldName().equals("inputPorts")) {
				problem = true;
			}
		}
		assertFalse(problem);
		
	}
	
	@Test
	public void testCorrectnessOfMissingOutputPorts() {
		DummyWorkflow dw = new DummyWorkflow();
		dw.setInputPorts(new NamedSet<InputWorkflowPort>());
		
		CorrectnessValidator cv = new CorrectnessValidator();
		ReportCorrectnessValidationListener rcvl = new ReportCorrectnessValidationListener();
		
		cv.checkCorrectness(dw, false, rcvl);
		
		Set<NullFieldProblem> nullFieldProblems = rcvl.getNullFieldProblems();
		assertEquals(Collections.EMPTY_SET, nullFieldProblems); // only done when completeness check
	}
	
	@Test
	public void testCompletenessOfMissingOutputPorts() {
		DummyWorkflow dw = new DummyWorkflow();
		dw.setInputPorts(new NamedSet<InputWorkflowPort>());
		
		CorrectnessValidator cv = new CorrectnessValidator();
		ReportCorrectnessValidationListener rcvl = new ReportCorrectnessValidationListener();
		
		cv.checkCorrectness(dw, true, rcvl);
		
		Set<NullFieldProblem> nullFieldProblems = rcvl.getNullFieldProblems();
		assertFalse(nullFieldProblems.isEmpty());
		boolean problem = false;
		for (NullFieldProblem nlp : nullFieldProblems) {
			if (nlp.getBean().equals(dw) && nlp.getFieldName().equals("outputPorts")) {
				problem = true;
			}
		}
		assertTrue(problem);

	}
	
	@Test
	public void testCompletenessOfSpecifiedOutputPorts() {
		DummyWorkflow dw = new DummyWorkflow();
		dw.setInputPorts(new NamedSet<InputWorkflowPort>());
		dw.setOutputPorts(new NamedSet<OutputWorkflowPort>());
		
		CorrectnessValidator cv = new CorrectnessValidator();
		ReportCorrectnessValidationListener rcvl = new ReportCorrectnessValidationListener();
		
		cv.checkCorrectness(dw, true, rcvl);
		
		Set<NullFieldProblem> nullFieldProblems = rcvl.getNullFieldProblems();
		boolean problem = false;
		for (NullFieldProblem nlp : nullFieldProblems) {
			if (nlp.getBean().equals(dw) && nlp.getFieldName().equals("outputPorts")) {
				problem = true;
			}
		}
		assertFalse(problem);
		
	}

}
