package net.sf.taverna.t2.activities.matlab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for MatAcitvity
 * @author user
 */
public class MatActivityTest
{

    private MatActivity activity;
    private MatActivityConfigurationBean configurationBean;

    @Before
    public void setUp() throws Exception
    {
        activity = new MatActivity();
        configurationBean = new MatActivityConfigurationBean();

        ActivityInputPortDefinitionBean inputPortBean = new ActivityInputPortDefinitionBean();
        inputPortBean.setDepth(0);
        inputPortBean.setName("example_input");
        inputPortBean.setHandledReferenceSchemes(new ArrayList<Class<? extends ReferenceScheme<?>>>());
        inputPortBean.setTranslatedElementType(String.class);
        inputPortBean.setAllowsLiteralValues(true);
        configurationBean.setInputPortDefinitions(Collections.singletonList(inputPortBean));

        ActivityOutputPortDefinitionBean outputPortBean = new ActivityOutputPortDefinitionBean();
        outputPortBean.setDepth(0);
        outputPortBean.setName("example_output");
        outputPortBean.setMimeTypes(new ArrayList<String>());
        configurationBean.setOutputPortDefinitions(Collections.singletonList(outputPortBean));
    }

    @Test
    public void testMatActivity()
    {
        assertNotNull(new MatActivity());
    }

    @Test
    @Ignore
    public void textExecuteAsynch() throws Exception
    {
        Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put("example_input", "hello");
        List<String> expectedOutputs = new ArrayList<String>();
        expectedOutputs.add("example_output");

        activity.configure(configurationBean);

        Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
        assertTrue(outputs.containsKey("example_output"));
        assertEquals("hello_example", outputs.get("example_output"));
    }

    @Test
    public void testConfigureMatActivityConfigurationBean() throws Exception
    {
        Set<String> expectedInputs = new HashSet<String>();
        expectedInputs.add("example_input");
        Set<String> expectedOutputs = new HashSet<String>();
        expectedOutputs.add("example_output");

        activity.configure(configurationBean);

        Set<ActivityInputPort> inputPorts = activity.getInputPorts();
        assertEquals(expectedInputs.size(), inputPorts.size());
        for (ActivityInputPort inputPort : inputPorts)
        {
            assertTrue("Wrong output : " + inputPort.getName(),
                    expectedInputs.remove(inputPort.getName()));

        }
        
        Set<OutputPort> outputPorts=activity.getOutputPorts();
        assertEquals(expectedOutputs.size(), outputPorts.size());
        for(OutputPort outputPort : outputPorts)
        {
            assertTrue("Wrong outptu: "+outputPort.getName(),
                    expectedOutputs.remove(outputPort.getName()));
        }
    }
}
