package net.sf.taverna.t2.partition.algorithms;

import static org.junit.Assert.*;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

import org.junit.Test;

public class LiteralValuePartitionAlgorithmTest {
	
	@Test
	public void testEquals() {
		LiteralValuePartitionAlgorithm a = new LiteralValuePartitionAlgorithm();
		LiteralValuePartitionAlgorithm b = new LiteralValuePartitionAlgorithm();
		LiteralValuePartitionAlgorithm c = new LiteralValuePartitionAlgorithm();
		
		a.setPropertyName("cheese");
		b.setPropertyName("cheese");
		c.setPropertyName("butter");
		
		assertEquals("They should be equal",a,a);
		assertEquals("They should be equal",a,b);
		assertFalse("They should not be equal",a.equals(c));
		assertFalse("They should not be equal",a.equals("cheese"));
	}
	
	@Test
	public void testHashcode() {
		LiteralValuePartitionAlgorithm a = new LiteralValuePartitionAlgorithm();
		LiteralValuePartitionAlgorithm b = new LiteralValuePartitionAlgorithm();
		LiteralValuePartitionAlgorithm c = new LiteralValuePartitionAlgorithm();
		
		a.setPropertyName("cheese");
		b.setPropertyName("cheese");
		c.setPropertyName("Z");
		
		assertEquals("They should have the same hashcode",a.hashCode(),b.hashCode());
	}
	
	@Test
	public void testConstructor() {
		LiteralValuePartitionAlgorithm p = new LiteralValuePartitionAlgorithm();
		assertNull("The property shoudl default to null",p.getPropertyName());
		
		p=new LiteralValuePartitionAlgorithm("pea");
		assertEquals("The property name should default to 'pea'","pea",p.getPropertyName());
	}
}

