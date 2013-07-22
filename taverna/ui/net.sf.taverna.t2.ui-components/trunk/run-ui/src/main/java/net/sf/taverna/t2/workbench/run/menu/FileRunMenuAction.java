/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package net.sf.taverna.t2.workbench.run.menu;

import java.net.URI;

import javax.swing.Action;

import uk.org.taverna.platform.run.api.RunService;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.run.actions.RunWorkflowAction;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.Workbench;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

public class FileRunMenuAction extends AbstractMenuAction {

	private static final URI FILE_RUN_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileRun");
	private EditManager editManager;
	private FileManager fileManager;
	private ReportManager reportManager;
	private Workbench workbench;
	private RunService runService;
	private SelectionManager selectionManager;
	private PerspectiveSPI resultsPerspective;

	public FileRunMenuAction() {
		super(FileRunMenuSection.FILE_RUN_SECTION_URI, 10, FILE_RUN_URI);
	}

	@Override
	protected Action createAction() {
		return new RunWorkflowAction(editManager, fileManager, reportManager, workbench, runService, selectionManager, resultsPerspective);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setReportManager(ReportManager reportManager) {
		this.reportManager = reportManager;
	}

	public void setWorkbench(Workbench workbench) {
		this.workbench = workbench;
	}

	public void setRunService(RunService runService) {
		this.runService = runService;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setResultsPerspective(PerspectiveSPI resultsPerspective) {
		this.resultsPerspective = resultsPerspective;
	}

}
