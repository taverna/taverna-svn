package net.sourceforge.taverna.scuflworkers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.implementation.WorkflowInstanceImpl;
import org.embl.ebi.escience.scufl.parser.XScuflParser;

import uk.ac.soton.itinnovation.freefluo.main.Engine;
import uk.ac.soton.itinnovation.freefluo.main.EngineImpl;

/**
 * This class is used to test workflows and their tasks 
 * without launching the gui.  You must first create the workflow
 * in Taverna and save it before instantiating and running this test class.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class WorkflowTestCase extends TestCase {
    
    private String workflowFile;

    public WorkflowTestCase(String workflowFile){
        this.workflowFile = workflowFile;
    }
    
    /**
     * This method runs the workflow.
     * @throws Exception
     */
    public void executeWorkflowTest() throws Exception{
        Engine engine = new EngineImpl();
        ScuflModel scuflModel = new ScuflModel();
        InputStream is = new FileInputStream(new File(this.workflowFile));
        XScuflParser.populate(is,scuflModel,"");
        WorkflowInstance workflow = new WorkflowInstanceImpl(engine,scuflModel,"1");
        
    }

}
