/**
 * 
 */
package uk.org.taverna.scufl2.validation.structural;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.iterationstrategy.DotProduct;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyStack;
import uk.org.taverna.scufl2.api.iterationstrategy.PortNode;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;


/**
 * @author alanrw
 *
 */
public class DotProductTest {
	
	private InputProcessorPort a;
	private InputProcessorPort b;

	private DotProduct getDot(int depthA, int depthB) {
		a = new InputProcessorPort();
		a.setName("a");
		a.setDepth(0);
		DotProduct dp = new DotProduct();
		PortNode nipn1 = new PortNode(dp, a);
		nipn1.setDesiredDepth(depthA);
		
		b = new InputProcessorPort();
		b.setName("b");
		b.setDepth(0);
		PortNode nipn2 = new PortNode(dp, b);
		nipn2.setDesiredDepth(depthB);

		return dp;
	}
	
	@Test
	public void testSingletonDotUnstagedIteration(){
		DotProduct dp = getDot(0, 0);
		
		StructuralValidator sv = new StructuralValidator();
		Map<InputProcessorPort, Integer> tempDepths = new HashMap<InputProcessorPort, Integer>();
		tempDepths.put(a, 1);
		tempDepths.put(b, 1);

		assertEquals(Integer.valueOf(1), sv.getIterationDepth(dp,
		tempDepths));
	}
	
	@Test
	public void testListDotUnstagedIteration() {
		DotProduct dp = getDot(0, 0);
		StructuralValidator sv = new StructuralValidator();
		Map<InputProcessorPort, Integer> tempDepths = new HashMap<InputProcessorPort, Integer>();
		tempDepths.put(a,2);
		tempDepths.put(b,2);
		
		assertEquals(Integer.valueOf(2), sv.getIterationDepth(dp,
		tempDepths));
	}
	
	@Test
	public void testDifferentDepthsDotUnstagedIteration() {
		DotProduct dp = getDot(0, 1);
		StructuralValidator sv = new StructuralValidator();
		Map<InputProcessorPort, Integer> tempDepths = new HashMap<InputProcessorPort, Integer>();
		tempDepths.put(a,3);
		tempDepths.put(b,4);
		
		assertEquals(Integer.valueOf(3), sv.getIterationDepth(dp,
		tempDepths));
		
	}
	
	@Test
	public void testValidationFailureWithDot() {
			DotProduct dp = getDot(0, 0);
			StructuralValidator sv = new StructuralValidator();
			Map<InputProcessorPort, Integer> tempDepths = new HashMap<InputProcessorPort, Integer>();
			tempDepths.put(a,1);
			tempDepths.put(b,2);
			assertNull(sv.getIterationDepth(dp,
		tempDepths));
	}
	
	@Test
	public void testStagedCombinationOfDot1() {
		
		Processor p = new Processor();
		IterationStrategyStack iss = new IterationStrategyStack(p);

	iss.add(getDot(1, 1));
	iss.add(getDot(0, 0));
	StructuralValidator sv = new StructuralValidator();
	sv.getValidatorState().setProcessor(p);
	Map<InputProcessorPort, Integer> tempDepths = new HashMap<InputProcessorPort, Integer>();
	tempDepths.put(a,2);
	tempDepths.put(b,2);

	assertEquals(Integer.valueOf(2), sv.calculateResultWrappingDepth(tempDepths));

}

	@Test
	public void testStagedCombinationOfDot2() {
		
		Processor p = new Processor();
		IterationStrategyStack iss = new IterationStrategyStack(p);

	iss.add(getDot(1, 1));
	iss.add(getDot(0, 0));
	StructuralValidator sv = new StructuralValidator();
	sv.getValidatorState().setProcessor(p);
	Map<InputProcessorPort, Integer> tempDepths = new HashMap<InputProcessorPort, Integer>();
	tempDepths.put(a,0);
	tempDepths.put(b,0);

	// Should pass because the single items (depth 0) are promoted to single
	// item lists before being passed into the iteration system so are
	// effectively both depth 1 going into the second stage which then
	// iterates to produce an index array length of 1
	assertEquals(Integer.valueOf(1), sv.calculateResultWrappingDepth(tempDepths));

}
	
	@Test
	public void testStagedCombinationOfDot3() {
		
		Processor p = new Processor();
		IterationStrategyStack iss = new IterationStrategyStack(p);

	iss.add(getDot(1, 1));
	iss.add(getDot(0, 0));
	StructuralValidator sv = new StructuralValidator();
	sv.getValidatorState().setProcessor(p);
	Map<InputProcessorPort, Integer> tempDepths = new HashMap<InputProcessorPort, Integer>();
	tempDepths.put(a,1);
	tempDepths.put(b,0);

	// Slightly strange superficially that this should work, but in fact
	// what happens is that the first single item is lifted to a list before
	// being passed to the iteration strategy. The result is that it's fine
	// to match with the dot product against the other list as neither at
	// this point have index arrays, then in the second stage both are lists
	// to be iterated over and both have single length index arrays.
	assertEquals(Integer.valueOf(1), sv.calculateResultWrappingDepth(tempDepths));

}
}
