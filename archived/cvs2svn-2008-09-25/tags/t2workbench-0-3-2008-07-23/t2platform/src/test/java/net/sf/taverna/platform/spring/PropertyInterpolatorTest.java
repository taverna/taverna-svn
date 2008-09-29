package net.sf.taverna.platform.spring;

import static net.sf.taverna.platform.spring.PropertyInterpolator.interpolate;

import java.util.Properties;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test case for the property interpolator, mostly because java regex is evil
 * 
 * @author Tom Oinn
 * 
 */
public class PropertyInterpolatorTest {

	static Properties exampleProperties() {
		Properties props = new Properties();
		props.put("a", "x");
		props.put("a.b", "xy");
		return props;
	}

	@Test
	public void testInterpolation() {
		assertEquals(interpolate("${a}", exampleProperties()), "x");
		assertEquals(interpolate("foo ${a} bar", exampleProperties()),
				"foo x bar");
		assertEquals(interpolate("foo ${a}${a.b} bar", exampleProperties()),
				"foo xxy bar");
	}

}
