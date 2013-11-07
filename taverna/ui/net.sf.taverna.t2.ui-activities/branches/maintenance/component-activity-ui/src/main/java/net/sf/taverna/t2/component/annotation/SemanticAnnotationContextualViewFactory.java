/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.component.annotation;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPort;

/**
 * 
 * 
 * @author David Withers
 * 
 */
public class SemanticAnnotationContextualViewFactory implements
		ContextualViewFactory<Annotated<?>> {
	private static final FileManager fileManager = FileManager.getInstance();

	@Override
	public boolean canHandle(Object selection) {
		Object dataflowSource = fileManager.getDataflowSource(fileManager
				.getCurrentDataflow());
		return (dataflowSource instanceof ComponentVersionIdentification)
				&& (selection instanceof Annotated)
				&& !(selection instanceof Activity || selection instanceof ActivityPort);
	}

	@Override
	public List<ContextualView> getViews(Annotated<?> selection) {
		return Arrays.asList(new SemanticAnnotationContextualView(selection),
				new TurtleContextualView(selection));
	}
}
