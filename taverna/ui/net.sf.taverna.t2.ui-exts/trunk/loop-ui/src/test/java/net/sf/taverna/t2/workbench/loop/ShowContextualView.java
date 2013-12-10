/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester
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
package net.sf.taverna.t2.workbench.loop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Processor;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.impl.EditManagerImpl;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.impl.FileManagerImpl;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.impl.SelectionManagerImpl;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.impl.ContextualViewComponent;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.impl.ContextualViewFactoryRegistryImpl;

/**
 * A standalone application to show contextual views
 * <p>
 * The application shows a JFrame containing a contextual view, together with
 * buttons which will select items in the {@link SelectionManager} for a
 * (rather) empty current dataflow.
 *
 * @author Stian Soiland-Reyes.
 *
 */
public class ShowContextualView {

	public static void main(String[] args) throws Exception {
		EditManager editManager = new EditManagerImpl();
		FileManager fileManager = new FileManagerImpl(editManager);
		ContextualViewFactoryRegistry contextualViewFactoryRegistry = new ContextualViewFactoryRegistryImpl();
		SelectionManagerImpl selectionMan = new SelectionManagerImpl();
		selectionMan.setFileManager(fileManager);
		selectionMan.setEditManager(editManager);
		new ShowContextualView(editManager, fileManager,selectionMan, contextualViewFactoryRegistry).showFrame();
	}

	private SelectionManager selectionManager;
	private FileManager fileManager;
	private EditManager editManager;
	private ContextualViewFactoryRegistry contextualViewFactoryRegistry;

	private uk.org.taverna.scufl2.api.core.Processor processor;

	private WorkflowBundle currentDataflow;

	public ShowContextualView(EditManager editManager, FileManager fileManager, final SelectionManager selectionManager, ContextualViewFactoryRegistry contextualViewFactoryRegistry) {
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.selectionManager = selectionManager;
		this.contextualViewFactoryRegistry = contextualViewFactoryRegistry;
		currentDataflow = fileManager.newDataflow();
		makeProcessor();

	}

	private void makeProcessor() {
	    processor = new Processor(currentDataflow.getMainWorkflow(), "Hello");
	}

	private List getSelections() {
		return Arrays.asList(processor, currentDataflow);
	}

	private Component makeSelectionButtons() {
		JPanel buttons = new JPanel();
		for (final Object selection : getSelections()) {
			buttons.add(new JButton(new AbstractAction("" + selection) {
				public void actionPerformed(ActionEvent e) {
					selectionManager.getDataflowSelectionModel(
							currentDataflow).setSelection(
							Collections.<Object> singleton(selection));
				}
			}));
		}
		return buttons;
	}

	protected void showFrame() {
		JFrame frame = new JFrame(getClass().getName());
		ContextualViewComponent contextualViewComponent = new ContextualViewComponent(editManager, selectionManager, contextualViewFactoryRegistry);
		frame.add(contextualViewComponent, BorderLayout.CENTER);

		frame.add(makeSelectionButtons(), BorderLayout.NORTH);
		frame.setSize(400, 400);
		frame.setVisible(true);
	}

}
