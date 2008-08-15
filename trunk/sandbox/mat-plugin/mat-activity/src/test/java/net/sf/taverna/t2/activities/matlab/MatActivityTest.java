package net.sf.taverna.t2.activities.matlab;

import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.sf.taverna.matlabactivity.matserver.api.MatArray;
import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.AbstractPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import org.junit.Test;

/**
 * Unit tests for MatAcitvity
 * @author petarj
 */
public class MatActivityTest {

    @Test
    public void simpleScript() throws ActivityConfigurationException, Exception {
        MatActivity activity = new MatActivity();
        MatActivityConfigurationBean bean = new MatActivityConfigurationBean();

        ActivityInputPortDefinitionBean inputPortBean = new ActivityInputPortDefinitionBean();
        inputPortBean.setDepth(0);
        inputPortBean.setName("input");
        inputPortBean.setMimeTypes(new ArrayList<String>());
        inputPortBean.setHandledReferenceSchemes(
                new ArrayList<Class<? extends ExternalReferenceSPI>>());
        inputPortBean.setTranslatedElementType(MatArray.class);
        inputPortBean.setAllowsLiteralValues(true);
        bean.setInputPortDefinitions(Collections.singletonList(inputPortBean));

        ActivityOutputPortDefinitionBean outputPortBean = new ActivityOutputPortDefinitionBean();
        outputPortBean.setDepth(0);
        outputPortBean.setName("output");
        outputPortBean.setMimeTypes(new ArrayList<String>());
        bean.setOutputPortDefinitions(Collections.singletonList(outputPortBean));
        bean.setSctipt("output=[input '_returned'];");

        activity.configure(bean);
        assertEquals("There sould be 1 input port", 1, activity.getInputPorts().
                size());
        assertEquals("There should be 1 output port", 1, activity.getOutputPorts().
                size());

        assertEquals("The input should be called input", "input", ((AbstractPort) activity.getInputPorts().
                toArray()[0]).getName());
        assertEquals("The output port should be called output", "output", ((AbstractPort) activity.getOutputPorts().
                toArray()[0]).getName());

        Map<String, Object> inputs = new HashMap<String, Object>();

        MatArray in = new MatArray();
        in.setType(MatArray.CHAR_TYPE);
        in.setDimensions(new int[]{1, "hello".length()});
        in.setCharData(new String[]{"hello"});
        inputs.put("input", in);

        Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
        expectedOutputs.put("output", MatArray.class);

        Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
                activity, inputs, expectedOutputs);
        
        assertTrue("There should be an output named output",
                outputs.containsKey("output"));
        assertEquals("hello_returned", ((MatArray) outputs.get("output")).
                getCharData()[0]);
    }
}
