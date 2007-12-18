package net.sf.taverna.t2.plugin;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class T2PerspectiveTest {
	private T2Perspective t2Perspective;
	
	@Before
	public void setUp() throws Exception {
		t2Perspective = new T2Perspective();
	}

	@Test
	public void testGetText() {
		assertEquals("Taverna 2 preview", t2Perspective.getText());
	}

}
