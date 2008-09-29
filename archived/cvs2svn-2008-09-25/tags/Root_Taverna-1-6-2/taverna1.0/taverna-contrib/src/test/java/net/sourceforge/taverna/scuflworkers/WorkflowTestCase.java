package net.sourceforge.taverna.scuflworkers;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;

import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateListener;
import uk.ac.soton.itinnovation.freefluo.main.Engine;
import uk.ac.soton.itinnovation.freefluo.main.EngineImpl;
import uk.ac.soton.itinnovation.freefluo.main.WorkflowState;
import uk.ac.soton.itinnovation.freefluo.util.clazz.ClassUtils;

/**
 * This class is used to test workflows and their tasks without launching the
 * gui. You must first create the workflow in Taverna and save it before
 * instantiating and running this test class.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.3 $
 */
public abstract class WorkflowTestCase extends TestCase {
	
	private static Logger logger = Logger.getLogger(WorkflowTestCase.class);

    private String workflowFile;
    private boolean allInputsRequired = false;

    /**
     * 
     * @param workflowFile
     * @param _allInputsRequired
     */
    public WorkflowTestCase(String workflowFile, boolean _allInputsRequired) {
        this.workflowFile = workflowFile;
        this.allInputsRequired = _allInputsRequired;
    }
    


    /**
     * This method runs the workflow.
     * 
     * @throws Exception
     */
    public void executeWorkflowTest() throws Exception {
        try {
            // prepare input data map
            DataThing lhsThing = DataThingFactory.bake("foo");
            DataThing rhsThing = DataThingFactory.bake("bar");
            HashMap input = new HashMap();
            input.put("lhs", lhsThing);
            input.put("rhs", rhsThing);
            // load workflow definition.
            String workflowDefinition = getFileAsString();

            Engine engine = new EngineImpl();
            String instanceId = engine.compile(workflowDefinition);
            engine.setInput(instanceId, input);
            engine.addWorkflowStateListener(instanceId,
                    new WorkflowStateListener() {
                        public void workflowStateChanged(
                                WorkflowStateChangedEvent event) {
                            WorkflowState state = event.getWorkflowState();
                            if (state.isFinal()) {
                                synchronized (lock) {
                                    lock.notify();
                                }
                            }
                        }
                    });

            synchronized (lock) {
                engine.run(instanceId);
                lock.wait();
            }

            logger.info("Workflow completed with state: " +

            engine.getStatus(instanceId));

            // retrieve all output data
            Map output = engine.getOutput(instanceId);

            // retrieve the output named 'out'.
            DataThing outDataThing = (DataThing) output.get("out");

            String outString = (String) outDataThing.getDataObject();
            logger.info("Output: " + outString);

            engine.destroy(instanceId);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static Object lock = new Object();

    private static String getFileAsString() throws Exception {
        return ClassUtils.loadResourceAsString("examples/concat.xml");
    }

}
