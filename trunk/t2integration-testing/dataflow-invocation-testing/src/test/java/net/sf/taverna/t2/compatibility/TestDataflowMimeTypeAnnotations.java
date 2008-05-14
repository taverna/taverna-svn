package net.sf.taverna.t2.compatibility;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.junit.Test;

import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationAssertionImpl;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.AnnotationChainImpl;
import net.sf.taverna.t2.annotation.annotationbeans.MimeType;
import net.sf.taverna.t2.testing.InvocationTestHelper;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;

/**
 * Assign a mime type to an output port of a dataflow and then retrieve it back
 * later
 * 
 * @author Ian Dunlop
 * 
 */
public class TestDataflowMimeTypeAnnotations extends InvocationTestHelper {

	@Test
	public void createDataflow() throws UnknownProcessorException,
			UnknownPortException, ProcessorCreationException,
			DataConstraintCreationException, DuplicateProcessorNameException,
			MalformedNameException, ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException,
			IOException, WorkflowTranslationException {
		Edits edits = EditsRegistry.getEdits();
		//create the dataflow (using the translator for the moment)
		Dataflow dataflow = translateScuflFile("simple_workflow_with_input.xml");
		//create the mime type text/plain
		MimeType mimeType = new MimeType();
		mimeType.setText("text/plain");
		//create an annotation assertion and add the mime type to it
		AnnotationAssertion annotationAssertionImpl = new AnnotationAssertionImpl();
		Edit<AnnotationAssertion> addAnnotationBean = edits
				.getAddAnnotationBean(annotationAssertionImpl, mimeType);

		try {
			addAnnotationBean.doEdit();
		} catch (EditException e2) {
			e2.printStackTrace();
		}
		//create an annotation chain and add the assertion to it
		AnnotationChain annotationChain = new AnnotationChainImpl();

		Edit<AnnotationChain> addAnnotationAssertionEdit = edits
				.getAddAnnotationAssertionEdit(annotationChain,
						annotationAssertionImpl);
		try {
			addAnnotationAssertionEdit.doEdit();
		} catch (EditException e1) {
			e1.printStackTrace();
		}
		
		//add the annotation chain to the output port
		for (DataflowOutputPort port : dataflow.getOutputPorts()) {
			try {
				port.getAddAnnotationEdit(annotationChain).doEdit();
			} catch (EditException e) {
				e.printStackTrace();
			}
		}
		//retrieve the mime type annotation from the output port
		for (DataflowOutputPort port : dataflow.getOutputPorts()) {
			Set<? extends AnnotationChain> newAnnotationChain = port
					.getAnnotations();
			for (AnnotationChain chain : newAnnotationChain) {
				List<AnnotationAssertion<?>> assertions = chain.getAssertions();
				for (AnnotationAssertion assertion : assertions) {
					if (assertion instanceof MimeType) {
						String text = ((MimeType) assertion).getText();
						assertEquals("Was not text/plain", text
								.equalsIgnoreCase("text/plain"));
					}
				}

			}
		}

	}

}
