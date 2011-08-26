/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.workflow;

import javax.swing.tree.DefaultMutableTreeNode;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflworkers.stringconstant.*;
import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessorFactory;
import org.embl.ebi.escience.scuflworkers.beanshell.*;
import java.lang.String;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.parser.*;
import org.embl.ebi.escience.scuflworkers.*;
import java.net.URL;



/**
 * A Scavenger that knows how to load nested workflow scripts
 * @author Tom Oinn
 */
public class WorkflowScavenger extends Scavenger {

    /**
     * Create a new Talisman scavenger, the single parameter
     * should be resolvable to a location from which the 
     * tscript could be fetched.
     */
    public WorkflowScavenger(String definitionURL)
	throws ScavengerCreationException {
	super(new WorkflowProcessorFactory(definitionURL));
	
	//WorkflowProcessorFactory wpf = new WorkflowProcessorFactory(definitionURL);
	//DefaultMutableTreeNode factoryNode = new DefaultMutableTreeNode(wpf);
	
	// Crafty addition of a load of the workflow in offline mode
	// followed by extraction of all the processors...
	ScuflModel model = new ScuflModel();
	try {
	    model.setOffline(true);
	    XScuflParser.populate(new URL(definitionURL).openStream(),
				  model, null);
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}
	Processor[] processors = model.getProcessors();
	for (int i = 0; i < processors.length; i++) {
	    // Create a new node for the factory corresponding to this processor
	    String tagName = ProcessorHelper.getTagNameForClassName(processors[i].getClass().getName());
	    XMLHandler xh = (XMLHandler)ProcessorHelper.getXMLHandlerForTagName(tagName);
	    System.out.println(tagName+":"+processors[i].getName());
	    if (xh != null) {
		ProcessorFactory pf = xh.getFactory(xh.elementForProcessor(processors[i]));
		if (pf instanceof WorkflowProcessorFactory) {
		    ((WorkflowProcessorFactory)pf).setName(processors[i].getName());
		}
		else if (pf instanceof StringConstantProcessorFactory) {
		    ((StringConstantProcessorFactory)pf).setName(processors[i].getName());
		}
		else if (pf instanceof BeanshellProcessorFactory) {
		    ((BeanshellProcessorFactory)pf).setName(processors[i].getName());
		}
		add(new DefaultMutableTreeNode(pf));
	    }
	}
    }
}
	
