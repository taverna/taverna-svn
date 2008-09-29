package ${packageName};

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ExamplePerspectiveTest {
	private ExamplePerspective perspective;
	
	@Before
	public void setUp() throws Exception {
		perspective = new ExamplePerspective();
	}

	@Test
	public void testGetText() {
		assertEquals("Example", perspective.getText());
	}

}
