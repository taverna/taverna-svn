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

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.workbench.MainWindow;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.AddLayerFactorySPI;

import org.apache.log4j.Logger;

import uk.org.taverna.configuration.app.ApplicationConfiguration;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.core.Processor;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AddLoopFactory implements AddLayerFactorySPI {

    private static final URI LOOP_TYPE = URI.create("http://ns.taverna.org.uk/2010/scufl2/taverna/dispatchlayer/Loop");

    
    private static Logger logger = Logger.getLogger(AddLoopFactory.class);
    private static final JsonNodeFactory JSON_NODE_FACTORY = JsonNodeFactory.instance;
    private static Scufl2Tools scufl2Tools = new Scufl2Tools();
    
	private EditManager editManager;
	private FileManager fileManager;
	private SelectionManager selectionManager;
	private ApplicationConfiguration applicationConfig;

	public boolean canAddLayerFor(Processor processor) {
	   return findLoopLayer(processor) == null;
	}


    public ObjectNode findLoopLayer(Processor processor) {
        List<Configuration> configs = scufl2Tools.configurationsFor(processor, selectionManager.getSelectedProfile());
        for (Configuration config : configs) {
            if (config.getJson().has("loop")) {
                return (ObjectNode) config.getJson().get("loop");
            }
        }
        return null;
    }
	
	@SuppressWarnings("serial")
	public Action getAddLayerActionFor(final Processor processor) {
		return new AbstractAction("Add looping") {

            public void actionPerformed(ActionEvent e) {
				    ObjectNode loopLayer = findLoopLayer(processor);
				    if (loopLayer == null) {
				        loopLayer = JSON_NODE_FACTORY.objectNode();
				    }
					// Pop up the configure loop dialog
                LoopConfigureAction loopConfigureAction = new LoopConfigureAction(
                        MainWindow.getMainWindow(), null, processor, loopLayer,
                        selectionManager.getSelectedProfile(), editManager,
                        fileManager, getApplicationConfig());
					loopConfigureAction.actionPerformed(e);
			}
		};
	}

	@Override
	public boolean canCreateLayerClass(URI dispatchLayerType) {
	    return dispatchLayerType.equals(LOOP_TYPE);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public void setSelectionManager(SelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }


    public ApplicationConfiguration getApplicationConfig() {
        return applicationConfig;
    }


    public void setApplicationConfig(ApplicationConfiguration applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

}
