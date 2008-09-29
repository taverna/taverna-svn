package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;

import org.junit.Before;
import org.junit.Test;

/**
 * @author David Withers
 * 
 */
public class AbstractDatalinkEditTest {

	private Datalink datalink;
	private boolean editDone;

	@Before
	public void setUp() throws Exception {
		datalink = new DatalinkImpl(null, null);
		editDone = false;
	}

	@Test
	public void testAbstractDatalinkEdit() {
		AbstractDatalinkEdit edit = new AbstractDatalinkEdit(datalink) {
			@Override
			protected void doEditAction(DatalinkImpl datalink)
					throws EditException {
			}

			@Override
			protected void undoEditAction(DatalinkImpl datalink) {
			}
		};
		assertEquals(datalink, edit.getSubject());
	}

	@Test(expected = RuntimeException.class)
	public void testAbstractDatalinkEditWithNull() {
		new AbstractDatalinkEdit(null) {
			@Override
			protected void doEditAction(DatalinkImpl datalink)
					throws EditException {
			}

			@Override
			protected void undoEditAction(DatalinkImpl datalink) {
			}
		};
	}

	@Test
	public void testDoEdit() throws EditException {
		AbstractDatalinkEdit edit = new AbstractDatalinkEdit(datalink) {
			@Override
			protected void doEditAction(DatalinkImpl datalink)
					throws EditException {
				editDone = true;
			}

			@Override
			protected void undoEditAction(DatalinkImpl datalink) {
			}
		};
		assertFalse(editDone);
		assertFalse(edit.isApplied());
		assertEquals(datalink, edit.doEdit());
		assertTrue(editDone);
		assertTrue(edit.isApplied());
	}

	@Test(expected = EditException.class)
	public void testDoEditTwice() throws EditException {
		AbstractDatalinkEdit edit = new AbstractDatalinkEdit(datalink) {
			@Override
			protected void doEditAction(DatalinkImpl datalink)
					throws EditException {
			}

			@Override
			protected void undoEditAction(DatalinkImpl datalink) {
			}
		};
		edit.doEdit();
		edit.doEdit();
	}

	@Test(expected = EditException.class)
	public void testDoEditWithWrongImpl() throws EditException {
		AbstractDatalinkEdit edit = new AbstractDatalinkEdit(new Datalink() {

			public int getResolvedDepth() {
				return 0;
			}

			public EventHandlingInputPort getSink() {
				return null;
			}

			public EventForwardingOutputPort getSource() {
				return null;
			}

			public Edit<? extends Datalink> getAddAnnotationEdit(
					AnnotationChain newAnnotation) {
				// TODO Auto-generated method stub
				return null;
			}

			public Set<? extends AnnotationChain> getAnnotations() {
				// TODO Auto-generated method stub
				return null;
			}

			public Edit<? extends Datalink> getRemoveAnnotationEdit(
					AnnotationChain annotationToRemove) {
				// TODO Auto-generated method stub
				return null;
			}

		}) {
			@Override
			protected void doEditAction(DatalinkImpl datalink)
					throws EditException {
			}

			@Override
			protected void undoEditAction(DatalinkImpl datalink) {
			}
		};
		edit.doEdit();
	}

	@Test
	public void testGetSubject() {
		AbstractDatalinkEdit edit = new AbstractDatalinkEdit(datalink) {
			@Override
			protected void doEditAction(DatalinkImpl datalink)
					throws EditException {
			}

			@Override
			protected void undoEditAction(DatalinkImpl datalink) {
			}
		};
		assertEquals(datalink, edit.getSubject());
	}

	@Test
	public void testIsApplied() throws EditException {
		AbstractDatalinkEdit edit = new AbstractDatalinkEdit(datalink) {
			@Override
			protected void doEditAction(DatalinkImpl datalink)
					throws EditException {
			}

			@Override
			protected void undoEditAction(DatalinkImpl datalink) {
			}
		};
		assertFalse(edit.isApplied());
		edit.doEdit();
		assertTrue(edit.isApplied());
		edit.undo();
		assertFalse(edit.isApplied());
	}

	@Test
	public void testUndo() throws EditException {
		AbstractDatalinkEdit edit = new AbstractDatalinkEdit(datalink) {
			@Override
			protected void doEditAction(DatalinkImpl datalink)
					throws EditException {
				editDone = true;
			}

			@Override
			protected void undoEditAction(DatalinkImpl datalink) {
				editDone = false;
			}
		};
		assertFalse(editDone);
		assertFalse(edit.isApplied());
		edit.doEdit();
		assertTrue(editDone);
		assertTrue(edit.isApplied());
		edit.undo();
		assertFalse(editDone);
		assertFalse(edit.isApplied());
	}

	@Test(expected = RuntimeException.class)
	public void testUndoBeforeDoEdit() {
		AbstractDatalinkEdit edit = new AbstractDatalinkEdit(datalink) {
			@Override
			protected void doEditAction(DatalinkImpl datalink)
					throws EditException {
			}

			@Override
			protected void undoEditAction(DatalinkImpl datalink) {
			}
		};
		edit.undo();
	}

}
