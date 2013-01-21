package uk.ac.soton.mib104.t2.activities.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

import org.junit.Before;
import org.junit.Test;

import uk.ac.soton.mib104.t2.activities.oauth.util.ApiDescImpl;

public class OAuthActivityTest {

	private OAuthActivity activity = new OAuthActivity();

	private OAuthActivityConfigurationBean configBean;

	@Test
	public void configureActivity() throws Exception {
		final Set<String> expectedInputs = new HashSet<String>();
		expectedInputs.add("client_id");
		expectedInputs.add("client_secret");
		expectedInputs.add("redirect_uri");
		expectedInputs.add("scope");
		
		final Set<String> expectedOutputs = new HashSet<String>();
		expectedOutputs.add("access_token");
		expectedOutputs.add("access_token_secret");

		activity.configure(configBean);

		Set<ActivityInputPort> inputPorts = activity.getInputPorts();
		assertEquals(expectedInputs.size(), inputPorts.size());
		for (ActivityInputPort inputPort : inputPorts) {
			assertTrue("Wrong input : " + inputPort.getName(),
					expectedInputs.remove(inputPort.getName()));
		}

		Set<OutputPort> outputPorts = activity.getOutputPorts();
		assertEquals(expectedOutputs.size(), outputPorts.size());
		for (OutputPort outputPort : outputPorts) {
			assertTrue("Wrong output : " + outputPort.getName(),
					expectedOutputs.remove(outputPort.getName()));
		}
	}

	@Test(expected = RuntimeException.class)
	public void executeAsynch() throws Exception {
		activity.configure(configBean);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("client_id", "your_client_id");
		inputs.put("client_secret", "your_client_secret");
		inputs.put("redirect_uri", "http://www.example.com/oauth/callback");
		inputs.put("scope", "Example");

		Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put("access_token", String.class);
		expectedOutputTypes.put("access_token_secret", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputTypes);

		assertEquals("Unexpected outputs", 2, outputs.size());
		assertNotNull(outputs.get("access_token"));
		assertNull(outputs.get("access_token_secret"));
	}

	@Test(expected = ActivityConfigurationException.class)
	public void invalidConfiguration() throws ActivityConfigurationException {
	    /*
		final OAuthActivityConfigurationBean invalidBean = new OAuthActivityConfigurationBean(ApiDescImpl.OAuth20_draft10);
		
		invalidBean.setAccessTokenEndpoint(null);
		invalidBean.setAccessTokenVerb(null);
		
		invalidBean.setAuthorizationEndpoint(null);
	    */
		activity.configure(null);
	}

	@Before
	public void makeConfigBean() throws Exception {
		configBean = new OAuthActivityConfigurationBean(ApiDescImpl.OAuth20_draft10);
	}

	@Test
	public void reConfiguredActivity() throws Exception {
		assertEquals("Unexpected inputs", 0, activity.getInputPorts().size());
		assertEquals("Unexpected outputs", 0, activity.getOutputPorts().size());

		activity.configure(configBean);
		assertEquals("Unexpected inputs", 4, activity.getInputPorts().size());
		assertEquals("Unexpected outputs", 2, activity.getOutputPorts().size());

		activity.configure(configBean);
		// Should not change on reconfigure
		assertEquals("Unexpected inputs", 4, activity.getInputPorts().size());
		assertEquals("Unexpected outputs", 2, activity.getOutputPorts().size());
	}

//	@Test
//	public void reConfiguredSpecialPorts() throws Exception {
//		fail("Auto-generated test stub");
//	}
	
}
