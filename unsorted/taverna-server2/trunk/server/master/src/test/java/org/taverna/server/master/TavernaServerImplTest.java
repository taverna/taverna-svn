package org.taverna.server.master;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.taverna.server.master.exceptions.BadPropertyValueException;
import org.taverna.server.master.exceptions.NoListenerException;
import org.taverna.server.master.exceptions.NoUpdateException;
import org.taverna.server.master.exceptions.UnknownRunException;
import org.taverna.server.master.interfaces.Listener;
import org.taverna.server.master.interfaces.TavernaRun;
import org.taverna.server.master.mocks.ExampleRun;
import org.taverna.server.master.mocks.SimpleListenerFactory;
import org.taverna.server.master.mocks.SimpleNonpersistentRunStore;

public class TavernaServerImplTest {
	private TavernaServerImpl server;
	private MockPolicy policy;
	private SimpleNonpersistentRunStore store;
	private ExampleRun.Builder runFactory;
	private SimpleListenerFactory lFactory;

	private String lrunname;
	private String lrunconf;

	Listener makeListener(TavernaRun run, final String config) {
		lrunname = run.toString();
		lrunconf = config;
		return new Listener() {
			@Override
			public String getConfiguration() {
				return config;
			}

			@Override
			public String getName() {
				return "bar";
			}

			@Override
			public String getProperty(String propName)
					throws NoListenerException {
				throw new NoListenerException();
			}

			@Override
			public String getType() {
				return "foo";
			}

			@Override
			public String[] listProperties() {
				return new String[0];
			}

			@Override
			public void setProperty(String propName, String value)
					throws NoListenerException, BadPropertyValueException {
				throw new NoListenerException();
			}
		};
	}

	{
		// Wire everything up; ought to be done with Spring, but this works...
		server = new TavernaServerImpl();
		server.policy = policy = new MockPolicy();
		server.runStore = store = new SimpleNonpersistentRunStore();
		store.setPolicy(policy);
		server.runFactory = runFactory = new ExampleRun.Builder(1);
		server.listenerFactory = lFactory = new SimpleListenerFactory();
		lFactory
				.setBuilders(singletonMap(
						"foo",
						(SimpleListenerFactory.Builder) new SimpleListenerFactory.Builder() {
							@Override
							public Listener build(TavernaRun run,
									String configuration)
									throws NoListenerException {
								return makeListener(run, configuration);
							}
						}));
	}

	@Test
	public void defaults1() {
		assertNotNull(server);
	}

	@Test
	public void defaults2() {
		assertEquals(10, server.getMaxSimultaneousRuns());
	}

	@Test
	public void defaults3() {
		assertEquals(1, server.getAllowedListeners().length);
	}

	@Test
	public void defaults4() {
		assertNull(server.getPrincipal());
	}

	@Test
	public void serverAsksPolicyForMaxRuns() {
		int oldmax = policy.maxruns;
		try {
			policy.maxruns = 1;
			assertEquals(1, server.getMaxSimultaneousRuns());
		} finally {
			policy.maxruns = oldmax;
		}
	}

	@Test
	public void makeAndKillARun() throws NoUpdateException, UnknownRunException {
		RunReference rr = server.submitWorkflow(null);
		assertNotNull(rr);
		assertNotNull(rr.name);
		server.destroyRun(rr.name);
	}

	@Test
	public void makeListenKillRun() throws Exception {
		RunReference run = server.submitWorkflow(null);
		try {
			lrunname = lrunconf = null;
			assertEquals(asList("foo"), asList(server.getAllowedListeners()));
			String l = server.addRunListener(run.name, "foo", "foobar");
			assertEquals("bar", l);
			assertEquals("foobar", lrunconf);
			assertEquals(lrunname, server.getRun(run.name).toString());
			assertEquals(asList("default", "bar"), asList(server
					.getRunListeners(run.name)));
			assertEquals(0,
					server.getRunListenerProperties(run.name, "bar").length);
		} finally {
			try {
				server.destroyRun(run.name);
			} catch (Exception e) {
				// Ignore
			}
		}
	}
}
