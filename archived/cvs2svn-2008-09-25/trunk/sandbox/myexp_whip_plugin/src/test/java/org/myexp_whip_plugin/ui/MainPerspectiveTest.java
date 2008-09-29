package org.myexp_whip_plugin.ui;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.myexp_whip_plugin.ui.MainPerspective;

public class MainPerspectiveTest {
	private MainPerspective perspective;
	
	@Before
	public void setUp() throws Exception {
		perspective = new MainPerspective();
	}

	@Test
	public void testGetText() {
		assertEquals("myExperiment (beta)", perspective.getText());
	}

}
