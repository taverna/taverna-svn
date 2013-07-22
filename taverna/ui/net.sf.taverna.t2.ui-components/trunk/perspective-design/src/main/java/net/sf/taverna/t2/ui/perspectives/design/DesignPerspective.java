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
package net.sf.taverna.t2.ui.perspectives.design;

import java.net.URI;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.fasterxml.jackson.databind.JsonNode;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.NamedSet;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.InputActivityPort;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;
import uk.org.taverna.scufl2.api.profiles.Profile;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.WorkflowPerspective;

public class DesignPerspective implements PerspectiveSPI, WorkflowPerspective {

	private static final URI NESTED_WORKFLOW_TYPE = URI
			.create("http://ns.taverna.org.uk/2010/activity/nested-workflow");

	private DesignPerspectiveComponent designPerspectiveComponent;

	private UIComponentFactorySPI graphViewComponentFactory;
	private UIComponentFactorySPI servicePanelComponentFactory;
	private UIComponentFactorySPI contextualViewComponentFactory;
	private UIComponentFactorySPI workflowExplorerFactory;
	private UIComponentFactorySPI reportViewComponentFactory;
	private FileManager fileManager;
	private SelectionManager selectionManager;
	private MenuManager menuManager;
	private EditManager editManager;

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	@Override
	public String getID() {
		return DesignPerspective.class.getName();
	}

	@Override
	public JComponent getPanel() {
		if (designPerspectiveComponent == null) {
			designPerspectiveComponent = new DesignPerspectiveComponent(graphViewComponentFactory,
					servicePanelComponentFactory, contextualViewComponentFactory,
					workflowExplorerFactory, reportViewComponentFactory, fileManager,
					selectionManager, menuManager, editManager);
		}
		return designPerspectiveComponent;
	}

	@Override
	public ImageIcon getButtonIcon() {
		return WorkbenchIcons.editIcon;
	}

	@Override
	public String getText() {
		return "Design";
	}

	@Override
	public int positionHint() {
		return 10;
	}

	public void setGraphViewComponentFactory(UIComponentFactorySPI graphViewComponentFactory) {
		this.graphViewComponentFactory = graphViewComponentFactory;
	}

	public void setServicePanelComponentFactory(UIComponentFactorySPI servicePanelComponentFactory) {
		this.servicePanelComponentFactory = servicePanelComponentFactory;
	}

	public void setContextualViewComponentFactory(
			UIComponentFactorySPI contextualViewComponentFactory) {
		this.contextualViewComponentFactory = contextualViewComponentFactory;
	}

	public void setWorkflowExplorerFactory(UIComponentFactorySPI workflowExplorerFactory) {
		this.workflowExplorerFactory = workflowExplorerFactory;
	}

	public void setReportViewComponentFactory(UIComponentFactorySPI reportViewComponentFactory) {
		this.reportViewComponentFactory = reportViewComponentFactory;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

}
