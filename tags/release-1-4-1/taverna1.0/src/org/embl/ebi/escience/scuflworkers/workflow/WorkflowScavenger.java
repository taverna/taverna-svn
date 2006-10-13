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
import org.jdom.*;



/**
 * A Scavenger that knows how to load nested workflow scripts
 * @author Tom Oinn
 */
public class WorkflowScavenger extends Scavenger {

    /**
     * Create a new Workflow scavenger, the single parameter
     * should be resolvable to a location from which the 
     * definition could be fetched.
     */
    public WorkflowScavenger(String definitionURL)
	throws ScavengerCreationException {
	super(new WorkflowProcessorFactory(definitionURL));
	// Get a reference back to the processor factory
	ProcessorFactory rootPF = (ProcessorFactory)getUserObject();
	//WorkflowProcessorFactory wpf = new WorkflowProcessorFactory(definitionURL);
	//DefaultMutableTreeNode factoryNode = new DefaultMutableTreeNode(wpf);
	
	// Crafty addition of a load of the workflow in offline mode
	// followed by extraction of all the processors...
	ScuflModel model = new ScuflModel();
	try {
	    model.setOffline(true);
	    XScuflParser.populate(new URL(definitionURL).openStream(),
				  model, null);
	    rootPF.setDescription(model.getDescription().getText());
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}
	doChildren(model);
    }
    
    /**
     * Create a new workflow scavenger from a JDOM Document and a name,
     * the name is required because there's no way to infer this from
     * the document, it's a property of the storage medium.
     */
    public WorkflowScavenger(Document doc, String workflowName) 
	throws ScavengerCreationException {
	super(new WorkflowProcessorFactory(doc.getRootElement()));
	ProcessorFactory rootPF = (ProcessorFactory)getUserObject();
	rootPF.setName(workflowName);
	ScuflModel model = new ScuflModel();
	try {
	    model.setOffline(true);
	    XScuflParser.populate(doc,
				  model, null);
	    rootPF.setDescription(model.getDescription().getText());
	}
	catch (Exception ex) {
	    //ex.printStackTrace();
	    throw new ScavengerCreationException("Cannot create scavenger "+ex);
	}
	doChildren(model);
    }

    private void doChildren(ScuflModel model) {
	Processor[] processors = model.getProcessors();
	for (int i = 0; i < processors.length; i++) {
	    // Create a new node for the factory corresponding to this processor
	    String tagName = ProcessorHelper.getTagNameForClassName(processors[i].getClass().getName());
	    XMLHandler xh = (XMLHandler)ProcessorHelper.getXMLHandlerForTagName(tagName);
	    System.out.println(tagName+":"+processors[i].getName());
	    if (xh != null) {
		ProcessorFactory pf = xh.getFactory(xh.elementForProcessor(processors[i]));
		pf.setName(processors[i].getName());
		if (processors[i].getDescription().equals("") == false) {
		    pf.setDescription(processors[i].getDescription());
		}
		else {
		    if (processors[i] instanceof WorkflowProcessor) {
			// Nested workflow with no description, explicitly set it to say
			// this otherwise a 'fetch descriptions' will attempt to load it
			// in online mode, this could well fail.
			pf.setDescription("<font color=\"red\">No description supplied for nested workflow</font>");
		    }
		}
		/**
		   if (pf instanceof WorkflowProcessorFactory) {
		   ((WorkflowProcessorFactory)pf).setName(processors[i].getName());
		   }
		   else if (pf instanceof StringConstantProcessorFactory) {
		   ((StringConstantProcessorFactory)pf).setName(processors[i].getName());
		   }
		   else if (pf instanceof BeanshellProcessorFactory) {
		   ((BeanshellProcessorFactory)pf).setName(processors[i].getName());
		   }
		*/
		add(new DefaultMutableTreeNode(pf));
	    }
	}
    }
}
	
